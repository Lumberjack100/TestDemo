package com.shmedo.mcloudapp.device.model

import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   配置模块
 */
data class ConfigModule(
    val configModule: DeviceFunctionModule
)

sealed class DeviceFunctionModule(
    val name: String = "",
    val desc: String = "",
    val iconResId: Int = 0,
    val navId: Int = 0,
)

class RunningStatusModule(
    name: String = "状态",
    desc: String = "获取当前设备状态",
    resID: Int = R.drawable.ic_device_current_state,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class TerminalTimeModule(
    name: String = "时间",
    desc: String = "获取当前设备时间",
    resID: Int = R.drawable.ic_device_current_time,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class TelemetryDataModule(
    name: String = "遥测",
    desc: String = "远距离测量",
    resID: Int = R.drawable.ic_device_telemetry,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class RebootModule(
    name: String = "重启",
    desc: String = "重新启动当前设备",
    resID: Int = R.drawable.ic_device_reboot,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class CollectorConfigModule(
    name: String = "采集器配置",
    desc: String = "采集器参数配置",
    resID: Int = R.drawable.ic_device_collector_config,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class SensorConfigModule(
    name: String = "传感器配置",
    desc: String = "传感器参数配置",
    resID: Int = R.drawable.ic_device_sensor_config,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class DataCenterModule(
    name: String = "数据中心",
    desc: String = "连接平台参数配置",
    resID: Int = R.drawable.ic_device_data_center,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class BasicConfigModule(
    name: String = "基础配置",
    desc: String = "设备基础参数配置",
    resID: Int = R.drawable.ic_basic_config,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class AdvancedConfigModule(
    name: String = "高级配置",
    desc: String = "设备高级参数配置",
    resID: Int = R.drawable.ic_device_advanced_setting,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class AdvancedSettingsModule(
    name: String = "设置",
    desc: String = "高级设置",
    resID: Int = R.drawable.ic_device_setting,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class SetupWizard(
    name: String = "设置向导",
    desc: String = "一键配置",
    resID: Int = R.drawable.ic_setup_wizard,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

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
    resID: Int = R.drawable.ic_device_setting,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)

class NetworkCommunicationModule(
    name: String = "网络通信",
    desc: String = "无线、有线配置",
    resID: Int = R.drawable.ic_device_net_communicate,
    navId: Int = 0,
) : DeviceFunctionModule(name, desc, resID, navId)
