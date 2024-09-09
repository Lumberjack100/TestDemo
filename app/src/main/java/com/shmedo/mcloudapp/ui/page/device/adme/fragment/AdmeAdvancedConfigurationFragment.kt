package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentAdmeAdvancedConfigurationBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc: ADME 高级配置页面
 *
 */
class AdmeAdvancedConfigurationFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeAdvancedConfigurationBinding
    private lateinit var toolbarViewModel: ToolbarViewModel


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_advanced_configuration,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeAdvancedConfigurationBinding
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
        // 获取具体的尺寸值，以像素（px）为单位
        //val dimenSizeInPx = Utils.getApp().resources.getDimension(R.dimen.device_home_module_item_space)
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
        }
    }

    override fun initData() {
        super.initData()
        initModuleList()
    }

    private fun processItemClick(module: ConfigModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        if (module.functionModule.navId != 0) {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(
                module.functionModule.navId,
                bundle
            )
        }
    }

    private fun initModuleList() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "计米轮",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeMeterWheelFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "测斜仪",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeInclinometerFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "执行机构",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = if (productType == ProductType.ADME) R.id.action_admeAdvancedConfigurationFragment_to_admeExecutiveAgencyFragment else R.id.action_admeAdvancedConfigurationFragment_to_admeHacExecutiveAgencyFragment
                )
            )
        )
        if (productType == ProductType.ADME) {
            moduleList.add(
                ConfigModule(
                    CommonModule(
                        name = "步进电机",
                        desc = "参数配置",
                        resID = R.drawable.ic_basic_config,
                        navId = R.id.action_admeAdvancedConfigurationFragment_to_admeStepperMotorFragment
                    )
                )
            )
        }

        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "堵转缓停",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeLockedRotorDetectionFragment
                )
            )
        )
        if (productType == ProductType.ADME && communicateWay is BleConnect) {
            moduleList.add(
                ConfigModule(
                    CommonModule(
                        name = "导槽校准",
                        desc = "正反测起点校准",
                        resID = R.drawable.ic_basic_config,
                        navId = R.id.action_admeAdvancedConfigurationFragment_to_admeGuideGrooveCalibrationFragment
                    )
                )
            )
        }
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "智能控制",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeIntelligentControlFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "阈值设置",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeThresholdFragment
                )
            )
        )
        if (productType == ProductType.ADME)
            moduleList.add(
                ConfigModule(
                    CommonModule(
                        name = "运动检校处理",
                        desc = "参数配置",
                        resID = R.drawable.ic_basic_config,
                        navId = R.id.action_admeAdvancedConfigurationFragment_to_admeSportsCalibrationProcessingFragment
                    )
                )
            )

        if (productType == ProductType.ADME_HAC)
            moduleList.add(
                ConfigModule(
                    CommonModule(
                        name = "报警设置",
                        desc = "参数配置",
                        resID = R.drawable.ic_basic_config,
                        navId = R.id.action_admeAdvancedConfigurationFragment_to_admeHacAlarmSettingFragment
                    )
                )
            )
        binding.rvModule.models = moduleList
    }

    override fun setResultData(cmdStr: String) {

    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}