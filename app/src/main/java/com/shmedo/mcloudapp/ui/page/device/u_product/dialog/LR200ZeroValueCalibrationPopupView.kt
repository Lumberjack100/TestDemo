package com.shmedo.mcloudapp.ui.page.device.u_product.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.CustomLr200ZeroValueCalibrationPopupBinding
import com.shmedo.mcloudapp.ui.viewmodel.state.LR200ZeroValueCalibrationViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/6
 *
 * 描述：LR200 一体式裂缝计零值校准
 */
class LR200ZeroValueCalibrationPopupView(context: Context) : CenterPopupView(context) {
    private lateinit var binding: CustomLr200ZeroValueCalibrationPopupBinding
    private lateinit var stateVM: LR200ZeroValueCalibrationViewModel

    private var title: String = ""
    private lateinit var clickListener: OnClickListener


    fun setTitle(
        title: String = "",
        vm: LR200ZeroValueCalibrationViewModel
    ): LR200ZeroValueCalibrationPopupView {
        this.title = title
        this.stateVM = vm
        return this
    }

    fun setClickListener(clickListener: OnClickListener): LR200ZeroValueCalibrationPopupView {
        this.clickListener = clickListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_lr200_zero_value_calibration_popup
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
            if (!stateVM.isCalibratingSuccess.get()) {
                clickListener.onSettingClick()
            }
        }
    }

    interface OnClickListener {
        fun onSettingClick()
    }
}