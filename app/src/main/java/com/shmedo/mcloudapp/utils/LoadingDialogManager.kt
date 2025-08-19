package com.shmedo.mcloudapp.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.shmedo.mcloudapp.ui.dialog.LoadingDialog
import com.shmedo.mcloudapp.ui.dialog.LoadingDialogConfig
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 创建者：gonghe
 * 创建时间：2024/10/21
 * 重构者：gonghe
 * 重构时间：2025/1/7
 * 描述：重构后的LoadingDialogManager
 * 
 * 特点：
 * 1. 简化的状态管理，避免复杂的队列机制
 * 2. 更安全的Context管理
 * 3. 自动清理和异常恢复
 * 4. 线程安全操作
 */
object LoadingDialogManager {
    
    private val activeDialogs = ConcurrentHashMap<String, WeakReference<LoadingDialog>>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lastAccessTime = AtomicLong(0)
    
    private const val AUTO_CLEANUP_INTERVAL = 30_000L // 30秒自动清理

    @JvmStatic
    @Synchronized
    fun showLoading(
        message: String,
        loadingId: String? = null,
        isCancelable: Boolean = true,
        timeout: Long = LoadingDialogConfig.DEFAULT_TIMEOUT,
        onCancel: (() -> Unit)? = null
    ): String {
        return try {
            val actualLoadingId = loadingId ?: UUID.randomUUID().toString()
            val context = getCurrentActivitySafely() ?: return actualLoadingId
            
            // 先关闭已存在的同ID对话框
            dismissLoading(actualLoadingId)
            
            val config = LoadingDialogConfig(
                message = message,
                loadingId = actualLoadingId,
                isCancelable = isCancelable,
                timeoutDuration = timeout,
                onCancel = onCancel
            )
            
            val dialog = LoadingDialog.create(context, config)
            activeDialogs[actualLoadingId] = WeakReference(dialog)
            lastAccessTime.set(System.currentTimeMillis())
            
            mainHandler.post {
                try {
                    dialog.show()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to show loading dialog")
                    activeDialogs.remove(actualLoadingId)
                }
            }
            
            actualLoadingId
        } catch (e: Exception) {
            Timber.e(e, "ShowLoading failed")
            loadingId ?: LoadingDialogConfig.DEFAULT_LOADING_ID
        }
    }

    @JvmStatic
    @Synchronized
    fun dismissLoading(loadingId: String = LoadingDialogConfig.DEFAULT_LOADING_ID) {
        try {
            activeDialogs[loadingId]?.get()?.let { dialog ->
                mainHandler.post {
                    try {
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to dismiss loading dialog")
                    }
                }
            }
            activeDialogs.remove(loadingId)
            lastAccessTime.set(System.currentTimeMillis())
        } catch (e: Exception) {
            Timber.e(e, "DismissLoading failed")
        }
    }

    @JvmStatic
    @Synchronized
    fun updateMessage(loadingId: String, message: String) {
        try {
            activeDialogs[loadingId]?.get()?.updateMessage(message)
            lastAccessTime.set(System.currentTimeMillis())
        } catch (e: Exception) {
            Timber.e(e, "UpdateMessage failed")
        }
    }

    @JvmStatic
    @Synchronized
    fun dismissAll() {
        try {
            activeDialogs.values.forEach { ref ->
                ref.get()?.let { dialog ->
                    mainHandler.post {
                        try {
                            dialog.dismiss()
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to dismiss dialog in dismissAll")
                        }
                    }
                }
            }
            activeDialogs.clear()
            lastAccessTime.set(System.currentTimeMillis())
        } catch (e: Exception) {
            Timber.e(e, "DismissAll failed")
        }
    }

    /**
     * 用于LoadingDialog内部调用，处理对话框显示失败
     */
    internal fun onDialogShowFailed(loadingId: String) {
        try {
            activeDialogs.remove(loadingId)
        } catch (e: Exception) {
            Timber.w(e, "Failed to handle dialog show failure")
        }
    }

    private fun getCurrentActivitySafely(): Context? {
        return try {
            val activity = CurrentActivityProvider.getCurrentActivity()
            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                activity
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to get current activity")
            null
        }
    }

    // 自动清理无效引用
    @JvmStatic
    fun performCleanup() {
        try {
            if (System.currentTimeMillis() - lastAccessTime.get() > AUTO_CLEANUP_INTERVAL) {
                synchronized(this) {
                    val iterator = activeDialogs.entries.iterator()
                    while (iterator.hasNext()) {
                        val entry = iterator.next()
                        if (entry.value.get() == null) {
                            iterator.remove()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Cleanup failed")
        }
    }
}

