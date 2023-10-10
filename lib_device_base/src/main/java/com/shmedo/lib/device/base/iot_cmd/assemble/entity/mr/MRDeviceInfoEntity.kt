package com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/9 <br/>
 * 描述：     TODO
 */
class MRDeviceInfoEntity(
    private var pages: Int = 1,//页码 1-基础信息 2-运行状态 3-接口状态 4-模块状态
    private var label: Int = 1,//标签 运行状态(1-通信数据 2-运行数据) 接口状态(1-串口状态 2-模拟量接口状态 3-开关量状态)
) {

    override fun toString(): String {
        return "pages=$pages&label=$label"
    }
}