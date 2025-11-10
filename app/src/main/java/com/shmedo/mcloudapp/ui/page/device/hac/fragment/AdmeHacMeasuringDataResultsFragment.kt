package com.shmedo.mcloudapp.ui.page.device.hac.fragment

import android.os.Bundle
import android.view.View
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentAdmeHacMeasuringDataResultsBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher

class AdmeHacMeasuringDataResultsFragment : BaseFragment() {
    private lateinit var binding: FragmentAdmeHacMeasuringDataResultsBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: EmptyViewModel

    override fun initViewModel() {
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_hac_measuring_data_results,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeHacMeasuringDataResultsBinding
        binding.llToolbar.toolbar.title = "数据展示"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            //            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().popBackStack(R.id.admeHacMeasuringDataFragment, false)
        }
        registerOnBackPressedDispatcher {
            //            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().popBackStack(R.id.admeHacMeasuringDataFragment, false)
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
    }

    inner class ClickProxy : BaseClickProxy() {

        //重新测量
        fun onReMeasureClick() {
            nav().popBackStack(R.id.admeHacMeasuringDataFragment, false)
        }

        //保存数据
        fun onSaveDataClick() {
            nav().popBackStack(R.id.admeHacMeasuringDataFragment, true)
        }
    }


}