package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable
import com.blankj.utilcode.util.ConvertUtils
import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   配置模块
 */
data class ConfigModuleTree(
    val configModules: MutableList<UnifiedDeviceModule> = arrayListOf(),
)


// 1. 添加包装器
data class UnifiedDeviceModule(
    val module: DeviceFunctionModule
) : BaseObservable() {

    // 代理所有属性
    val name: String get() = module.name
    val desc: String get() = module.desc
    val resID: Int get() = module.resID
    val iconSize: Int get() = module.iconSize
    val navId: Int get() = module.navId
    val isConnected: Boolean get() = module.isConnected
    val isSupport: Boolean get() = module.isSupport

    // 代理所有方法
    fun refreshStatus(state: Boolean) {
        module.refreshStatus(state)
        notifyChange()
    }

    fun refreshSupport(state: Boolean) {
        module.refreshSupport(state)
        notifyChange()
    }

    fun isModuleAvailable(): Boolean = module.isModuleAvailable()
}

// 2. 扩展函数
fun DeviceFunctionModule.toUnified() = UnifiedDeviceModule(this)
fun MutableList<DeviceFunctionModule>.toUnifiedList() = map { it.toUnified() }.toMutableList()

sealed class DeviceFunctionModule(
    val name: String = "",
    val desc: String = "",
    val resID: Int = 0,
    val iconSize: Int = ConvertUtils.dp2px(40f),
    val navId: Int = 0,
    var isConnected: Boolean = true,
    var isSupport: Boolean = true
) : BaseObservable() {

    fun refreshStatus(state: Boolean) {
        this.isConnected = state
        notifyChange()
    }

    fun refreshSupport(state: Boolean) {
        this.isSupport = state
        notifyChange()
    }

    // 增加一个方法来检查模块的支持状态
    fun isModuleAvailable(): Boolean {
        return isSupport && isConnected
    }
}

class CommonModule(
    name: String = "",
    desc: String = "",
    resID: Int = R.drawable.ic_module_current_state,
    iconSize: Int = ConvertUtils.dp2px(40f),
    navId: Int = 0,
    isSupport: Boolean = true
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    iconSize = iconSize,
    navId = navId,
    isSupport = isSupport
)

class RunningStatusModule(
    name: String = "状态",
    desc: String = "获取当前设备状态",
    resID: Int = R.drawable.ic_module_current_state,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class TimeCalibrationModule(
    name: String = "时间校准",
    desc: String = "获取当前设备时间",
    resID: Int = R.drawable.ic_module_time_calibration,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class TelemetryDataModule(
    name: String = "召测",
    desc: String = "召测当前采集数据",
    resID: Int = R.drawable.ic_sample,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class RebootModule(
    name: String = "重启",
    desc: String = "重新启动当前设备",
    resID: Int = R.drawable.ic_module_reboot,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class DataCenterModule(
    name: String = "链路配置",
    desc: String = "连接平台参数配置",
    resID: Int = R.drawable.ic_module_datacenter,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class AdvancedSettingsModule(
    name: String = "设置",
    desc: String = "高级设置",
    resID: Int = R.drawable.ic_module_setting,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class CommandDebugConfigModule(
    name: String = "指令下发",
    desc: String = "调试指令日志输出",
    resID: Int = R.drawable.ic_module_cmd_debug,
    navId: Int = R.id.action_global_to_commandDebug,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

//<editor-fold desc="ADME 功能模块">
class BasicConfigModule(
    name: String = "基础配置",
    desc: String = "设备基础参数配置",
    resID: Int = R.drawable.ic_basic_config,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)
// </editor-fold>

//<editor-fold desc="DAS 功能模块">
class CollectorConfigModule(
    name: String = "采集器配置",
    desc: String = "采集器参数配置",
    resID: Int = R.drawable.ic_device_collector_config,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class SensorConfigModule(
    name: String = "传感器配置",
    desc: String = "传感器参数配置",
    resID: Int = R.drawable.ic_module_sensor_setting,
    navId: Int = 0,
    isSupport: Boolean = true
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
    isSupport = isSupport
)
// </editor-fold>

//<editor-fold desc="MR702 功能模块">
class DeviceOperationModule(
    name: String = "设备操作",
    desc: String = "时间校准、人工置数、召测等",
    resID: Int = R.drawable.ic_module_setting,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class MR702ManualSettingModule(
    name: String = "人工置数",
    desc: String = "人工录入历史采集数据",
    resID: Int = R.drawable.ic_manual_setting,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class DeviceLogUploadModule(
    name: String = "日志读取",
    desc: String = "可筛选、读取并上传平台",
    resID: Int = R.drawable.ic_log_upload,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class MR702ParameterExportModule(
    name: String = "参数导出",
    desc: String = "当前配置参数，导出并上传",
    resID: Int = R.drawable.ic_parameter_export,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class MR702ParameterImportModule(
    name: String = "参数导入",
    desc: String = "从平台获取参数配置导入",
    resID: Int = R.drawable.ic_parameter_import,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class MR702ManualPhotoTakingModule(
    name: String = "手动拍照",
    desc: String = "确认摄像机已接入",
    resID: Int = R.drawable.ic_manual_photo_taking,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class MR702Remote485SilenceModule(
    name: String = "远程消警",
    desc: String = "只对485报警器有效",
    resID: Int = R.drawable.ic_sample,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class MR702CleanClearAlarmModule(
    name: String = "清除消警",
    desc: String = "清除消警状态，恢复低等级阈值触发",
    resID: Int = R.drawable.ic_sample,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

class MR702RainSetZeroModule(
    name: String = "雨量置零",
    desc: String = "清除当前所有雨量统计",
    resID: Int = R.drawable.ic_sample,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)
// </editor-fold>

class OneClickSilenceModule(
    name: String = "一键消音",
    desc: String = "立即停止语音播报",
    resID: Int = R.drawable.ic_parameter_export,
    navId: Int = 0,
) : DeviceFunctionModule(
    name = name,
    desc = desc,
    resID = resID,
    navId = navId,
)

