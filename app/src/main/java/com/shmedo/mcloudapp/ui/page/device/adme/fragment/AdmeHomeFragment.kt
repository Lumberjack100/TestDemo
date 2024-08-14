package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.models
import com.lxj.xpopup.XPopup
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme.AdmeEquipModelEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeMotionState
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.AdvancedSettingsModule
import com.shmedo.mcloudapp.model.BasicConfigModule
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceOperationModule
import com.shmedo.mcloudapp.model.RebootModule
import com.shmedo.mcloudapp.model.RunningStatusModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDeviceHomeFragment
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import kotlinx.coroutines.delay

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc: ADME 主页面
 *
 */
class AdmeHomeFragment : UniversalDeviceHomeFragment() {
    private val modeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_device_mode) }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        //mHeadStates.productLightResId.set(R.drawable.device_logo_gateway)
        //mHeadStates.productGrayResId.set(R.drawable.device_logo_gateway_gray)
        mHeadStates.isRunningStateVisible.set(true)
        mHeadStates.isAdmeModeChooseViewVisible.set(true)
    }

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                RunningStatusModule(
                    resID = R.drawable.ic_module_current_state,
                    navId = R.id.action_admeHomeFragment_to_admeCurrentStateFragment
                )
            )
        )
        when (mHeadStates.admeModeText.get()) {
            modeList[0] -> {//设备配置模式
                moduleList.add(
                    ConfigModule(BasicConfigModule(navId = R.id.action_admeHomeFragment_to_admeBasicParamConfigFragment))
                )
                if (communicateWay is BleConnect) {
                    moduleList.add(
                        ConfigModule(
                            CommonModule(
                                name = "孔深测量",
                                desc = "测量测斜管深度",
                                resID = R.drawable.ic_measuring_hole_depth,
                                navId = R.id.action_global_to_admeMeasuringHoleDepthFragment
                            )
                        )
                    )
                    moduleList.add(
                        ConfigModule(
                            CommonModule(
                                name = "指令下发",
                                desc = "自定义指令下发",
                                resID = R.drawable.ic_device_instruction_send,
                                navId = 0
                            )
                        )
                    )
                }
                moduleList.add(
                    ConfigModule(
                        DeviceOperationModule(
                            name = "高级配置",
                            desc = "计米轮、测斜议、执行机构等",
                            navId = R.id.action_global_to_admeAdvancedConfigurationFragment
                        )
                    )
                )
                moduleList.add(
                    ConfigModule(RebootModule())
                )
                moduleList.add(
                    ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_admeAdvancedSettingFragment))
                )
            }

            modeList[1] -> {//自动监测模式
                moduleList.add(
                    ConfigModule(BasicConfigModule(navId = R.id.action_admeHomeFragment_to_admeBasicParamConfigFragment))
                )
                moduleList.add(
                    ConfigModule(
                        DeviceOperationModule(
                            name = "高级配置",
                            desc = "计米轮、测斜议、执行机构等",
                            navId = R.id.action_global_to_admeAdvancedConfigurationFragment
                        )
                    )
                )
                moduleList.add(
                    ConfigModule(RebootModule())
                )
                moduleList.add(
                    ConfigModule(AdvancedSettingsModule(navId = R.id.action_global_to_admeAdvancedSettingFragment))
                )
            }
        }
        binding.rvModule.models = moduleList
    }

    override fun chooseAdmeMode() {
        val selectedIndex = modeList.indexOf(mHeadStates.admeModeText.get())
        XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
        XPopup.Builder(context)
            .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .enableDrag(false)
            .asBottomList(
                "", modeList,
                null, selectedIndex,
                { position, text ->
                    //不可以主动切换到异常保护模式
                    if (text == modeList[2]) {
                        return@asBottomList
                    }
                    mHeadStates.admeModeText.set(
                        when (position) {
                            0 -> {
                                mMessenger.admeDeviceMode.set("0")
                                modeList[0]
                            }

                            1 -> {
                                mMessenger.admeDeviceMode.set("1")
                                modeList[1]
                            }

                            else -> modeList[2]
                        }
                    )
                    updateConfigModuleData()
                    setEquipModel()
                }, 0, R.layout.custom_xpopup_adapter_text_center
            )
            .show()
    }

    override fun onNetPlatformReady() {
        queryEquipmentBaseInfo()
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        //蓝牙模式下，等蓝牙建立连接后查询设备工作模式
        queryEquipmentBaseInfo()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryEquipmentBaseInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 获取电机的运行状态
     */
    private fun getMotorMotionData(timeMillis: Long = 0L) {
        launchWithViewLifecycle {
            delay(timeMillis)

            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_MD_GET_MOTION_STATE
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    private fun setEquipModel() {
        val entity = AdmeEquipModelEntity(
            when (mHeadStates.admeModeText.get()) {
                modeList[0] -> "0"
                modeList[1] -> "1"
                else -> "2"
            }
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_EQUIPMENT_MODEL,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is CommandDebugConfigModule -> {
                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
                    true,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().navigate(configModule.navId, bundle)
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
            IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS -> {//获取设备的基本信息
                val result =
                    iotParseManager.parse<AdmeBaseInfo>(
                        cmdStr,
                        IOTCommandType.ADME_MD_GET_EQUIPMENT_BASIS
                    )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取设备的基本信息出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            //获取设备的运行状态
                            getMotorMotionData()
                        }
                        val baseInfo: AdmeBaseInfo = result.data
                        mHeadStates.admeModeText.set(
                            when (baseInfo.equimodel) {
                                "0" -> modeList[0]
                                "1" -> modeList[1]
                                else -> modeList[2]
                            }
                        )
                        mMessenger.admeDeviceMode.set(baseInfo.equimodel)
                        updateConfigModuleData()
                    }
                }
            }

            IOTCommandType.ADME_MD_GET_MOTION_STATE -> {//获取ADME的运行状态
                val result = iotParseManager.parse<AdmeMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取设备的运行状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            //获取设备的运行状态
//                            getMotorMotionData(20000)
                        }
                        updateMotionState(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_EQUIPMENT_MODEL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置设备模式出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            else -> {

            }
        }
    }

    /**
     * 刷新电机运动状态
     */
    private fun updateMotionState(admeMotionState: AdmeMotionState) {
        when (admeMotionState.motionstate) {
            "0" -> mHeadStates.runningStateText.set("管口停止")
            "1" -> mHeadStates.runningStateText.set("管底停止")
            "2" -> mHeadStates.runningStateText.set("管口测量")
            "3" -> mHeadStates.runningStateText.set("管口测试")
            "4" -> mHeadStates.runningStateText.set("上拉测量")
            "5" -> mHeadStates.runningStateText.set("上拉测试")
            "6" -> mHeadStates.runningStateText.set("下放测量")
            "7" -> mHeadStates.runningStateText.set("下放测试")
            "8" -> mHeadStates.runningStateText.set("防冻下放完成")
            else -> mHeadStates.runningStateText.set("未知状态:${admeMotionState.motionstate}")
        }
    }
}