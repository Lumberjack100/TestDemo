package com.shmedo.mcloudapp.ui.page.device.hac.dialog

import android.content.Context
import android.text.InputFilter
import androidx.databinding.DataBindingUtil
import com.hjq.toast.Toaster
import com.lxj.xpopup.core.BottomPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseDialogClickProxy
import com.shmedo.mcloudapp.databinding.HacConfigNumberBottomDialogBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity

/**
 * 创建者：gonghe
 * 创建时间：2024/11/27
 * 描述： TODO
 */
class HacConfigNumberBottomDialog(
    context: Context,
    private val config: DialogConfig
) : BottomPopupView(context) {

    private lateinit var binding: HacConfigNumberBottomDialogBinding
    private val mStates: HacConfigNumberViewModel by lazy {
        (context as BaseActivity).getActivityScopeViewModel()
    }

    data class DialogConfig(
        val title: String = "",
        val number: String = "",
        val onConfirm: (String, DialogCallback) -> Unit = { _, _ -> }
    ) {
        class Builder {
            private var title: String = ""
            private var number: String = ""
            private var onConfirm: (String, DialogCallback) -> Unit = { _, _ -> }

            fun setTitle(title: String) = apply { this.title = title }
            fun setNumber(number: String) = apply { this.number = number }
            fun setOnConfirm(listener: (String, DialogCallback) -> Unit) =
                apply { this.onConfirm = listener }

            fun build() = DialogConfig(title, number, onConfirm)
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.hac_config_number_bottom_dialog
    }

    override fun onCreate() {
        super.onCreate()
        initBinding()
        initializeState()
        setupInputListeners()
    }

    private fun initBinding() {
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.stateVM = mStates
        binding.click = ClickProxy()
    }

    private fun initializeState() {
        mStates.updateInitialState(
            title = "设置${config.title}",
            number = config.number,
        )
    }

    private fun setupInputListeners() {
        // Prefix input filter
        val prefixFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val pattern = "^[A-Z]*$".toRegex()
            val newText = dest.subSequence(0, dstart).toString() +
                    source.subSequence(start, end) +
                    dest.subSequence(dend, dest.length)
            if (newText.isEmpty() || pattern.matches(newText)) null else ""
        }

        binding.etNumber.apply {
            filters = arrayOf(prefixFilter)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setSelection(text?.length ?: 0)
                }
            }
        }
    }

    inner class ClickProxy : BaseDialogClickProxy() {
        override fun onCloseClick() {
            dismiss()
        }

        override fun onConfirmClick() {
            val number = mStates.number.get()

            when {
                number.isEmpty() -> {
                    Toaster.show("请输入${config.title}")
                }

                else -> {
                    // 将关闭弹窗的控制权交给外部
                    config.onConfirm(number, dialogCallback)
                }
            }
        }
    }

    private val dialogCallback = object : DialogCallback {
        override fun dismiss() {
            this@HacConfigNumberBottomDialog.dismiss()
        }
    }

    // 新增回调接口
    interface DialogCallback {
        fun dismiss()
    }

    companion object {
        fun create(
            context: Context,
            block: DialogConfig.Builder.() -> Unit
        ): HacConfigNumberBottomDialog {
            val config = DialogConfig.Builder().apply(block).build()
            return HacConfigNumberBottomDialog(context, config)
        }
    }
}