package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.assemble.entity.das.AuthenticationEntity
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.model.das.AuthenticationInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.AuthenticationResultInfo
import com.shmedo.lib.cmd.base.md_cmd.model.das.DasBaseConfigInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.DesUtil
import com.shmedo.lib.cmd.base.md_cmd.utils.HexUtils
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showDialogFragment
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CollectorConfigModule
import com.shmedo.mcloudapp.model.CommandDebugConfigModule
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DataCenterModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.SensorConfigModule
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceHomeFragment
import com.shmedo.mcloudapp.ui.page.device.common.BleCustomCommandLogPrintFragment
import com.shmedo.mcloudapp.ui.page.device.u_product.dialog.FindDeviceBeepDialog
import kotlinx.coroutines.flow.debounce
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.nio.charset.StandardCharsets

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： 物联网采集器(DAS) - 支持4G和蓝牙两种通讯方式
 */
class DASHomeFragment : BaseDeviceHomeFragment() {
    private val mdParseManager: MDParserManager by inject()

    private var collectorModel = ""

    override fun initData() {
        super.initData()
        if (deviceInfo.productName.startsWith("MR701")) {
            mHeadStates.productErrorResId.set(R.drawable.device_logo_mr701_old_error)
            mHeadStates.productAlarmResId.set(R.drawable.device_logo_mr701_old_alarm)
            mHeadStates.productOfflineResId.set(R.drawable.device_logo_mr701_old_offline)
            mHeadStates.productNormalResId.set(R.drawable.device_logo_mr701_old)
            mHeadStates.productName.set("智能遥测终端机")
            mHeadStates.productToken.set("MR701")
        } else {
            when (productType) {
                ProductType.BHY -> {
                    mHeadStates.productErrorResId.set(R.drawable.device_logo_bhy_3s_error)
                    mHeadStates.productAlarmResId.set(R.drawable.device_logo_bhy_3s_alarm)
                    mHeadStates.productOfflineResId.set(R.drawable.device_logo_bhy_3s_offline)
                    mHeadStates.productNormalResId.set(R.drawable.device_logo_bhy_3s)
                }

                else -> {
                    mHeadStates.productErrorResId.set(R.drawable.device_logo_das_error)
                    mHeadStates.productAlarmResId.set(R.drawable.device_logo_das_alarm)
                    mHeadStates.productOfflineResId.set(R.drawable.device_logo_das_offline)
                    mHeadStates.productNormalResId.set(R.drawable.device_logo_das)
                }
            }
        }
        mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
    }

