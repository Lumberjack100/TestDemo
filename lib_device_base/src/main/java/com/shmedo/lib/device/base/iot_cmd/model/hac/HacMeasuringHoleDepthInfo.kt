package com.shmedo.lib.device.base.iot_cmd.model.hac

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/22 <br></br>
 * 描述：     ADME AC10 孔深测量配置参数
 */
data class HacMeasuringHoleDepthInfo(
    var address: String = "", //MAC 地址
    var lowtbtss: String = "", //下放堵转检测（0:关闭，1:开启）
    var holelist: List<HacHoleAreaDepthInfo> = arrayListOf(), //孔深区域列表
)