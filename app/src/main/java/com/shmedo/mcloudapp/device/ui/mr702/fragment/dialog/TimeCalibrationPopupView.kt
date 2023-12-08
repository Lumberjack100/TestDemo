package com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomTimeCalibrationPopupBinding
import com.shmedo.mcloudapp.device.viewmodel.state.CommandResponseViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/6
 *
 * 描述： 时间校准
 */
class TimeCalibrationPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomTimeCalibrationPopupBinding
    private lateinit var stateVM: CommandResponseViewModel

    private var title: String = ""
    private lateinit var clickListener: OnClickListener


    fun setTitle(
        title: String = "",
        vm: CommandResponseViewModel
    ): TimeCalibrationPopupView {
        this.title = title
        this.stateVM = vm
        return this
    }

    fun setClickListener(clickListener: OnClickListener): TimeCalibrationPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_time_calibration_popup
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
        binding.tvCalibration.setOnClickListener {
            clickListener.onSettingClick()
        }
    }

    interface OnClickListener {
        fun onSettingClick()
    }
}