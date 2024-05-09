package com.shmedo.mcloudapp.device.ui.hac.fragment

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.fragment.BaseVmDbDialogFragment
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeHacMeasuringHoleDepthViewModel
import com.shmedo.mcloudapp.ext.showMessage

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
class AdmeHacAutoMeasuringHoleDepthBottomDialog : BaseVmDbDialogFragment() {
    private val mStates: AdmeHacMeasuringHoleDepthViewModel by viewModels({ requireParentFragment() })
    private val bleViewModel: BleViewModel by viewModels()


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
        mStates.isExitButtonVisible.set(false)
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
        fun newInstance() = AdmeHacAutoMeasuringHoleDepthBottomDialog()
    }
}