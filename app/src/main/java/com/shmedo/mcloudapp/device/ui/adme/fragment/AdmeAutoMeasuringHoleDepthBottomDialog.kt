package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentAdmeAutoMeasuringHoleDepthBottomDialogBinding
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeMeasuringHoleDepthViewModel
import kotlinx.coroutines.Job

class AdmeAutoMeasuringHoleDepthBottomDialog : BaseVmDbDialogFragment() {
    private val binding: FragmentAdmeAutoMeasuringHoleDepthBottomDialogBinding by lazy { mDatabind as FragmentAdmeAutoMeasuringHoleDepthBottomDialogBinding }
    private val mStates: AdmeMeasuringHoleDepthViewModel by viewModels({ requireParentFragment() })
    private val bleViewModel: BleViewModel by viewModels()

    private var queryJob: Job? = null

    override val dataBindingConfig: DataBindingConfig
        get() = DataBindingConfig(
            R.layout.fragment_adme_auto_measuring_hole_depth_bottom_dialog,
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
        mStates.motionPulse.set("0")
        mStates.motionDistance.set("0")
    }

//    override fun onResume() {
//        super.onResume()
//        startQueryMotorMotionData()
//    }

    private fun startQueryMotorMotionData() {
        queryJob = launchAndRepeatWithViewLifecycle {


        }
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
        fun onExitClick()
    }

    companion object {
        fun newInstance() = AdmeAutoMeasuringHoleDepthBottomDialog()
    }
}