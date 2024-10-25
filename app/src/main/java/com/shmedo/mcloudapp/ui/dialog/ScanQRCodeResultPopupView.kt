package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.BottomPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ScanQrCodeResultPopupViewBinding
import com.shmedo.mcloudapp.ui.viewmodel.state.DeviceManageHomeViewModel

class ScanQRCodeResultPopupView (context: Context) : BottomPopupView(context) {
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

    interface OnClickListener {
        fun onBleConnectClick()

        fun on4gConnectClick()
    }
}