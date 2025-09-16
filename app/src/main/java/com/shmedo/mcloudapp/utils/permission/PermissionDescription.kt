package com.shmedo.mcloudapp.utils.permission

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionDescription
import com.hjq.permissions.permission.PermissionPageType
import com.hjq.permissions.permission.base.IPermission
import com.shmedo.mcloudapp.R


/**
 * 创建者：gonghe
 * 创建时间：2025/9/15
 * 描述： 权限请求描述实现
 */
class PermissionDescription : OnPermissionDescription {

    /** 消息处理 Handler 对象 */
    companion object {
        val HANDLER: Handler = Handler(Looper.getMainLooper())

        /** 权限请求描述弹窗显示类型：Dialog */
        private const val DESCRIPTION_WINDOW_TYPE_DIALOG = 0

        /** 权限请求描述弹窗显示类型：PopupWindow */
        private const val DESCRIPTION_WINDOW_TYPE_POPUP = 1
    }

    /** 权限请求描述弹窗显示类型 */
    private var mDescriptionWindowType: Int = DESCRIPTION_WINDOW_TYPE_DIALOG

    /** 消息 Token */
    private val mHandlerToken: Any = Any()

    /** 权限申请说明弹窗 */
    private var mPermissionPopupWindow: PopupWindow? = null

    /** 权限申请说明对话框 */
    private var mPermissionDialog: Dialog? = null

    override fun askWhetherRequestPermission(
        activity: Activity,
        requestList: List<IPermission>,
        continueRequestRunnable: Runnable,
        breakRequestRunnable: Runnable
    ) {
        mDescriptionWindowType = DESCRIPTION_WINDOW_TYPE_POPUP
        for (permission in requestList) {
            if (permission.getPermissionPageType(activity) == PermissionPageType.OPAQUE_ACTIVITY) {
                mDescriptionWindowType = DESCRIPTION_WINDOW_TYPE_DIALOG
            }
        }

        if (mDescriptionWindowType == DESCRIPTION_WINDOW_TYPE_POPUP) {
            continueRequestRunnable.run()
            return
        }

        showDialog(
            activity = activity,
            dialogTitle = activity.getString(com.shmedo.lib.core.R.string.common_permission_description_title),
            dialogMessage = generatePermissionDescription(activity, requestList),
            confirmButtonText = activity.getString(com.shmedo.lib.core.R.string.common_permission_confirm)
        ) { dialog, _ ->
            dialog?.dismiss()
            continueRequestRunnable.run()
        }
    }

    override fun onRequestPermissionStart(activity: Activity, requestList: List<IPermission>) {
        if (mDescriptionWindowType != DESCRIPTION_WINDOW_TYPE_POPUP) {
            return
        }

        val showPopupRunnable = Runnable {
            showPopupWindow(activity, generatePermissionDescription(activity, requestList))
        }
        // 这里解释一下为什么要延迟一段时间再显示 PopupWindow，这是因为系统没有开放任何 API 给外层直接获取权限是否永久拒绝
        // 目前只有申请过了权限才能通过 shouldShowRequestPermissionRationale 判断是不是永久拒绝，如果此前没有申请过权限，则无法判断
        // 针对这个问题能想到最佳的解决方案是：先申请权限，如果极短的时间内，权限申请没有结束，则证明权限之前没有被用户勾选了《不再询问》
        // 此时系统的权限弹窗正在显示给用户，这个时候再去显示应用的 PopupWindow 权限说明弹窗给用户看，所以这个 PopupWindow 是在发起权限申请后才显示的
        // 这样做是为了避免 PopupWindow 显示了又马上消失，这样就不会出现 PopupWindow 一闪而过的效果，提升用户的视觉体验
        // 最后补充一点：350 毫秒只是一个经验值，经过测试可覆盖大部分机型，具体可根据实际情况进行调整，这里不做强制要求
        // 相关 Github issue 地址：https://github.com/getActivity/XXPermissions/issues/366
        HANDLER.postAtTime(showPopupRunnable, mHandlerToken, SystemClock.uptimeMillis() + 350)
    }

