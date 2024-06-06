package com.shmedo.mcloudapp.device.ui.hac.fragment

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeHacMeasuringHoleDepthViewModel

class AdmeHacManualMeasuringHoleDepthBottomDialog : BaseVmDbDialogFragment() {
    private val mStates: AdmeHacMeasuringHoleDepthViewModel by viewModels({ requireParentFragment() })

    override val dataBindingConfig: DataBindingConfig
        get() = DataBindingConfig(
            R.layout.fragment_adme_hac_manual_measuring_hole_depth_bottom_dialog,
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
        mStates.motorInfo.set("正常")
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
        if(!isFirst)
            fragmentClickListener?.onRefresh()
    }

    companion object {
        fun newInstance() = AdmeHacManualMeasuringHoleDepthBottomDialog()
    }
}