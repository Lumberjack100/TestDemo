package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDeviceReplacementBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.DeviceReplacementViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author：gonghe
 * @time: 2025/8/11
 * @desc: 设备更换
 *
 */
class DeviceReplacementFragment : BaseFragment() {
    private lateinit var binding: FragmentDeviceReplacementBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: DeviceReplacementViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    private lateinit var deviceInfo: DeviceInfo

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_device_replacement,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceReplacementBinding
        binding.llToolbar.toolbar.title = "设备更换"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    override fun initData() {
        // 从参数中获取设备信息
        arguments?.let {
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
        }
    }


    override fun lazyLoadData() {

    }

    inner class ClickProxy : BaseClickProxy() {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar, isKeyboardEnable = true)
    }

    companion object {
        /**
         * 创建Bundle参数 (兼容原有接口)
         */
        fun newBundleArguments(
            deviceInfo: DeviceInfo,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}