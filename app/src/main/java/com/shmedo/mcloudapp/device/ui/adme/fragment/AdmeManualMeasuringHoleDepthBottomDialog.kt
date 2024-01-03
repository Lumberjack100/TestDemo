package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentAdmeManualMeasuringHoleDepthBottomDialogBinding
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeMeasuringHoleDepthViewModel

class AdmeManualMeasuringHoleDepthBottomDialog : BaseVmDbDialogFragment() {
    private val binding: FragmentAdmeManualMeasuringHoleDepthBottomDialogBinding by lazy { mDatabind as FragmentAdmeManualMeasuringHoleDepthBottomDialogBinding }
    private val mStates: AdmeMeasuringHoleDepthViewModel by viewModels({ requireParentFragment() })
    private val bleViewModel: BleViewModel by viewModels()


    override val dataBindingConfig: DataBindingConfig
        get() = DataBindingConfig(
            R.layout.fragment_adme_manual_measuring_hole_depth_bottom_dialog,
            BR.stateVM,
            mStates
        ).addBindingParam(
            BR.click,
            ClickProxy()
        )

    override fun setWindowStyle(gravity: Int) {
        super.setWindowStyle(Gravity.BOTTOM)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mStates.isExitButtonVisible.set(false)
        mStates.pauseButtonText.set("暂停")
        mStates.motionPulse.set("0")
        mStates.motionDistance.set("0")
    }

    inner class ClickProxy {
        fun onCloseClick() {
            if (!bleViewModel.isConnected() || mStates.isExitButtonVisible.get()) {
                dismiss()
                return
            }
            showMessage("确认退出数据运行吗?", "温馨提示", "确定", {
                fragmentClickListener?.onCloseClick()
            }, "取消")
        }

        fun onStopClick() {
            fragmentClickListener?.onStopClick()
        }

        fun onPauseClick() {
            fragmentClickListener?.onPauseClick()
        }

        fun onExitClick() {
            fragmentClickListener?.onExitClick()
        }
    }

    private var fragmentClickListener: OnDialogFragmentClickListener? = null
    fun setOnDialogFragmentClickListener(listener: OnDialogFragmentClickListener?) {
        fragmentClickListener = listener
    }

    interface OnDialogFragmentClickListener {
        fun onCloseClick()
        fun onStopClick()
        fun onPauseClick()
        fun onExitClick()
    }

    companion object {
        fun newInstance() = AdmeManualMeasuringHoleDepthBottomDialog()
    }
}