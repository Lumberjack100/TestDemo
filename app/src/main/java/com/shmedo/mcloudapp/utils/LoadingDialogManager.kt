package com.shmedo.mcloudapp.utils


import android.os.Handler
import android.os.Looper
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingConfig
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingDialogState
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


/**
 * 创建者：gonghe
 * 创建时间：2024/10/21
 * 描述： TODO
 */
object LoadingDialogManager {
    private val activeLoadings = ConcurrentHashMap<String, LoadingConfig>()//存储活跃加载请求的唯一标识符及其对应的消息
    private val mainHandler = Handler(Looper.getMainLooper())
    private var weakDialog =
        WeakReference<LoadingDialogFragment?>(null) //持有当前显示的 LoadingDialogFragment 的弱引用，防止内存泄漏

    @Synchronized
    fun showLoading(
        message: String,
        loadingId: String? = null,
        isCancelable: Boolean = true,
        timeout: Long = LoadingDialogState.DEFAULT_TIMEOUT,
        onCancel: (() -> Unit)? = null
    ): String {
        val id = loadingId ?: UUID.randomUUID().toString()
        val config = LoadingConfig(
            message = message,
            loadingId = id,
            isCancelable = isCancelable,
            timeoutDuration = timeout,
            onCancel = onCancel
        )
        activeLoadings[id] = config

        mainHandler.post {
            showDialogIfNeeded(config)
        }

        return id
    }

    private fun showDialogIfNeeded(config: LoadingConfig) {
        val activity = CurrentActivityProvider.getCurrentActivity() ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val currentDialog = weakDialog.get()
        if (currentDialog == null || !currentDialog.isAdded || currentDialog.isRemoving) {
            val newDialog = LoadingDialogFragment.newInstance(config)
            weakDialog = WeakReference(newDialog)
            newDialog.show(activity.supportFragmentManager, LoadingDialogFragment.TAG)
        } else {
            currentDialog.viewModel.updateMessage(config.loadingId, config.message)
        }
    }

    @Synchronized
    fun dismissLoading(loadingId: String = LoadingDialogState.GLOBAL_LOADING) {
        activeLoadings.remove(loadingId)

        if (activeLoadings.isEmpty()) {
            mainHandler.post {
                weakDialog.get()?.dismissAllowingStateLoss()
                weakDialog.clear()
            }
        }
    }

    @Synchronized
    fun updateMessage(loadingId: String, message: String) {
        activeLoadings[loadingId]?.let { config ->
            activeLoadings[loadingId] = config.copy(message = message)
            mainHandler.post {
                weakDialog.get()?.viewModel?.updateMessage(loadingId, message)
            }
        }
    }

    internal fun onDialogCanceled(loadingId: String) {
        activeLoadings[loadingId]?.onCancel?.invoke()
        dismissLoading(loadingId)
    }

    fun resetAll() {
        activeLoadings.clear()
        mainHandler.post {
            weakDialog.get()?.dismissAllowingStateLoss()
            weakDialog.clear()
        }
    }
}

