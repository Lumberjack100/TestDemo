package com.shmedo.mcloudapp.ui.page.device.adme.dialog

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeMeasuringHoleDepthViewModel

class AdmeAutoMeasuringHoleDepthBottomDialog : BaseVmDbDialogFragment() {
    private val mStates: AdmeMeasuringHoleDepthViewModel by viewModels({ requireParentFragment() })

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

    inner class ClickProxy {
        fun onCloseClick() {
            fragmentClickListener?.onCloseClick()
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
        fun onRefresh()
        fun onStopClick()
        fun onExitClick()
    }

    override fun onResume() {
        super.onResume()
        if (!isFirst)
            fragmentClickListener?.onRefresh()
    }

    companion object {
        fun newInstance() = AdmeAutoMeasuringHoleDepthBottomDialog()
    }
}