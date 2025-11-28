package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.BottomPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ScanQrCodeResultPopupViewBinding
import com.shmedo.mcloudapp.ui.viewmodel.state.DeviceManageHomeViewModel

/**
 * 扫码结果底部弹窗
 * 用于选择设备连接方式（蓝牙连接 / 4G连接）
 */
class ScanQRCodeResultPopupView(context: Context) : BottomPopupView(context) {
    private lateinit var binding: ScanQrCodeResultPopupViewBinding
    private lateinit var stateVM: DeviceManageHomeViewModel

    private var title: String = ""
    private var clickListener: OnClickListener? = null

    fun setTitle(
        title: String = "",
        vm: DeviceManageHomeViewModel
    ): ScanQRCodeResultPopupView {
        this.title = title
        this.stateVM = vm
        return this
    }

    fun setClickListener(clickListener: OnClickListener): ScanQRCodeResultPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.scan_qr_code_result_popup_view
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.stateVM = stateVM

        // 处理 Edge-to-Edge 模式下系统导航栏的遮挡问题
        applySystemBarSafeArea()

        if (title.isNotEmpty())
            binding.popupHead.tvTitle.text = title

        binding.popupHead.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvBleConnect.setOnClickListener {
            clickListener?.onBleConnectClick()
            dismiss()
        }

        binding.tv4gConnect.setOnClickListener {
            clickListener?.on4gConnectClick()
            dismiss()
        }
    }

    /**
     * 让弹窗整体与内部内容同时避开系统导航栏
     */
    private fun applySystemBarSafeArea() {
        // 外层 View（由 XPopup 管理），用于整体抬升
        val popupContainer = this
        // 内部内容视图（有背景和圆角），用于额外 padding
        val contentView = binding.root

        val initialContentPaddingBottom = contentView.paddingBottom
        val initialPopupBottomMargin =
            (popupContainer.layoutParams as? MarginLayoutParams)?.bottomMargin ?: 0

        fun applyInsets(insets: WindowInsetsCompat) {
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 1. 整体抬升，整个 BottomPopupView 通过动态 bottomMargin 抬离系统导航栏，保持圆角背景完整。
            popupContainer.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = initialPopupBottomMargin + systemBars.bottom
            }
            // 2. 内部内容额外增加 bottom padding，让按钮区与导航栏保持安全距离，手势导航时高度为 0，不会额外抬升
            contentView.updatePadding(
                bottom = initialContentPaddingBottom + systemBars.bottom
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(popupContainer) { _, insets ->
            applyInsets(insets)
            insets
        }

        //doOnAttach + requestApplyInsets 确保在弹窗展示瞬间就能拿到最新 insets
        popupContainer.doOnAttach {
            ViewCompat.getRootWindowInsets(it)?.let(::applyInsets)
            ViewCompat.requestApplyInsets(it)
        }
    }

    interface OnClickListener {
        fun onBleConnectClick()

        fun on4gConnectClick()
    }
}