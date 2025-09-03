package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRCommunicationData
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterParamFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 遥测终端机网络信息
 *
 */
class MR702NetInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "网络信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        // 查询设备基本信息（page=1, label=1）
        val entity1 = MRDeviceInfoEntity(pages = 1, label = 1)
        val command1 = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity1)
        commands.add(command1)

        // 查询设备通信数据（page=2, label=1）
        val entity2 = MRDeviceInfoEntity(pages = 2, label = 1)
        val command2 = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity2)
        commands.add(command2)

        // 查询设备状态
        commands.add(IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS))

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        val deviceInfo = result.data
                        if (deviceInfo.pages == "1" && deviceInfo.label == "1") {
                            initBaseInfo(deviceInfo.baseInfo)
                        } else if (deviceInfo.pages == "2" && deviceInfo.label == "1") {
                            initCommunicationInfo(deviceInfo.communicationData)
                        }
                    }
                }
            }

            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initStatusInfo(result.data)
                    }
                }
            }

            else -> {
                // 其他指令类型忽略
            }
        }
    }

    private fun initBaseInfo(stateInfo: MRBaseInfo) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("数据网络"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "信号强度",
                value = when (stateInfo.csq.toInt()) {
                    1 -> "优"
                    2 -> "良好"
                    3 -> "较差"
                    else -> "未知"
                }
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "IMEI",
                value = stateInfo.imei,
                isClipboard = true
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "ICCID",
                value = stateInfo.iccid,
                isClipboard = true,
                isBottomItem = true
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initCommunicationInfo(communicationData: MRCommunicationData) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("数据链路"))
            val status1 = communicationData.status1.compareAndReturn(
                "0",
                "未启用",
                communicationData.status1.compareAndReturn("1", "已连接", "未连接")
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数据链路1",
                value = status1,
                textColorRes = if (communicationData.status1 == "0" || status1 == "未连接") 0 else ColorUtils.getColor(
                    R.color.online_colorPrimary
                ),
                isClickable = true,
            )

            val status2 = communicationData.status2.compareAndReturn(
                "0",
                "未启用",
                communicationData.status2.compareAndReturn("1", "已连接", "未连接")
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数据链路2",
                value = status2,
                textColorRes = if (communicationData.status2 == "0" || status2 == "未连接") 0 else ColorUtils.getColor(
                    R.color.online_colorPrimary
                ),
                isClickable = true,
            )

            val status3 = communicationData.status3.compareAndReturn(
                "0",
                "未启用",
                communicationData.status3.compareAndReturn("1", "已连接", "未连接")
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数据链路3",
                value = status3,
                textColorRes = if (communicationData.status3 == "0" || status3 == "未连接") 0 else ColorUtils.getColor(
                    R.color.online_colorPrimary
                ),
                isClickable = true,
            )

            val status4 = communicationData.status4.compareAndReturn(
                "0",
                "未启用",
                communicationData.status4.compareAndReturn("1", "已连接", "未连接")
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数据链路4",
                value = status4,
                textColorRes = if (communicationData.status4 == "0" || status4 == "未连接") 0 else ColorUtils.getColor(
                    R.color.online_colorPrimary
                ),
                isClickable = true,
            )

            val status5 = communicationData.status5.compareAndReturn(
                "0",
                "未启用",
                communicationData.status5.compareAndReturn("1", "已连接", "未连接")
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数据链路5",
                value = status5,
                textColorRes = if (communicationData.status5 == "0" || status5 == "未连接") 0 else ColorUtils.getColor(
                    R.color.online_colorPrimary
                ),
                isClickable = true,
                isBottomItem = true
            )


            binding.recyclerview.bindingAdapter.apply {
                mutable.addAll(groupList)
                notifyItemRangeInserted(itemCount, groupList.size)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun <T> initStatusInfo(content: T) {
        try {
            // 将 JSON 字符串解析为 Map 对象
            val statusMap = MoshiUtil.fromJson<Map<String, Any>>(content as String)
            if (statusMap == null) {
                return
            }

            val groupList = mutableListOf<Any>()
            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("北斗短报文"))


            val cmdStr = content as String

            val pattern = "\"bd_signal\":([0-9.]+)".toRegex()
            val bdSignal = pattern.find(cmdStr)?.groupValues?.get(1) ?: "0"
            val bdSignalValue = bdSignal.toDoubleOrNull() ?: 0.0
            val bdSignalIntValue = bdSignalValue.toInt() // 用于信号强度等级判断

            //未接入：bd_signal 为 0 值且无 bdsim 字段或 bd_signal 为 0 值且 bdsim 为 0 值
            val hasBdsim = cmdStr.contains("\"key\":\"bdsim\"")
            val bdsimValue = if (hasBdsim) {
                // 简单的字符串匹配，查找 bdsim 的值
                val pattern = "\"key\":\"bdsim\",\"value\":\"([^\"]+)\"".toRegex()
                pattern.find(cmdStr)?.groupValues?.get(1) ?: ""
            } else ""

            if (bdSignalIntValue == 0 && (!hasBdsim || bdsimValue == "000000")) {
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数传终端",
                    value = "未接入",
                    textColorRes = 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "信号强度",
                    value = AppContants.Companion.PLACE_HOLDER_VALUE,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "信号值(dB)",
                    value = AppContants.Companion.PLACE_HOLDER_VALUE,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "北斗卡号",
                    value = AppContants.Companion.PLACE_HOLDER_VALUE,
                    isBottomItem = true
                )
                binding.recyclerview.bindingAdapter.apply {
                    mutable.addAll(groupList)
                    notifyItemRangeInserted(itemCount, groupList.size)
                }
                return
            }

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数传终端",
                value = "已接入",
                textColorRes = ColorUtils.getColor(R.color.online_colorPrimary)
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "信号强度",
                value = when (bdSignalIntValue) {
                    0 -> "无"
                    in 1..41 -> "极差"
                    in 42..44 -> "较差"
                    in 45..46 -> "一般"
                    in 47..49 -> "良好"
                    else -> if (bdSignalIntValue >= 50) "极好" else "无"
                }
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "信号值(dB)",
                value = bdSignalIntValue.toString(),
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "北斗卡号",
                value = if (bdsimValue == "000000") AppContants.Companion.PLACE_HOLDER_VALUE else bdsimValue,
                isBottomItem = true
            )

            binding.recyclerview.bindingAdapter.apply {
                mutable.addAll(groupList)
                notifyItemRangeInserted(itemCount, groupList.size)
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun processItemClick(infoBasicItem: DeviceStatusInfoBasicItem) {
        if (infoBasicItem.name.startsWith("数据链路")) {
            val index = infoBasicItem.name.substringAfter("数据链路").toIntOrNull() ?: 0
            val item = DataCenterStatusItem(
                centerid = index,
                name = "数据链路$index",
                //用 when 表达式
                status = when {
                    infoBasicItem.value.contains("未启用") -> "0"
                    infoBasicItem.value.contains("已连接") -> "1"
                    infoBasicItem.value.contains("未连接") -> "2"
                    else -> "0"
                }
            )

            val bundle = UniversalDataCenterParamFragment.newBundleArguments(
                item,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_global_to_dataCenterParamFragment,
                bundle
            )
        }
    }
}