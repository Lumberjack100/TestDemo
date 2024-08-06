package com.shmedo.mcloudapp.ui.page.device.hac.dialog

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeHacMeasuringHoleDepthViewModel

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
class AdmeHacAutoMeasuringHoleDepthBottomDialog : BaseVmDbDialogFragment() {
    private val mStates: AdmeHacMeasuringHoleDepthViewModel by viewModels({ requireParentFragment() })


    override val dataBindingConfig: DataBindingConfig
        get() = DataBindingConfig(
            R.layout.fragment_adme_hac_auto_measuring_hole_depth_bottom_dialog,
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
        mStates.isStopQueryMotorState.set(false)
        mStates.isExitButtonVisible.set(false)
        mStates.motorInfo.set("正常")
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
        if(!isFirst)
            fragmentClickListener?.onRefresh()
    }

    companion object {
        fun newInstance() = AdmeHacAutoMeasuringHoleDepthBottomDialog()
    }
}