    override fun initModuleData() {
        val groupList = mutableListOf<Any>()
        groupList.add(DeviceStatusInfoGroupItem("设备信息"))
        groupList.add(
            ConfigModuleTree(
                configModules = arrayListOf(
                    CommonModule(
                        name = "基本信息",
                        resID = R.drawable.ic_module_basic_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_dasBaseInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "网络信息",
                        resID = R.drawable.ic_module_net_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_dasNetInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "传感信息",
                        resID = R.drawable.ic_module_state_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_dasSensorInfoFragment
                    ).toUnified(),
                    CommonModule(
                        name = "位置信息",
                        resID = R.drawable.ic_module_location_info,
                        iconSize = ConvertUtils.dp2px(34f),
                        navId = R.id.action_global_to_commonLocationInfoFragment
                    ).toUnified()
                )
            )
        )

        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("设备配置"))
        val configModuleTree = ConfigModuleTree()
        configModuleTree.configModules.add(
            CollectorConfigModule(
                name = "采集配置",
                resID = R.drawable.ic_module_collect_setting,
                navId = R.id.action_global_to_dasCollectorSettingFragment
            ).toUnified()
        )
        configModuleTree.configModules.add(
            DataCenterModule(
                name = "链路配置",
                resID = R.drawable.ic_module_datacenter_new,
                navId = if (communicateWay is NetPlatformConnect) R.id.action_global_to_dasDataCenterHomeFragment else R.id.action_global_to_bleDasDataCenterHomeFragment
            ).toUnified()
        )
        if (communicateWay is NetPlatformConnect) {
            configModuleTree.configModules.add(
                CommonModule(
                    name = "上报配置",
                    resID = R.drawable.ic_module_work_mode_new,
                    navId = R.id.action_global_to_dasReportConfigFragment
                ).toUnified()
            )
        }
        configModuleTree.configModules.add(
            SensorConfigModule(
                name = "传感配置",
                resID = R.drawable.ic_module_sensor_setting_new,
                navId = R.id.action_global_to_dasSensorHomeFragment
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
        if (communicateWay is BleConnect) {
            configModuleTree.configModules.add(
                CommandDebugConfigModule(
                    resID = R.drawable.ic_module_cmd_debug_new,
                ).toUnified()
            )
        }

        groupList.add(configModuleTree)
        binding.rvModule.models = groupList
    }

    override fun processOtherItemClick(configModule: DeviceFunctionModule) {
        when (configModule) {
            is DataCenterModule -> {
                nav().safeNavigate(
                    configModule.navId,
                    BaseDataCenterHomeFragment.newBundleArguments(
                        centerNum = 3,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                )
            }

            is CollectorConfigModule -> {//采集器配置
                val bundle = DasCollectorSettingFragment.newBundleArguments(
                    collectorModel,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice,
                )
                nav().safeNavigate(
                    configModule.navId,
                    bundle
                )
            }

            is SensorConfigModule -> {//传感器配置
                val bundle = DasSensorHomeFragment.newBundleArguments(
                    collectorModel,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice,
                )
                nav().safeNavigate(
                    configModule.navId,
                    bundle
                )
            }

            is CommandDebugConfigModule -> {//指令下发
                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
                    false,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice
                )
                nav().safeNavigate(configModule.navId, bundle)
            }


            else -> {
                super.processOtherItemClick(configModule)
            }
        }
    }

    override fun onBleDeviceReady() {
        setAuthenticateWay()
    }

    /**
     * 蓝牙连接成功,发送认证方式
     */
    private fun setAuthenticateWay() {
        commandItems.clear()
        val entity = AuthenticationEntity(deviceInfo.deviceToken, "0")
        val command =
            MDCommandUtil.getCommand(MDCommandType.AUTHENTICATION_CONFIG, entity.toCommandString())
        commandItems.add(command)

        Timber.d("设置认证类型指令===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = false)
    }

    /**
     * 开始认证流程
     */
    private fun sendAuthenticateCodeCmd(authenticateParam: String) {
        Timber.d("解密前:%s", authenticateParam)
        val resultData = HexUtils.hexStringToBytes(authenticateParam)
        try {
            val deskey = "12345678"
            // 解密后认证码
            val strDecrypt = String(DesUtil.decrypt(resultData, deskey)!!, StandardCharsets.UTF_8)
            Timber.d("解密后:%s", strDecrypt)

            if (strDecrypt.isNotEmpty()) {
                // 反转6位随机码
                val reverseRandomCode = strDecrypt.substring(0, 6).reversed()
                val byteEncrypt =
                    DesUtil.encrypt(reverseRandomCode.toByteArray() + deskey.toByteArray(), deskey)
                // 加密后认证码
                val strEncrypt = HexUtils.bytesToHexString(byteEncrypt!!)!!
                val command =
                    "##222,${deviceInfo.deviceToken},0,${strEncrypt.uppercase()}${MDConstants.COMMAND_FOOTER}"
                Timber.d("设备登录验证指令===%s", command)

                commandItems.clear()
                commandItems.add(command)
                sendCommandFromCmdList(isStartTimeoutJob = false)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 查询 DAS 设备的配置参数信息
     */
    private fun queryBleDASConfigInfo() {
        commandItems.clear()

        val command =
            MDCommandUtil.getCommand(MDCommandType.BASE_CONFIG)
        commandItems.add(command)

        Timber.d("获取基础配置信息指令===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = false)
    }

    override fun setResultData(cmdStr: String) {
        if (communicateWay == BleConnect) {
            handleBleCommandResult(cmdStr)
        } else {
            handle4GCommandResult(cmdStr)
        }
    }

    /**
     * 处理4G通讯指令结果
     */
    private fun handle4GCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SEARCH_DEVICE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设备查找出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            showDialogFragment(FindDeviceBeepDialog.Companion.TAG) {
                                FindDeviceBeepDialog.Companion.newInstance(productType)
                            }
                        }
                    }
                }
            }

            else -> {
                processOtherCmdResult(
                    IOTCommandUtil.extractCommandType(cmdStr),
                    cmdStr
                )
            }
        }
    }

