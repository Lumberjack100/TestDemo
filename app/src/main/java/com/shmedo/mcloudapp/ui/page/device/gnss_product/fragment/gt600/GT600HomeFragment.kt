package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.ItemM50MeasureDataBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.M50MeasureDataItem
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.CommonSensorDataHistoryFragment

/**
 * 创建者：gonghe
 * 创建时间：2025/11/17
 * 描述： TODO
 */
class GT600HomeFragment : BaseDeviceHomeFragment() {
    private var measureDataItem: M50MeasureDataItem = M50MeasureDataItem()

    private val sensorTypeList = arrayListOf("215")


    override fun initData() {
        super.initData()
        mHeadStates.productErrorResId.set(R.drawable.device_logo_gt600_error)
        mHeadStates.productAlarmResId.set(R.drawable.device_logo_gt600_alarm)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_gt600_offline)
        mHeadStates.productNormalResId.set(R.drawable.device_logo_gt600)
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 扩展适配器支持 M50MeasureDataItem
        binding.rvModule.bindingAdapter.addType<M50MeasureDataItem>(R.layout.item_m50_measure_data)
    }

    override fun BindingViewHolder.processOtherItemViewBind(itemViewType: Int) {
        if (itemViewType == R.layout.item_m50_measure_data) {
            val binding = getBinding<ItemM50MeasureDataBinding>()

            // 设置数据绑定参数
            binding.setVariable(BR.m, measureDataItem)
            binding.setVariable(BR.click, ClickProxy())
            binding.executePendingBindings()
        }
    }

    override fun initModuleData() {
        val groupList = mutableListOf<Any>()

        // 添加测量数据作为第一个项目
        groupList.add(measureDataItem)
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))

        // 设备信息模块
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    CommonModule(
                        name = "基本信息",
                        resID = R.drawable.ic_module_basic_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_gt600BaseInfoFragment,
                    ).toUnified(),

                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_gt600NetInfoFragment,
                    ).toUnified(),

                    CommonModule(
                        name = "状态信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_gt600StatusInfoFragment,
                    ).toUnified(),

                    CommonModule(
                        name = "姿态信息",
                        resID = R.drawable.ic_module_satellite_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_gt600LocationInfoFragment,
                    ).toUnified()
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("快捷配置"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    CommonModule(
                        name = "一键配置",
                        resID = R.drawable.ic_module_cmd_debug_new,
                        navId = R.id.action_global_to_quickConfigCommandParam,
                    ).toUnified(),
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()

        configModuleTree.configModules.add(
            CommonModule(
                name = "工作模式",
                resID = R.drawable.ic_module_work_mode_new,
                navId = R.id.action_global_to_m20SWorkModelParamFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "网络配置",
                resID = R.drawable.ic_module_network_setting,
                navId = R.id.action_global_to_m50NetworkConfigFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            DataCenterModule(
                name = "链路配置",
                resID = R.drawable.ic_module_datacenter_new,
                navId = R.id.action_global_to_dataCenterHomeFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "GNSS配置",
                resID = R.drawable.ic_module_cors,
                navId = R.id.action_global_to_m50GNSSConfigFragment,
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "串口配置",
                resID = R.drawable.ic_module_serial_port,
                navId = 0,
                isSupport = false
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "时间校准",
                resID = R.drawable.ic_module_time_calibration_new,
                navId = R.id.action_global_to_time_calibration
            ).toUnified()
        )
        configModuleTree.configModules.add(
            CommonModule(
                name = "系统配置",
                resID = R.drawable.ic_module_system_setting,
                navId = R.id.action_global_to_advancedSettingFragment
            ).toUnified()
        )

        groupList.add(configModuleTree)
        binding.rvModule.models = groupList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is DataCenterModule -> {
                nav().safeNavigate(
                    configModule.navId,
                    BaseDataCenterHomeFragment.newBundleArguments(
                        centerNum = 4,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                )
            }

            is CommonModule -> {
                if (configModule.navId == R.id.action_global_to_gt600LocationInfoFragment) {
                    // 姿态监测页面需要传递 deviceToken
                    nav().safeNavigate(
                        configModule.navId,
                        Gt600LocationInfoFragment.newBundleArguments(deviceInfo.deviceToken)
                    )
                } else {
                    super.processOtherItemClick(configModule)
                }
            }

            else -> {
                super.processOtherItemClick(configModule)
            }
        }
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        initLastHistorySensorData()
    }

    /**
     * 初始化历史传感器数据
     */
    private fun initLastHistorySensorData() {
        if (NetworkUtils.isConnected()) {
            launchWithViewLifecycle {
                val resultMap: Map<String, String> =
                    deviceRequestViewModel.queryLatestSensorData(
                        deviceInfo.deviceToken,
                        iotSensorTypeList = sensorTypeList
                    )
                if (resultMap.isEmpty())
                    return@launchWithViewLifecycle

                updateMeasureDataFromMap(resultMap)
            }
        }
    }

    /**
     * 从Map更新测量数据显示
     */
    private fun updateMeasureDataFromMap(resultMap: Map<String, String>) {
        val xDisplacement = resultMap["x"]?.takeUnless { it.isBlank() }?.let { "$it mm" }
            ?: AppContants.PLACE_HOLDER_VALUE
        val yDisplacement = resultMap["y"]?.takeUnless { it.isBlank() }?.let { "$it mm" }
            ?: AppContants.PLACE_HOLDER_VALUE
        val zDisplacement = resultMap["z"]?.takeUnless { it.isBlank() }?.let { "$it mm" }
            ?: AppContants.PLACE_HOLDER_VALUE
        val measurementTime = resultMap["time"]?.replace(".000", "")?.replace("-", ".")
            ?: AppContants.PLACE_HOLDER_VALUE

        measureDataItem.refreshStatusWithTime(
            xDisplacement,
            yDisplacement,
            zDisplacement,
            measurementTime
        )
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 跳转到位置信息页面
         */
        override fun onGotoLocationClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            nav().safeNavigate(
                R.id.action_global_to_commonLocationInfoFragment,
                BaseIOTDeviceFragment.newBundleArguments(
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
            )
        }

        override fun onTakePhotoClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            // TODO: 实现拍照功能
        }

        override fun onGoToSensorDataHistoryClick() {
            nav().safeNavigate(
                R.id.action_global_to_commonSensorDataHistoryFragment,
                CommonSensorDataHistoryFragment.newBundleArguments(
                    productType,
                    deviceInfo
                )
            )
        }
    }
}