package com.shmedo.mcloudapp.utils


import android.os.Handler
import android.os.Looper
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingConfig
import com.shmedo.mcloudapp.ui.viewmodel.state.LoadingDialogState
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


/**
 * 创建者：gonghe
 * 创建时间：2024/10/21
 * 描述： TODO
 */
object LoadingDialogManager {
    private val lock = Any()
    private val activeLoadings = ConcurrentHashMap<String, LoadingConfig>()//存储活跃加载请求的唯一标识符及其对应的消息
    private val loadingQueue = LinkedList<LoadingConfig>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var weakDialog =
        WeakReference<LoadingDialogFragment?>(null) //持有当前显示的 LoadingDialogFragment 的弱引用，防止内存泄漏
    private var lastShowTime: Long = 0

    private const val AUTO_RECOVER_THRESHOLD = 30000L // 30秒自动恢复阈值

    @Synchronized
    fun showLoading(
        message: String,
        loadingId: String? = null,
        isCancelable: Boolean = true,
        timeout: Long = LoadingDialogState.DEFAULT_TIMEOUT,
        onCancel: (() -> Unit)? = null
    ): String {
        try {
            synchronized(lock) {
                val id = loadingId ?: UUID.randomUUID().toString()
                val config = LoadingConfig(
                    message = message,
                    loadingId = id,
                    isCancelable = isCancelable,
                    timeoutDuration = timeout,
                    onCancel = onCancel
                )

                activeLoadings[id] = config
                loadingQueue.offer(config)
                lastShowTime = System.currentTimeMillis()

                mainHandler.post {
                    processNextLoading()
                }

                return id
            }
        } catch (e: Exception) {
            resetAll()
            throw e
        }
    }

    private fun processNextLoading() {
        synchronized(lock) {
            if (loadingQueue.isEmpty()) return
            val config = loadingQueue.peek()
            showDialogIfNeeded(config)
        }
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
        synchronized(lock) {
            activeLoadings.remove(loadingId)
            loadingQueue.removeIf { it.loadingId == loadingId }

            if (activeLoadings.isEmpty()) {
                // 清理所有待执行的消息
                mainHandler.removeCallbacksAndMessages(null)
                weakDialog.get()?.let { dialog ->
                    dialog.viewModel.clearTimeoutJobs()
                    dialog.dismissAllowingStateLoss()
                }
                weakDialog.clear()
            } else {
                processNextLoading()
            }
        }
    }

    @Synchronized
    fun updateMessage(loadingId: String, message: String) {
        synchronized(lock) {
            activeLoadings[loadingId]?.let { config ->
                activeLoadings[loadingId] = config.copy(message = message)
                mainHandler.post {
                    weakDialog.get()?.viewModel?.updateMessage(loadingId, message)
                }
            }
        }
    }

    internal fun onDialogCanceled(loadingId: String) {
        synchronized(lock) {
            activeLoadings[loadingId]?.onCancel?.invoke()
            dismissLoading(loadingId)
        }
    }

    fun resetAll() {
        synchronized(lock) {
            activeLoadings.clear()
            loadingQueue.clear()
            mainHandler.removeCallbacksAndMessages(null)
            weakDialog.get()?.let { dialog ->
                dialog.viewModel.clearTimeoutJobs()
                dialog.dismissAllowingStateLoss()
            }
            weakDialog.clear()
        }
    }

    // 添加自动恢复机制
    private fun autoRecover() {
        if (System.currentTimeMillis() - lastShowTime > AUTO_RECOVER_THRESHOLD) {
            resetAll()
        }
    }


}

