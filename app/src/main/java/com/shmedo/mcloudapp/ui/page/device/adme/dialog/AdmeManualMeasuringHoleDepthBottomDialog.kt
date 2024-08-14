package com.shmedo.mcloudapp.ui.page.device.adme.dialog

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeMeasuringHoleDepthViewModel

class AdmeManualMeasuringHoleDepthBottomDialog : BaseVmDbDialogFragment() {
    private val mStates: AdmeMeasuringHoleDepthViewModel by viewModels({ requireParentFragment() })


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
        mStates.isDoManualStopAction.set(false)
        mStates.pauseButtonText.set("暂停")
        mStates.motionPulse.set("0")
        mStates.motionDistance.set("0")
    }

    inner class ClickProxy {
        fun onCloseClick() {
            fragmentClickListener?.onCloseClick()
        }

        fun onStopClick() {
            mStates.isDoManualStopAction.set(true)
            fragmentClickListener?.onStopClick()
        }

        fun onPauseClick() {
            mStates.isDoManualStopAction.set(false)
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
        fun onRefresh()
        fun onStopClick()
        fun onPauseClick()
        fun onExitClick()
    }

    override fun onResume() {
        super.onResume()
        if (!isFirst)
            fragmentClickListener?.onRefresh()
    }

    companion object {
        fun newInstance() = AdmeManualMeasuringHoleDepthBottomDialog()
    }
}