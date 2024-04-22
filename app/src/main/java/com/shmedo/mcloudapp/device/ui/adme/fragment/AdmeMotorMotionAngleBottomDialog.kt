package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeGuideGrooveCalibrationViewModel

class AdmeMotorMotionAngleBottomDialog : BaseVmDbDialogFragment() {
    private val mStates: AdmeGuideGrooveCalibrationViewModel by viewModels({ requireParentFragment() })
    private val bleViewModel: BleViewModel by viewModels()

    override val dataBindingConfig: DataBindingConfig
        get() = DataBindingConfig(
            R.layout.fragment_adme_motor_motion_angle_bottom_dialog,
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
        mStates.isStopAction.set(false)
        mStates.pauseButtonText.set("暂停")
        mStates.motionPulse.set("0")
        mStates.motionAngle.set("0")
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
            mStates.isStopAction.set(true)
            fragmentClickListener?.onStopClick()
        }

        fun onPauseClick() {
            mStates.isStopAction.set(false)
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
        fun newInstance() = AdmeMotorMotionAngleBottomDialog()
    }
}