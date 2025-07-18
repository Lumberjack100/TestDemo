package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50CurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/7/18
 * @desc: 一体式自供电 GNSS 接收机(M50)运行信息
 *
 */
class M50RunningInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "运行信息"
    }

    override fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.QUERY_DEVICE_STATUS)
                || (commandType == IOTCommandType.SAMPLE)

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initRunningData1(result.data)
                        if (result.data.contains("\"work_mode\":1")) {
                            sendCommandFromCmdList()
                            binding.refreshLayout.finish()

                        } else {
//                            var command =
//                                IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=1")
//                            commandItems.add(command)
//
//                            sendCommandFromCmdList(isStartTimeoutJob = true)

                            initRunningData2("{\"sum_value\":0.000,\"x_value\":0.000,\"y_value\":0.000,\"z_value\":0.000}")
                            initRunningData3("{\"sum_value\":0.000,\"x_value\":0.000,\"y_value\":0.000,\"z_value\":0.000}")
                        }
                    }
                }
            }

            IOTCommandType.SAMPLE -> { // 召测
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        if (cmdStr.contains("method=1")) {
                            initRunningData2(result.data)
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initRunningData1(content: String) {
        try {
            val stateInfo = MoshiUtil.fromJson<M50CurrentStateInfo>(content)
            if (stateInfo == null) {
                binding.refreshLayout.showEmpty()
                return
            }
            binding.refreshLayout.showContent()
            val groupList = mutableListOf<Any>()

            groupList.add(DeviceStatusInfoGroupItem("工作信息"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "工作模式",
                value = when (stateInfo.workMode) {
                    "1" -> "基站"
                    "2" -> "测站"
                    else -> AppContants.PLACE_HOLDER_VALUE
                },
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "上报模式",
                value = when (stateInfo.reportMode) {
                    "0" -> "常在线"
                    "1" -> "低功耗"
                    "2" -> "自适应"
                    else -> AppContants.PLACE_HOLDER_VALUE
                },
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "网络模式",
                value = when (stateInfo.netMode) {
                    "0" -> "4G传输"
                    "1" -> "电台传输"
                    "2" -> "自动"
                    else -> AppContants.PLACE_HOLDER_VALUE
                }
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "卫星数量",
                value = stateInfo.starNum,
                isBottomItem = true
            )

            binding.recyclerview.models = groupList
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initRunningData2(content: String) {
        try {
            // {"sw":1,"mode":8,"initENU":"0.000000,0.000000,0.000000","baseLine":0.000000,"fixRate":0.0,"gap_fixRate":0.0,"result":"0.000,0.000,0.000","status":"not-fix","dataSource":"mqtt"}
            val resultMap = MoshiUtil.fromJson<Map<String, String>>(content) ?: return
            if (resultMap.isEmpty()) return
            val groupList = mutableListOf<Any>()

            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("数据解算"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "前端解算功能",
                value = when (resultMap["sw"]) {
                    "0" -> "启用"
                    "1" -> "未启用"
                    else -> AppContants.PLACE_HOLDER_VALUE
                }
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "解算模式",
                value = when (resultMap["mode"]) {
                    "1" -> "后端解算"
                    "2", "4", "8" -> "前端解算"
                    else -> AppContants.PLACE_HOLDER_VALUE
                }
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "累计固定解比例",
                value = resultMap["fixRate"]?.let { "$it%" } ?: AppContants.PLACE_HOLDER_VALUE
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "实时固定解比例",
                value = resultMap["gap_fixRate"]?.let { "$it%" } ?: AppContants.PLACE_HOLDER_VALUE
            )

            val baseLine =
                resultMap["baseLine"]?.toDoubleOrNull()?.div(1000) ?: AppContants.PLACE_HOLDER_VALUE
            val kmValue = DeviceStatusInfoProcessor.formatDoubleValue(
                baseLine.toString(),
                AppContants.PLACE_HOLDER_VALUE,
                2
            )
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "基线长",
                value = DeviceStatusInfoProcessor.formatDoubleValue(
                    baseLine.toString(),
                    AppContants.PLACE_HOLDER_VALUE,
                    2
                ),
                unit = if (kmValue == AppContants.PLACE_HOLDER_VALUE) "" else "km"
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "实时解算状态",
                value = when (resultMap["status"]) {
                    "succ" -> "解算成功"
                    "not-fix" -> "无固定解"
                    "base-not-ready" -> "基站未就绪"
                    "initialing" -> "初始化中"
                    "calculating" -> "解算中"
                    "not-licensed" -> " 板卡未注册"
                    "not-define-stat" -> "未定"
                    else -> AppContants.PLACE_HOLDER_VALUE
                }
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "差分数据源",
                value = resultMap["dataSource"] ?: AppContants.PLACE_HOLDER_VALUE,
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

    private fun initRunningData3(content: String) {
        try {
            // {"sw":1,"mode":8,"initENU":"0.000000,0.000000,0.000000","baseLine":0.000000,"fixRate":0.0,"gap_fixRate":0.0,"result":"0.000,0.000,0.000","status":"not-fix","dataSource":"mqtt"}
            val resultMap = MoshiUtil.fromJson<Map<String, String>>(content) ?: return
            if (resultMap.isEmpty()) return
            val groupList = mutableListOf<Any>()

            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("初始坐标"))
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "初始化完成时间",
                value = resultMap["time"] ?: AppContants.PLACE_HOLDER_VALUE,
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "东（E）",
                value = resultMap["time"] ?: AppContants.PLACE_HOLDER_VALUE,
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "北（N）",
                value = resultMap["time"] ?: AppContants.PLACE_HOLDER_VALUE,
            )

            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "天（U）",
                value = resultMap["dataSource"] ?: AppContants.PLACE_HOLDER_VALUE,
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