package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
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
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
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
                    )
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
                    )
                )

                val status3 = communicationData.status3.compareAndReturn(
                    "0",
                    "未启用",
                    communicationData.status3.compareAndReturn("1", "已连接", "未连接")
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "数据链路3",
                    value = "$status3(米度物联平台)",
                    textColorRes = if (communicationData.status3 == "0" || status3 == "未连接") 0 else ColorUtils.getColor(
                        R.color.online_colorPrimary
                    )
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
                    )
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

}