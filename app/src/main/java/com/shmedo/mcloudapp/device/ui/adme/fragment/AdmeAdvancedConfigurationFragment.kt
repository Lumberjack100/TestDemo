package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentAdmeAdvancedConfigurationBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc: ADME 高级配置页面
 *
 */
class AdmeAdvancedConfigurationFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentAdmeAdvancedConfigurationBinding by lazy { getBinding() as FragmentAdmeAdvancedConfigurationBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_advanced_configuration,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "高级配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initModuleAdapter()
    }

    private fun initModuleAdapter() {
        binding.rvModule.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(15f),
                    false
                )
            )
            addType<ConfigModule>(R.layout.item_device_config_module)
            R.id.item.onClick {
                val module = getModel<ConfigModule>()
                processItemClick(module)
            }
        }.models = getModuleList()
    }

    private fun processItemClick(module: ConfigModule) {
        if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        if (module.configModule.navId != 0) {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(
                module.configModule.navId,
                bundle
            )
        }
    }

    override fun setResultData(cmdStr: String) {

    }

    private fun getModuleList() =
        arrayListOf<ConfigModule>(
            ConfigModule(
                CommonModule(
                    name = "计米轮",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeMeterWheelFragment
                )
            ),
            ConfigModule(
                CommonModule(
                    name = "测斜仪",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeInclinometerFragment
                )
            ),
            ConfigModule(
                CommonModule(
                    name = "执行机构",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeInclinometerFragment
                )
            ),
            ConfigModule(
                CommonModule(
                    name = "步进电机",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeStepperMotorFragment
                )
            ),
            ConfigModule(
                CommonModule(
                    name = "堵转缓停",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeLockedRotorDetectionFragment
                )
            ),
            ConfigModule(
                CommonModule(
                    name = "智能控制",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeInclinometerFragment
                )
            ),
            ConfigModule(
                CommonModule(
                    name = "阈值设置",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeInclinometerFragment
                )
            ),
        )

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}