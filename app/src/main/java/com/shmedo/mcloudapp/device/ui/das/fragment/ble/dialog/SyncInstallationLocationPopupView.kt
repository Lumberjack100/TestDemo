package com.shmedo.mcloudapp.device.ui.das.fragment.ble.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.SyncInstallationLocationPopupViewBinding
import com.shmedo.mcloudapp.device.viewmodel.state.AdvancedSettingViewModel

class SyncInstallationLocationPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: SyncInstallationLocationPopupViewBinding
    private lateinit var stateVM: AdvancedSettingViewModel

    private var title: String = ""
    private var clickListener: OnClickListener? = null


    fun setTitle(
        title: String = "",
        vm: AdvancedSettingViewModel
    ): SyncInstallationLocationPopupView {
        this.title = title
        this.stateVM = vm
        return this
    }

    fun setClickListener(clickListener: OnClickListener): SyncInstallationLocationPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.sync_installation_location_popup_view
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
        binding.ivLocate.setOnClickListener {
            clickListener?.onRefreshingLocationClick()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            clickListener?.onConfirmClick()
            dismiss()
        }
    }

    interface OnClickListener {
        fun onRefreshingLocationClick()
        fun onConfirmClick()
    }
}