    /**
     * 处理蓝牙通讯指令结果
     */
    private fun handleBleCommandResult(cmdStr: String) {
        updateLastCommunicationTime()

        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.AUTHENTICATION_CONFIG -> {
                val result = mdParseManager.parse<AuthenticationInfo>(
                    cmdStr,
                    MDCommandType.AUTHENTICATION_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        setAuthenticateWay()//重新认证
                        return
                    }

                    is MDCommandResult.Success -> {
                        val authenticationInfo: AuthenticationInfo = result.data
                        sendAuthenticateCodeCmd(authenticationInfo.publicKey)
                    }
                }
            }

            MDCommandType.DAS_SEND_AUTHENTICATION_RESULT -> {
                val result = mdParseManager.parse<AuthenticationResultInfo>(
                    cmdStr,
                    MDCommandType.DAS_SEND_AUTHENTICATION_RESULT
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        setAuthenticateWay()//重新认证
                        return
                    }

                    is MDCommandResult.Success -> {
                        val authenticationInfo: AuthenticationResultInfo = result.data
                        if (authenticationInfo.result == "1") {
                            onAuthenticateResult(true)
                        } else {
                            Toaster.show("设备认证失败!")
                            bleViewModel.disconnect()
                            onAuthenticateResult(false)
                        }
                    }
                }
            }

            MDCommandType.BASE_CONFIG -> {
                val result = mdParseManager.parse<DasBaseConfigInfo>(
                    cmdStr,
                    MDCommandType.BASE_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询基础配置信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        initBaseConfigInfo(result.data)
                    }
                }
            }

            MDCommandType.INSTALL_LOCATION -> {
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "位置同步失败"
                        Timber.e(errMsg)
                        // 自动同步失败时不显示错误提示，静默处理
                        cancelNearbyCommunicationTimeoutJob()
                        isLocationSyncInProgress = false
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Timber.d("位置自动同步成功")
                            isLocationSyncInProgress = false
                            locationViewModel.stopLocation()
                        }
                    }
                }
            }

            else -> {
                if (cmdStr.contains("Please verify the equipment.")) {
                    Toaster.show("设备认证失败!")
                    bleViewModel.disconnect()
                    onAuthenticateResult(false)

                } else if (cmdStr.contains("Equipment Verify OK.")) {
                    onAuthenticateResult(true)

                } else {

                }
            }
        }
    }

    private fun onAuthenticateResult(isSuccess: Boolean) {
        cancelNearbyCommunicationTimeoutJob()
        if (isSuccess) {
            queryBleDASConfigInfo()
            autoSyncLocationIfNeeded()
        }
    }

    private fun initBaseConfigInfo(info: DasBaseConfigInfo) {
        try {
            collectorModel = info.collectorModel
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun setupHeartbeat() {
        launchWithViewLifecycle {
            lastCommunicationTime
                .debounce(AppContants.Communication.DELAY_BLE_HEART_BEAT)  // 30秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime =
                        TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
                    // 仅当设备连接并且需要发送心跳时，才发送心跳包
                    if (mHeadStates.isConnected.get()) {
                        Timber.d("发送心跳包指令 startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = MDCommandUtil.getCommand(MDCommandType.HEART_BEAT)
                        Timber.d("发送心跳包指令: $command")
                        sendBleCommand(command)
                    }
                }
        }
    }
}