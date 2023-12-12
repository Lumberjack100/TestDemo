package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.Utils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.util.AppContants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentQueryDeviceDataBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.viewmodel.state.QueryDeviceDataViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel

class QueryDeviceDataFragment : BaseFragment() {
    private val binding: FragmentQueryDeviceDataBinding by lazy { getBinding() as FragmentQueryDeviceDataBinding }
    private val mHeadStates: QueryDeviceDataViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val modeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_device_mode) }

    private var statusBarColor = 0
    private var keyWord: String = ""

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_query_device_data, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "数据查询"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }

    }

    override fun initData() {
        arguments?.let {
            keyWord = it.getString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, "")
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        mHeadStates.sn.set(keyWord)
    }

    override fun createObserver() {

    }

    override fun lazyLoadData() {

    }

    inner class ClickProxy : BaseClickProxy() {
        fun onStartTimeClick() {

        }

        fun onEndTimeClick() {

        }

        fun onChooseItemCountClick() {

        }

        fun onSearchClick() {

        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun newBundleArguments(
            keyWord: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, keyWord)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}