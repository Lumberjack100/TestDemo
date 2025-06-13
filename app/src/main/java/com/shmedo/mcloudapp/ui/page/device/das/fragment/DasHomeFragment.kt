package com.shmedo.mcloudapp.ui.page.device.das.fragment

import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.CollectorConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.TelemetryDataModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDeviceHomeFragment

class DasHomeFragment : UniversalDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        mHeadStates.productNormalResId.set(R.drawable.device_logo_qingxieyi)
        mHeadStates.productOfflineResId.set(R.drawable.device_logo_qingxieyi_gray)
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    name = "关于设备",
                    desc = "设备基本信息、运行数据",
                    R.drawable.ic_device_running_info,
                    navId = R.id.action_global_to_dasStatusInfoFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(TimeCalibrationModule())
        )
        moduleList.add(
            ConfigModule(TelemetryDataModule())
        )
        moduleList.add(
            ConfigModule(RebootModule())
        )
        moduleList.add(
            ConfigModule(CollectorConfigModule(navId = R.id.action_global_to_dasNetInfoFragment))
        )
        moduleList.add(
            ConfigModule(DataCenterModule(navId = R.id.action_global_to_dasDataCenterHomeFragment))
        )
        moduleList.add(
            ConfigModule(SensorConfigModule(navId = R.id.action_global_to_dasSensorHomeFragment))
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "上报方式",
                    desc = "上报规则设置",
                    resID = R.drawable.ic_device_data_center,
                    navId = R.id.action_global_to_dasTerminalParameterFragment
                )
            )
        )

        binding.rvModule.models = moduleList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {

            else -> {
                if (configModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        configModule.navId,
                        bundle
                    )
                }
            }
        }
    }

    override fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {
        when (commandType) {
            else -> {

            }
        }
    }
}