package com.shmedo.mcloudapp.device.ui.m20.fragment

import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DataCenterModule
import com.shmedo.mcloudapp.device.model.DeviceFunctionModule
import com.shmedo.mcloudapp.device.model.RebootModule
import com.shmedo.mcloudapp.device.model.RunningStatusModule
import com.shmedo.mcloudapp.device.model.SetupWizard
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDataCenterHomeFragment
import com.shmedo.mcloudapp.device.ui.common.UniversalDeviceHomeFragment
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   配置主页
 */
class M20HomeFragment : UniversalDeviceHomeFragment() {
    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    "关于设备",
                    "设备基本信息、运行数据",
                    R.drawable.ic_device_running_info,
                    navId = R.id.action_m20HomeFragment_to_m20DeviceInfoFragment
                )
            )
        )
        moduleList.add(ConfigModule(SetupWizard(name = "水平初始化")))
        moduleList.add(ConfigModule(DataCenterModule(navId = R.id.action_global_universalDataCenterHomeFragment)))

        moduleList.add(
            ConfigModule(RebootModule())
        )
        moduleList.add(ConfigModule(AdvancedSettingsModule(navId = R.id.action_m20HomeFragment_to_m20AdvancedSettingFragment)))

        binding.rvModule.models = moduleList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is SetupWizard -> {
                if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return
                }
                showMessage("确定进行水平初始化吗？", "温馨提示", "确定", {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(IOTCommandType.M20_MD_LEVEL_INITIAL)
                    commandItems.add(command)
                    showLoadingDialog(StringUtils.getString(R.string.processing))
                    sendCommandFromCmdList(isStartTimeoutJob = true)
                }, "取消")
            }

            is DataCenterModule -> {
                val bundle = UniversalDataCenterHomeFragment.newBundleArguments(
                    4,
                    ProductType.M20,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(
                    configModule.navId,
                    bundle
                )
            }

            else -> {
                if (configModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
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
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "水平初始化失败:" + result.message
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("初始化完成")
                        }
                    }
                }
            }

            else -> {

            }
        }
    }
}