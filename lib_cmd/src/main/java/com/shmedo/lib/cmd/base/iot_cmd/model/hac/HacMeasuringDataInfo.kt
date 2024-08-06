package com.shmedo.lib.cmd.base.iot_cmd.model.hac

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/11 <br></br>
 * 描述：     ADME AC10 数据测量配置参数
 */
data class HacMeasuringDataInfo(
    var equipmodel: String = "", //电机工作标识 0：停止 1：开始测量 2: 异常
    var address: String = "", //MAC 地址
    var downwaitetime: String = "", //下放等待时间
    var datatype: String = "", //数据解算方式（0:顶部固定法，1底部固定法）
    var onewaytest: String = "", //单向测量 0 :关闭 1:开启
    var checkreverse: String = "", //反转自检
    var currhole: String = "1", //当前孔号
    var holelist: String = "",
)