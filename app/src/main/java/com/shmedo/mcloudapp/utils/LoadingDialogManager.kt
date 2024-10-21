package com.shmedo.mcloudapp.utils

/**
 * 创建者：gonghe
 * 创建时间：2024/10/21
 * 描述： TODO
 */
import android.os.Handler
import android.os.Looper
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogFragment
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object LoadingDialogManager {
    private val activeLoadings = ConcurrentHashMap<String, String>() //存储活跃加载请求的唯一标识符及其对应的消息
    private val cancellationCallbacks = ConcurrentHashMap<String, () -> Unit>()  // 存储每个加载请求对应的取消回调
    private var globalLoadingDialogRef: WeakReference<LoadingDialogFragment?> =
        WeakReference(null)// 持有当前显示的 LoadingDialogFragment 的弱引用，防止内存泄漏

    // 确保所有操作在主线程上执行
    private val mainHandler = Handler(Looper.getMainLooper())


    /**
     * 统一的显示加载对话框方法
     * @param message 显示的消息
     * @param onCancel 取消时的回调
     * @return loadingId，用于后续的关闭
     */
    private fun showLoadingInternal(
        message: String,
        onCancel: (() -> Unit)?,
        loadingId: String? = null
    ): String {
        val id = loadingId ?: UUID.randomUUID().toString()
        activeLoadings[id] = message
        if (onCancel != null) {
            cancellationCallbacks[id] = onCancel
        }

        mainHandler.post {
            val activity = CurrentActivityProvider.getCurrentActivity()
            if (activity == null || activity.isFinishing || activity.isDestroyed) {
                // 当前没有有效的 Activity，移除加载请求
                activeLoadings.remove(id)
                cancellationCallbacks.remove(id)
                return@post
            }

            val existingDialog = globalLoadingDialogRef.get()

            if (existingDialog == null || existingDialog.isRemoving) {
                val newLoadingDialog = LoadingDialogFragment.newInstance(message)
                newLoadingDialog.show(activity.supportFragmentManager, LoadingDialogFragment.TAG)
                globalLoadingDialogRef = WeakReference(newLoadingDialog)
            } else {
                existingDialog.viewModel.updateMessage(message)
            }
        }

        return id
    }

    /**
     * 统一的关闭加载对话框方法
     * @param loadingId 要关闭的加载请求的唯一标识符
     */
    private fun dismissLoadingInternal(loadingId: String) {
        if (!activeLoadings.containsKey(loadingId)) {
            //没有这样的 loadingId，忽略
            return
        }
        activeLoadings.remove(loadingId)
        cancellationCallbacks.remove(loadingId)

        // 根据 loadingId 关闭对应的对话框
        if (activeLoadings.isEmpty()) {
            mainHandler.post {
                val loadingDialog = globalLoadingDialogRef.get()
                loadingDialog?.let {
                    if (it.isAdded && !it.isRemoving) {
                        it.dismissAllowingStateLoss()
                    }
                    globalLoadingDialogRef.clear()
                }
            }
        }
    }

    /**
     * 旧方法：显示加载对话框（无需 loadingId）
     * 通过内部生成的 loadingId 进行管理
     */
    fun showLoading(message: String = "加载中...", onCancel: (() -> Unit)? = null) {
        //增加计数
        showLoadingInternal(message, onCancel = onCancel, loadingId = "GLOBAL_LOADING")
    }

    /**
     * 旧方法：关闭加载对话框（无需 loadingId）
     * 通过内部的 loadingId 进行管理
     */
    fun dismissLoading() {
        dismissLoadingInternal("GLOBAL_LOADING")
    }

    /**
     * 新方法：显示加载对话框，并返回 loadingId
     *
     * @param message 要显示的消息，默认为 "加载中..."
     * @param onCancel 加载对话框被取消时执行的回调，用于取消相关操作（如网络请求）
     * @return 生成的 loadingId
     */
    fun showLoadingWithId(message: String = "加载中...", onCancel: (() -> Unit)? = null): String {
        return showLoadingInternal(message, onCancel = onCancel, loadingId = null)
    }

    /**
     * 新方法：关闭指定 loadingId 的加载对话框
     *
     * @param loadingId 要关闭的加载请求的唯一标识符
     */
    fun dismissLoadingWithId(loadingId: String) {
        dismissLoadingInternal(loadingId)
    }

    /**
     * 新方法：更新指定 loadingId 的加载对话框消息
     *
     * @param loadingId 要更新的加载请求的唯一标识符
     * @param message 新的消息内容
     */
    fun updateMessageWithId(loadingId: String, message: String) {
        if (!activeLoadings.containsKey(loadingId)) {
            // 无此 loadingId，忽略
            return
        }
        activeLoadings[loadingId] = message

        mainHandler.post {
            val loadingDialog = globalLoadingDialogRef.get()
            if (loadingDialog != null && loadingDialog.isAdded && !loadingDialog.isRemoving) {
                loadingDialog.viewModel.updateMessage(message)
            }
        }
    }


    /**
     * 当 LoadingDialogFragment 被取消时调用
     * 执行所有注册的取消回调
     */
    internal fun onDialogCanceled() {
        mainHandler.post {
            //执行所有取消回调
            cancellationCallbacks.values.forEach { it.invoke() }

            //重置有 `loadingId` 的加载请求
            activeLoadings.clear()
            cancellationCallbacks.clear()

            // 关闭对话框
            val loadingDialog = globalLoadingDialogRef.get()
            loadingDialog?.let {
                if (it.isAdded && !it.isRemoving) {
                    it.dismissAllowingStateLoss()
                }
                globalLoadingDialogRef.clear()
            }
        }
    }


    /**
     * 重置所有加载请求（适用于 Activity 销毁等情况）
     */
    fun resetAll() {
        activeLoadings.clear()
        cancellationCallbacks.clear()
        globalLoadingDialogRef.clear()

        mainHandler.post {
            // 关闭对话框
            val loadingDialog = globalLoadingDialogRef.get()
            loadingDialog?.let {
                if (it.isAdded && !it.isRemoving) {
                    it.dismissAllowingStateLoss()
                }
                globalLoadingDialogRef.clear()
            }
        }
    }
}