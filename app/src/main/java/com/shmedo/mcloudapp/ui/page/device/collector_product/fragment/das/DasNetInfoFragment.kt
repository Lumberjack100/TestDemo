package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasNetStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.model.common.DeviceNetStatus
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoOne
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.notNullKeyEmpty
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 物联网采集器(DAS)网络信息 - 支持4G和蓝牙两种通讯方式
 *
 */
class DasNetInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {
    private var isBleMode = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "网络信息"
    }

    override fun initData() {
        super.initData()
        // 判断通讯方式
        isBleMode = communicateWay == BleConnect
    }

    override fun queryStatusInfo() {
        if (isBleMode) {
            queryBleInfo()
        } else {
            query4GInfo()
        }
    }

    /**
     * 4G通讯模式查询信息
     */
    private fun query4GInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_DEVICE_BASE)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_NET_STATUS, "index=0")
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 蓝牙通讯模式查询信息
     */
    private fun queryBleInfo() {
        commandItems.clear()

        /**
         * 查询设备状态1: ##041\r\n <br/>
         * 应答: $$041,(1),(2),(3),(4),(5),(6)\r\n <br/>
         * （1）SN号 <br/>
         * （2）IMEI号 <br/>
         * （3）SIM卡号 <br/>
         * （4）启动代码1 <br/>
         * （5）启动代码2 <br/>
         * （6）信号强度，1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号 <br/>
         */
        var command = MDCommandUtil.getCommand(MDCommandType.QUERY_DAS_STATUS_1)
        commandItems.add(command)

        /**
         * 查询数据链路网络状态 ##044n\r\n<br/>
         * 应答:$$044n,(1),(2),(3),(4),(5),(6),(7),(8),(9),(10)\r\n<br/>
         * 注：n取值1，2，3<br/>
         * （1）已发送数据<br/>
         * （2）已生成数据<br/>
         * （3）flash使能，取值0,1，1表示使能，0表示未使能<br/>
         * （4）flash读指针<br/>
         * （5）flash写指针<br/>
         * （6）链路使能：取值0,1，1表示使能，0表示未使能<br/>
         * （7）链路状态：取值0,1，1表示已上线，0表示未上线<br/>
         * （8）4G模块状态：<br/>
         * （9）MQTT状态：	<br/>
         * （10）在线率，单位%<br/>
         * 示例：$$0441,7,7,1,0x001000D4,0x001000D4,1,1,4,7,93.6
         */
        for (i in 1..3) {
            val command = MDCommandUtil.getCommand(MDCommandType.QUERY_NETWORK_STATUS, i.toString())
            commandItems.add(command)
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun isTargetCommandType(commandType: IOTCommandType): Boolean = true

    override fun setResultData(cmdStr: String) {
        if (isBleMode) {
            handleBleCommandResult(cmdStr)
        } else {
            handle4GCommandResult(cmdStr)
        }
    }

    /**
     * 处理4G通讯命令结果
     */
    private fun handle4GCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_DEVICE_BASE -> {
                val result = iotParseManager.parse<DasBaseInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_DEVICE_BASE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询基本信息出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        init4GBaseInfo(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_NET_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_NET_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询通讯状态出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        val content: String = result.data
                        init4GCommunicationInfo(content)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    /**
     * 处理蓝牙通讯命令结果
     */
    private fun handleBleCommandResult(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_DAS_STATUS_1 -> {//##041\r\n：查询设备状态1
                val result = mdParseManager.parse<DeviceStatusInfoOne>(
                    cmdStr,
                    MDCommandType.QUERY_DAS_STATUS_1
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询基本信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleDeviceStatusOne(result.data)
                    }
                }
            }

            MDCommandType.QUERY_NETWORK_STATUS -> {
                val result = mdParseManager.parse<DeviceNetStatus>(
                    cmdStr,
                    MDCommandType.QUERY_NETWORK_STATUS
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询数据链路状态错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleCommunicationInfo(result.data)
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun init4GBaseInfo(baseInfo: DasBaseInfo) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("数据网络"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "运营商",
                value = when (baseInfo.isp) {
                    "1" -> "中国移动"
                    "2" -> "中国联通"
                    "3" -> "中国电信"
                    else -> AppContants.Companion.PLACE_HOLDER_VALUE
                }
            )
            baseInfo.csq.notNullKey {
                var temp = it.toIntOrNull() ?: 0
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "信号强度",
                    value = when (temp) {
                        in 26..31 -> "优"
                        in 19..25 -> "良好"
                        in 12..18 -> "较差"
                        else -> "差"
                    }
                )
            }
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = "IMEI",
                    value = baseInfo.imei.ifEmpty { IOTConstants.HOLD_VALUE },
                    isClipboard = true
                )
            )
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = "ICCID",
                    value = baseInfo.iccid.ifEmpty { IOTConstants.HOLD_VALUE },
                    isClipboard = true,
                    isBottomItem = true
                )
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun init4GCommunicationInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val dataList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<DasNetStatusInfo>>(content)
                }
                if (dataList.isNullOrEmpty()) {
                    return@launchWithViewLifecycle
                }
                val groupList = mutableListOf<Any>()

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("数据链路"))
                dataList.forEachIndexed { index, netStatusInfo ->
                    val statusText = netStatusInfo.errno.compareAndReturn(
                        1,
                        "已连接",
                        netStatusInfo.errno.compareAndReturn(0, "未启用", "未连接")
                    )
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "数据链路${netStatusInfo.index}",
                        value = if (netStatusInfo.index == 3) "$statusText(米度物联平台)" else statusText,
                        textColorRes = if (statusText == "已连接") ColorUtils.getColor(R.color.online_colorPrimary) else 0,
                        isBottomItem = index == dataList.size - 1
                    )
                }

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

    private fun initBleDeviceStatusOne(info: DeviceStatusInfoOne) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("数据网络"))
            info.signalStrength.notNullKeyEmpty {
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "信号强度",
                    value = when (info.signalStrength.toInt()) {
                        in 26..31 -> "优"
                        in 19..25 -> "良好"
                        in 12..18 -> "较差"
                        else -> "差"
                    }
                )
            }
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = "IMEI",
                    value = info.imeiNumber.ifEmpty { IOTConstants.HOLD_VALUE },
                    isClipboard = true
                )
            )
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = "ICCID",
                    value = info.simNumber.ifEmpty { IOTConstants.HOLD_VALUE },
                    isClipboard = true,
                    isBottomItem = true
                )
            )

            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("数据链路"))

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initBleCommunicationInfo(netStatus: DeviceNetStatus) {
        try {
            val groupList = mutableListOf<Any>()
            val statusText = netStatus.linkEnable.compareAndReturn(
                "0",
                "未启用",
                netStatus.linkStatus.compareAndReturn("1", "已连接", "未连接")
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "数据链路${netStatus.linkNumber}",
                value = if (netStatus.linkNumber == "3") "$statusText(米度物联平台)" else statusText,
                textColorRes = if (statusText == "已连接") ColorUtils.getColor(R.color.online_colorPrimary) else 0,
                isBottomItem = netStatus.linkStatus == "3"
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