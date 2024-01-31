package com.shmedo.mcloudapp.common.ext

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.OnBackPressedListener
import com.shmedo.mcloudapp.common.fragment.LoadingDialogFragment

/******************* loading框 版本1 start *************************/
////loading框
//private var loadingDialog: MaterialDialog? = null
//
///**
// * 打开等待框
// */
//fun AppCompatActivity.showLoadingDialog(message: String = "请求网络中") {
//    if (!this.isFinishing) {
//        if (loadingDialog == null) {
//            loadingDialog = MaterialDialog(this)
//                .cancelable(true)
//                .cancelOnTouchOutside(false)
//                .cornerRadius(12f)
//                .customView(R.layout.layout_custom_progress_dialog_view)
//                .lifecycleOwner(this)
//            loadingDialog?.getCustomView()?.run {
//                this.findViewById<TextView>(R.id.loading_tips).text = message
////                this.findViewById<ProgressBar>(R.id.progressBar).indeterminateTintList = SettingUtil.getOneColorStateList(this@showLoadingExt)
//            }
//        }
//        loadingDialog?.show()
//    }
//}
//
///**
// * 打开等待框
// */
//fun Fragment.showLoadingDialog(message: String = "请求网络中") {
//    activity?.let {
//        if (!it.isFinishing) {
//            if (loadingDialog == null) {
//                loadingDialog = MaterialDialog(it)
//                    .cancelable(true)
//                    .cancelOnTouchOutside(false)
//                    .customView(R.layout.layout_custom_progress_dialog_view)
//                    .lifecycleOwner(viewLifecycleOwner)
//                loadingDialog?.getCustomView()?.run {
//                    this.findViewById<TextView>(R.id.loading_tips).text = message
////                    this.findViewById<ProgressBar>(R.id.progressBar).indeterminateTintList = SettingUtil.getOneColorStateList(it)
//                }
//            }
//            loadingDialog?.show()
//        }
//    }
//}
//
///**
// * 关闭等待框
// */
//fun Activity.dismissLoadingDialog() {
//    loadingDialog?.dismiss()
//    loadingDialog = null
//}
//
///**
// * 关闭等待框
// */
//fun Fragment.dismissLoadingDialog() {
//    loadingDialog?.dismiss()
//    loadingDialog = null
//}
/******************* loading框 版本1 end *************************/

/******************* loading框 版本2 start TODO 此版本存在问题 *************************/
///**
// * 打开等待框
// */
//fun AppCompatActivity.showLoadingDialog(message: String = "请求网络中") {
//    if (!this.isFinishing) {
//        val loadingDialog = MaterialDialog(this)
//            .cancelable(true)
//            .cancelOnTouchOutside(false)
//            .cornerRadius(12f)
//            .customView(R.layout.layout_custom_progress_dialog_view)
//            .lifecycleOwner(this)
//
//        loadingDialog.getCustomView().findViewById<TextView>(R.id.loading_tips)?.text = message
//
//        loadingDialog.show()
//    }
//}
//
///**
// * 关闭等待框
// */
//fun AppCompatActivity.dismissLoadingDialog() {
//    try {
//        val dialog = this::class.java.getDeclaredField("loadingDialog")
//        dialog.isAccessible = true
//        (dialog.get(this) as? MaterialDialog)?.dismiss()
//        dialog.set(this, null)
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}
//
///**
// * 在 Fragment 中显示等待框
// */
//fun Fragment.showLoadingDialog(message: String = "请求网络中") {
//    if (!isAdded) return  // 确保 Fragment 已经添加到其宿主 Activity
//
//    (activity as? AppCompatActivity)?.showLoadingDialog(message)
//}
//
///**
// * 在 Fragment 中关闭等待框
// */
//fun Fragment.dismissLoadingDialog() {
//    if (!isAdded) return  // 确保 Fragment 已经添加到其宿主 Activity
//
//    (activity as? AppCompatActivity)?.dismissLoadingDialog()
//}
/******************* loading框 版本2 end *************************/

fun AppCompatActivity.showLoadingDialog(message: String = "请求网络中") {
    val fragmentManager = supportFragmentManager
    val existingDialog = fragmentManager.findFragmentByTag("loadingDialog")
    if (existingDialog == null) {
        val dialogFragment = LoadingDialogFragment.newInstance(message)
        dialogFragment.show(fragmentManager, "loadingDialog")
    }
}

fun AppCompatActivity.dismissLoadingDialog() {
    val fragmentManager = supportFragmentManager
    val existingDialog = fragmentManager.findFragmentByTag("loadingDialog") as? DialogFragment
    existingDialog?.dismiss()
}


fun Fragment.showLoadingDialog(message: String = "请求网络中") {
    if (!isAdded) return  // 确保 Fragment 已经添加到其宿主 Activity

    val fragmentManager = childFragmentManager
    val existingDialog = fragmentManager.findFragmentByTag("loadingDialog")
    if (existingDialog == null) {
        val dialogFragment = LoadingDialogFragment.newInstance(message)
        dialogFragment.show(fragmentManager, "loadingDialog")
    }
}

fun Fragment.dismissLoadingDialog() {
    if (!isAdded) return  // 确保 Fragment 已经添加到其宿主 Activity

    val fragmentManager = childFragmentManager
    val existingDialog = fragmentManager.findFragmentByTag("loadingDialog") as? DialogFragment
    existingDialog?.dismiss()
}


fun Activity.showWaitDialog(message: String = "请求网络中") {
    WaitDialog.show(message).onBackPressedListener = OnBackPressedListener {
        WaitDialog.dismiss()
        return@OnBackPressedListener false
    }
}

fun Activity.dismissWaitDialog() {
    WaitDialog.dismiss()
}

fun Fragment.showWaitDialog(message: String = "请求网络中") {
    WaitDialog.show(message).onBackPressedListener = OnBackPressedListener {
        WaitDialog.dismiss()
        return@OnBackPressedListener false
    }
}

fun Fragment.dismissWaitDialog() {
    WaitDialog.dismiss()
}
