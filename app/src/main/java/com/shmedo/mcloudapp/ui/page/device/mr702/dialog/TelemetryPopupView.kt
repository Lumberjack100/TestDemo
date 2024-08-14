package com.shmedo.mcloudapp.ui.page.device.mr702.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomTelemetryPopupBinding
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandResponseViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/6
 *
 * 描述： 遥测
 *
 *
 */
class TelemetryPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomTelemetryPopupBinding
    private lateinit var stateVM: CommandResponseViewModel
    private var title: String = ""

    fun setTitle(
        title: String = "",
        vm: CommandResponseViewModel
    ): TelemetryPopupView {
        this.title = title
        this.stateVM = vm
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_telemetry_popup
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
        binding.popupFooter.tvOk.setOnClickListener {
            dismiss()
        }
    }
}