package com.shmedo.lib.cmd.base.iot_cmd.model.hac

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/7/20 <br></br>
 * 描述：     预警值
 */
data class HacWarningValue (
    var x1min: String = "", //一级预警 X 轴最小值
    var x1max: String = "", //一级预警 X 轴最大值
    var y1min: String = "", //一级预警 Y 轴最小值
    var y1max: String = "", //一级预警 Y 轴最大值
    var x2min: String = "", //二级预警 X 轴最小值
    var x2max: String = "", //二级预警 X 轴最大值
    var y2min: String = "", //二级预警 Y 轴最小值
    var y2max: String = "", //二级预警 Y 轴最大值
    var x3min: String = "", //三级预警 X 轴最小值
    var x3max: String = "", //三级预警 X 轴最大值
    var y3min: String = "", //三级预警 Y 轴最小值
    var y3max: String = "", //三级预警 Y 轴最大值
)