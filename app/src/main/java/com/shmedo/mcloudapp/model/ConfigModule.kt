package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable
import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   配置模块
 */
data class ConfigModule(
    val functionModule: DeviceFunctionModule
)

data class ConfigModuleTree(
    val configModules: MutableList<ConfigModule> = arrayListOf(),
)

sealed class DeviceFunctionModule(
    val name: String = "",
    val desc: String = "",
    val iconResId: Int = 0,
    val navId: Int = 0,
    var isConnected: Boolean = true,
    var isSupport: Boolean = true
) : BaseObservable() {

    fun refreshStatus(state: Boolean) {
        this.isConnected = state
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
    navId: Int = 0,
    isSupport: Boolean = true
) : DeviceFunctionModule(name, desc, resID, navId, isSupport = isSupport)

class RunningStatusModule(
    name: String = "状态",
    desc: String = "获取当前设备状态",
    resID: Int = R.drawable.ic_module_current_state,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class TimeCalibrationModule(
    name: String = "时间校准",
    desc: String = "获取当前设备时间",
    resID: Int = R.drawable.ic_module_time_calibration,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class TelemetryDataModule(
    name: String = "召测",
    desc: String = "召测当前采集数据",
    resID: Int = R.drawable.ic_sample,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class WorkModeModule(
    name: String = "工作模式",
    desc: String = "GNSS模式设置",
    resID: Int = R.drawable.ic_module_work_mode,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class RebootModule(
    name: String = "重启",
    desc: String = "重新启动当前设备",
    resID: Int = R.drawable.ic_module_reboot,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class RestoreFactoryModule(
    name: String = "恢复出厂",
    desc: String = "设备恢复到出厂设置",
    resID: Int = R.drawable.ic_module_reset,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class DataCenterModule(
    name: String = "数据中心",
    desc: String = "连接平台参数配置",
    resID: Int = R.drawable.ic_module_datacenter,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class AdvancedSettingsModule(
    name: String = "设置",
    desc: String = "高级设置",
    resID: Int = R.drawable.ic_module_setting,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class FirmwareUpgradeModule(
    name: String = "固件升级",
    desc: String = "选择固件升级系统",
    resID: Int = R.drawable.ic_module_firmware_upgrade,
    navId: Int = 0,
    isSupport: Boolean = true
) : DeviceFunctionModule(name, desc, resID, navId, isSupport = isSupport)

class LoraConfigModule(
    name: String = "LORA设置",
    desc: String = "传感器LORA电台设置",
    resID: Int = R.drawable.ic_module_lora,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class AlarmConfigModule(
    name: String = "报警设置",
    desc: String = "报警功能设置",
    resID: Int = R.drawable.ic_module_alarm,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class CommandDebugConfigModule(
    name: String = "指令调试",
    desc: String = "调试指令日志输出",
    resID: Int = R.drawable.ic_module_cmd_debug,
    navId: Int = R.id.action_global_to_commandDebug,
) : DeviceFunctionModule(name, desc, resID, navId)

//<editor-fold desc="ADME 功能模块">
class BasicConfigModule(
    name: String = "基础配置",
    desc: String = "设备基础参数配置",
    resID: Int = R.drawable.ic_basic_config,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)
// </editor-fold>

class SetupWizard(
    name: String = "设置向导",
    desc: String = "一键配置",
    resID: Int = R.drawable.ic_setup_wizard,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

//<editor-fold desc="DAS 功能模块">
class CollectorConfigModule(
    name: String = "采集器配置",
    desc: String = "采集器参数配置",
    resID: Int = R.drawable.ic_device_collector_config,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class SensorConfigModule(
    name: String = "传感器配置",
    desc: String = "传感器参数配置",
    resID: Int = R.drawable.ic_module_sensor_setting,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)
// </editor-fold>

//<editor-fold desc="MR702 功能模块">
class MR702PortConfigModule(
    name: String = "接口配置",
    desc: String = "串口、ADC、DI、DO配置",
    resID: Int = R.drawable.ic_device_sensor_config,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class MR702TerminalParameterModule(
    name: String = "终端参数",
    desc: String = "本机触摸屏和上报规则设置",
    resID: Int = R.drawable.ic_device_data_center,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class DeviceOperationModule(
    name: String = "设备操作",
    desc: String = "时间校准、人工置数、召测等",
    resID: Int = R.drawable.ic_module_setting,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class NetworkCommunicationModule(
    name: String = "网络通信",
    desc: String = "无线、有线配置",
    resID: Int = R.drawable.ic_device_net_communicate,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class ManualSettingModule(
    name: String = "人工置数",
    desc: String = "人工录入历史采集数据",
    resID: Int = R.drawable.ic_manual_setting,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class DeviceLogUploadModule(
    name: String = "日志读取",
    desc: String = "可筛选、读取并上传平台",
    resID: Int = R.drawable.ic_log_upload,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class ParameterExportModule(
    name: String = "参数导出",
    desc: String = "当前配置参数，导出并上传",
    resID: Int = R.drawable.ic_parameter_export,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class ParameterImportModule(
    name: String = "参数导入",
    desc: String = "从平台获取参数配置导入",
    resID: Int = R.drawable.ic_parameter_import,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class ManualPhotoTakingModule(
    name: String = "手动拍照",
    desc: String = "确认摄像机已接入",
    resID: Int = R.drawable.ic_manual_photo_taking,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)
// </editor-fold>

class OneClickSilenceModule(
    name: String = "一键消音",
    desc: String = "立即停止语音播报",
    resID: Int = R.drawable.ic_parameter_export,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

//<editor-fold desc="MR702 功能模块">

class DataStorageModule(
    name: String = "数据存储",
    desc: String = "数据存储设置",
    resID: Int = R.drawable.ic_manual_photo_taking,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

// </editor-fold>
