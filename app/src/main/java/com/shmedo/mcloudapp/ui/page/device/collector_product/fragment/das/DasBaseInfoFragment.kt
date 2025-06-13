package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.util.Log
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.model.das.DeviceStatusInfoOne
import com.shmedo.lib.cmd.base.md_cmd.model.das.VersionMessageInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 物联网采集器(DAS)基本信息
 *
 */
class DasBaseInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {
    private var isBleMode = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "基本信息"
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
         * 获取版本信息 ##040\r\n <br/>
         * 应答: $$040,(1),(2),(3)\r\n<br/>
         * (1)“产品序列号”<br/>
         * (2)“固件版本号”<br/>
         * (3)“生产日期”<br/>
         */
        command = MDCommandUtil.getCommand(MDCommandType.VERSION_MESSAGE)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

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

            MDCommandType.VERSION_MESSAGE -> {//##040\r\n：获取版本信息
                val result = mdParseManager.parse<VersionMessageInfo>(
                    cmdStr,
                    MDCommandType.VERSION_MESSAGE
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询版本信息出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleVersionInfo(result.data)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun init4GBaseInfo(baseInfo: DasBaseInfo) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("设备信息"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备SN",
                value = baseInfo.sn,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备启动代码",
                value = baseInfo.code,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = baseInfo.ver,
                isBottomItem = true
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initBleDeviceStatusOne(info: DeviceStatusInfoOne) {
        try {
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("设备信息"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备SN",
                value = info.snNumber,
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "设备启动代码",
                value = "${MDCommandUtil.formatStringTwo(info.startCodeOne)}${
                    MDCommandUtil.formatStringTwo(info.startCodeTwo)
                }",
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initBleVersionInfo(info: VersionMessageInfo) {
        try {
            val groupList = mutableListOf<Any>()
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "固件版本",
                value = info.firmwareVersion,
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