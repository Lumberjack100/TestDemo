package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentMr702InterfaceStatusInfoBinding
import com.shmedo.mcloudapp.device.viewmodel.state.MR702InterfaceStatusInfoViewModel

class MR702InterfaceStatusInfoFragment : BaseFragment() {
    private val binding: FragmentMr702InterfaceStatusInfoBinding by lazy { getBinding() as FragmentMr702InterfaceStatusInfoBinding }
    private val mStates: MR702InterfaceStatusInfoViewModel by viewModels()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_interface_status_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun createObserver() {

    }


    companion object {
        fun newInstance() = MR702InterfaceStatusInfoFragment()
    }
}