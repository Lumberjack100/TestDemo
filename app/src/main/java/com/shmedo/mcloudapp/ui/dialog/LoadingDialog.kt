package com.shmedo.mcloudapp.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLoadingDialogBinding
import com.shmedo.mcloudapp.utils.LoadingDialogManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author：gonghe
 * @time: 2025/1/7
 * @desc: 重构后的LoadingDialog
 * 
 * 特点：
 * 1. 基于原生Dialog，避免Fragment生命周期问题
 * 2. 简化状态管理，不依赖ViewModel
 * 3. 线程安全的状态控制
 * 4. 自动内存管理和异常恢复
 */
class LoadingDialog private constructor(
    private val context: Context,
    private val config: LoadingDialogConfig
) : Dialog(context, R.style.TransparentLoadingDialog) {

    private val binding: FragmentLoadingDialogBinding by lazy {
        FragmentLoadingDialogBinding.inflate(LayoutInflater.from(context))
    }
    
    private val mainHandler = Handler(Looper.getMainLooper())
    private var timeoutJob: Job? = null
    private val isDestroyed = AtomicBoolean(false)
    
    init {
        initDialog()
        setupTimeout()
    }

    private fun initDialog() {
        try {
            setContentView(binding.root)
            setCancelable(config.isCancelable)
            setCanceledOnTouchOutside(false)
            
            // 设置窗口属性
            window?.let { window ->
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.setDimAmount(0.5f)
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                
                // 防止窗口泄漏 - 让系统自动管理焦点
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
            
            // 设置取消监听器
            setOnCancelListener { 
                safeExecute { config.onCancel?.invoke() }
            }
            
            updateMessage(config.message)
            
        } catch (e: Exception) {
            Timber.e(e, "LoadingDialog init failed")
        }
    }

    private fun setupTimeout() {
        if (config.timeoutDuration > 0) {
            timeoutJob = CoroutineScope(Dispatchers.Main).launch {
                try {
                    delay(config.timeoutDuration)
                    if (!isDestroyed.get()) {
                        safeExecute { 
                            // 通过Manager来关闭，保持一致性
                            LoadingDialogManager.dismissLoading(config.loadingId)
                        }
                    }
                } catch (e: CancellationException) {
                    // 正常取消，忽略
                } catch (e: Exception) {
                    Timber.w(e, "Timeout job failed")
                }
            }
        }
    }

    fun updateMessage(message: String) {
        safeExecute {
            if (!isDestroyed.get()) {
                binding.tvMessage.text = message
            }
        }
    }

    override fun show() {
        safeExecute {
            if (!isDestroyed.get() && context is AppCompatActivity && 
                !context.isFinishing && !context.isDestroyed) {
                try {
                    super.show()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to show LoadingDialog")
                    // 如果显示失败，通知Manager清理引用
                    LoadingDialogManager.onDialogShowFailed(config.loadingId)
                }
            }
        }
    }

    override fun dismiss() {
        safeExecute {
            if (!isDestroyed.get()) {
                try {
                    super.dismiss()
                } catch (e: Exception) {
                    Timber.w(e, "Failed to dismiss LoadingDialog")
                } finally {
                    cleanup()
                }
            }
        }
    }

    private fun cleanup() {
        isDestroyed.set(true)
        timeoutJob?.cancel()
        timeoutJob = null
    }

    private fun safeExecute(action: () -> Unit) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                action()
            } else {
                mainHandler.post { action() }
            }
        } catch (e: Exception) {
            Timber.w(e, "SafeExecute failed")
        }
    }

    companion object {
        fun create(context: Context, config: LoadingDialogConfig): LoadingDialog {
            return LoadingDialog(context, config)
        }
    }
}