    override fun onRequestPermissionEnd(activity: Activity, requestList: List<IPermission>) {
        // 移除跟这个 Token 有关但是没有还没有执行的消息
        HANDLER.removeCallbacksAndMessages(mHandlerToken)
        // 销毁当前正在显示的弹窗
        dismissPopupWindow()
        dismissDialog()
    }

    /**
     * 生成权限描述文案
     */
    private fun generatePermissionDescription(activity: Activity, requestList: List<IPermission>): String {
        return PermissionConverter.getDescriptionsByPermissions(activity, requestList)
    }

    /**
     * 显示 Dialog
     *
     * @param dialogTitle               对话框标题
     * @param dialogMessage             对话框消息
     * @param confirmButtonText         对话框确认按钮文本
     * @param confirmListener           对话框确认按钮点击事件
     */
    private fun showDialog(
        activity: Activity,
        dialogTitle: String?,
        dialogMessage: String?,
        confirmButtonText: String?,
        confirmListener: DialogInterface.OnClickListener?
    ) {
        mPermissionDialog?.let { dismissDialog() }

        if (activity.isFinishing || activity.isDestroyed) return

        // 另外这里需要判断 Activity 的类型来申请权限，这是因为只有 AppCompatActivity 才能调用 Support 库的 AlertDialog 来显示，否则会出现报错
        // java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity
        // 为什么不直接用 App 包 AlertDialog 来显示，而是两套规则？因为 App 包 AlertDialog 是系统自带的类，不同 Android 版本展现的样式可能不太一样
        // 如果这个 Android 版本比较低，那么这个对话框的样式就会变得很丑，准确来讲也不能说丑，而是当时系统的 UI 设计就是那样，它只是跟随系统的样式而已
        mPermissionDialog = if (activity is AppCompatActivity) {
            AlertDialog.Builder(activity)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                // 对话框一定要设置成不可取消的
                .setCancelable(false)
                .setPositiveButton(confirmButtonText, confirmListener)
                .create()
        } else {
            android.app.AlertDialog.Builder(activity)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                // 对话框一定要设置成不可取消的
                .setCancelable(false)
                .setPositiveButton(confirmButtonText, confirmListener)
                .create()
        }

        mPermissionDialog?.show()

        // 将 Activity 和 Dialog 生命周期绑定在一起，避免可能会出现的内存泄漏
        // 当然如果上面创建的 Dialog 已经有做了生命周期管理，则不需要执行下面这行代码
//        mPermissionDialog?.let { WindowLifecycleManager.bindDialogLifecycle(activity, it) }
    }

    /**
     * 销毁 Dialog
     */
    private fun dismissDialog() {
        val dialog = mPermissionDialog ?: return
        if (dialog.isShowing) {
            dialog.dismiss()
        }
        mPermissionDialog = null
    }

    /**
     * 显示 PopupWindow
     *
     * @param content               弹窗显示的内容
     */
    private fun showPopupWindow(activity: Activity, content: String) {
        mPermissionPopupWindow?.let { dismissPopupWindow() }
        if (activity.isFinishing || activity.isDestroyed) return

        val decorView = activity.window.decorView as ViewGroup
        val contentView = LayoutInflater.from(activity)
            .inflate(R.layout.permission_description_popup, decorView, false)

        mPermissionPopupWindow = PopupWindow(activity).apply {
            setContentView(contentView)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            animationStyle = android.R.style.Animation_Dialog
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isTouchable = true
            isOutsideTouchable = true
        }

        val messageView: TextView =
            mPermissionPopupWindow!!.contentView.findViewById(R.id.tv_permission_description_message)
        messageView.text = content

        mPermissionPopupWindow!!.showAtLocation(decorView, Gravity.TOP, 0, 0)

        // 将 Activity 和 PopupWindow 生命周期绑定在一起，避免可能会出现的内存泄漏
        // 当然如果上面创建的 PopupWindow 已经有做了生命周期管理，则不需要执行下面这行代码
//        WindowLifecycleManager.bindPopupWindowLifecycle(activity, mPermissionPopupWindow!!)
    }

    /**
     * 销毁 PopupWindow
     */
    private fun dismissPopupWindow() {
        val popup = mPermissionPopupWindow ?: return
        if (popup.isShowing) {
            popup.dismiss()
        }
        mPermissionPopupWindow = null
    }

}