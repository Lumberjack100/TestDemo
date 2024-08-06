package com.shmedo.lib.cmd.base.iot_cmd.model.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/10 <br/>
 * 描述：     TODO
 */
data class MRDeviceInfo(
    val pages: String = "1",
    val label: String = "1",
    val baseInfo: MRBaseInfo = MRBaseInfo(),
    val communicationData: MRCommunicationData = MRCommunicationData(),
    val runningData: MRRunningData = MRRunningData(),
    val moduleStatusInfo: MRModuleStatusInfo = MRModuleStatusInfo(),
    val interfaceStatusInfo: MRInterfaceStatusInfo = MRInterfaceStatusInfo(),
    val ioStatusInfo: MRIOStatusInfo = MRIOStatusInfo(),
)