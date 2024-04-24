package com.shmedo.mcloudapp.device.ui.gw100.fragment

import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DeviceFunctionModule
import com.shmedo.mcloudapp.device.model.LoraConfigModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RestoreFactoryModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.LoraSettingFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDeviceHomeFragment
import com.shmedo.mcloudapp.ext.nav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/23
 * 描述： TODO
 */
class GW100HomeFragment : UniversalDeviceHomeFragment() {
    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(false)
        mHeadStates.productLightResId.set(R.drawable.ic_device_logo_def)
        mHeadStates.productGrayResId.set(R.drawable.ic_device_logo_def)
        mHeadStates.isPlatformConnectionStateVisible.set(false)
    }

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    navId = R.id.action_gW100HomeFragment_to_gW100BaseInfoFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(TimeCalibrationModule())
        )
        moduleList.add(
            ConfigModule(
                CommonModule(
                    name = "网关设置",
                    desc = "GNSS电台网关设置",
                    resID = R.drawable.ic_device_data_center,
                    navId = R.id.action_gW100HomeFragment_to_gW100GatewaySettingsFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(
                LoraConfigModule(
                    navId = R.id.action_global_to_loraSettingFragment
                )
            )
        )
        moduleList.add(
            ConfigModule(RebootModule())
        )
        moduleList.add(
            ConfigModule(RestoreFactoryModule())
        )
        binding.rvModule.models = moduleList
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        //蓝牙模式下，等蓝牙建立连接后查询设备基本信息
        queryBaseInfo()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS
        )
        commandItems.add(command)
        sendCommandFromCmdList()
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is LoraConfigModule -> {//LORA设置
                val bundle = LoraSettingFragment.newBundleArguments(
                    ProductType.COLLECTOR_G_0,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(
                    R.id.action_global_to_loraSettingFragment,
                    bundle
                )
            }

            else -> {
                if (configModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        configModule.navId,
                        bundle
                    )
                }
            }
        }
    }

    override fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {
        when (commandType) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询基本信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        val content: String = result.data
                        initStatusInfo(content)
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content)
                } ?: return@launchWithViewLifecycle

                if (commonCurrentStateInfoList.isEmpty()) {
                    Toaster.show("数据为空")
                    return@launchWithViewLifecycle
                }
                mHeadStates.firmwareVersion.set(commonCurrentStateInfoList[0].firmwareVersion)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}