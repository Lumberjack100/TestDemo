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
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/7/18
 * @desc: 优化的一体式自供电 GNSS 接收机(M50)运行信息
 *
 */
class M50RunningInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "运行信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()
        
        // 查询设备状态
        val statusCommand = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commands.add(statusCommand)
        
        // 召测数据（method=2）
        val sampleCommand = IOTCommandUtil.getCommand(IOTCommandType.SAMPLE, "method=2")
        commands.add(sampleCommand)

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
                        // 基站仅获取工作信息
                        if (result.data.contains("\"work_mode\":1")) {
                            cancelCommunication()
                        }
                        // 测站：除了获取工作信息即可，还需要获取数据解算和初始坐标信息
                        // 这些信息会在SAMPLE指令响应中处理
                    }
                }
            }
            
            IOTCommandType.SAMPLE -> {
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
                        if (cmdStr.contains("method=2")) {
                            initRunningData2(result.data)
                        }
                    }
                }
            }
            
            else -> {
                // 其他指令类型忽略
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

            // 工作信息
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

            // 数据解算
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
                    "not-licensed" -> "板卡未注册"
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

            // 初始坐标
            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
            groupList.add(DeviceStatusInfoGroupItem("初始坐标"))
            val initCompletionTime =
                resultMap["initdate"] ?: AppContants.PLACE_HOLDER_VALUE
            DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                groupList,
                name = "初始完成时间",
                value = initCompletionTime
            )

            val initENU = resultMap["initENU"] ?: AppContants.PLACE_HOLDER_VALUE
            initENU.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .let {
                    if (it.size == 3) {
                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "东（E）",
                            value = it[0],
                        )

                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "北（N）",
                            value = it[1],
                        )

                        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                            groupList,
                            name = "天（U）",
                            value = it[2],
                            isBottomItem = true
                        )
                    }
                }

            // 使用bindingAdapter添加数据，避免重复刷新
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