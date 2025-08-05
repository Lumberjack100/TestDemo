package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdmeAdvancedConfigurationBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.UnifiedDeviceModule
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc: ADME 高级配置页面
 *
 */
class AdmeAdvancedConfigurationFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeAdvancedConfigurationBinding
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
            addType<UnifiedDeviceModule>(R.layout.item_device_config_module)
            R.id.item.onClick {
                val unifiedModule = getModel<UnifiedDeviceModule>()
                processItemClick(unifiedModule.module)
            }
        }
    }

    override fun initData() {
        super.initData()
        initModuleList()
    }

    private fun processItemClick(functionModule: DeviceFunctionModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        if (functionModule.navId != 0) {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                functionModule.navId,
                bundle
            )
        }
    }

    private fun initModuleList() {
        val moduleList = arrayListOf<UnifiedDeviceModule>()
        moduleList.add(
            CommonModule(
                name = "计米轮",
                desc = "参数配置",
                resID = R.drawable.ic_basic_config,
                navId = R.id.action_admeAdvancedConfigurationFragment_to_admeMeterWheelFragment
            ).toUnified()
        )
        moduleList.add(
            CommonModule(
                name = "测斜仪",
                desc = "参数配置",
                resID = R.drawable.ic_basic_config,
                navId = R.id.action_admeAdvancedConfigurationFragment_to_admeInclinometerFragment
            ).toUnified()
        )
        moduleList.add(
            CommonModule(
                name = "执行机构",
                desc = "参数配置",
                resID = R.drawable.ic_basic_config,
                navId = if (productType == ProductType.ADME) R.id.action_admeAdvancedConfigurationFragment_to_admeExecutiveAgencyFragment else R.id.action_admeAdvancedConfigurationFragment_to_admeHacExecutiveAgencyFragment
            ).toUnified()
        )
        if (productType == ProductType.ADME) {
            moduleList.add(
                CommonModule(
                    name = "步进电机",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeStepperMotorFragment
                ).toUnified()
            )
        }

        moduleList.add(
            CommonModule(
                name = "堵转缓停",
                desc = "参数配置",
                resID = R.drawable.ic_basic_config,
                navId = R.id.action_admeAdvancedConfigurationFragment_to_admeLockedRotorDetectionFragment
            ).toUnified()
        )
        if (productType == ProductType.ADME && communicateWay is BleConnect) {
            moduleList.add(
                CommonModule(
                    name = "导槽校准",
                    desc = "正反测起点校准",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeGuideGrooveCalibrationFragment
                ).toUnified()
            )
        }
        moduleList.add(
            CommonModule(
                name = "智能控制",
                desc = "参数配置",
                resID = R.drawable.ic_basic_config,
                navId = R.id.action_admeAdvancedConfigurationFragment_to_admeIntelligentControlFragment
            ).toUnified()
        )
        moduleList.add(
            CommonModule(
                name = "阈值设置",
                desc = "参数配置",
                resID = R.drawable.ic_basic_config,
                navId = R.id.action_admeAdvancedConfigurationFragment_to_admeThresholdFragment
            ).toUnified()
        )
        if (productType == ProductType.ADME)
            moduleList.add(
                CommonModule(
                    name = "运动检校处理",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeSportsCalibrationProcessingFragment
                ).toUnified()
            )

        if (productType == ProductType.ADME_HAC)
            moduleList.add(
                CommonModule(
                    name = "报警配置",
                    desc = "参数配置",
                    resID = R.drawable.ic_basic_config,
                    navId = R.id.action_admeAdvancedConfigurationFragment_to_admeHacAlarmSettingFragment
                ).toUnified()
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