package com.shmedo.lib.device.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/29/20 <br></br>
 * 描述：     ADME 执行机构参数
 */
data class AdmeExecutiveAgencyInfo(
    var meastype: String = "", //测量方式（0:实时测量，1:整时整点测量，2:定时定点测量）
    var datatype: String = "", //数据解算方式（0:顶部固定法，1底部固定法）
    var datareply: String = "",//数据应答（0:关闭，1:启用）
    var roundwaitetime: String = "", //每轮等待时间
    var roundmeasinval: String = "", //每轮测量间隔
    var updatedate: String = "",// 修改日期
    var invalday: String = "", //间隔天数
    var roundmeasstart: String = "", //每轮测量开始时间
    var datainval: String = "", //数据读取间隔
    var compensatetime: String = "", //测量补偿时间
    var driveaddress: String = "",//电机驱动器地址
    var downspeed: String = "",//电机下放速度
    var interdeep: String = "", //测斜管孔深
    var downwaitetime: String = "",//下放等待时间
    var upspeed: String = "",//电机上拉速度
    var measpacing: String = "", //测量间距
    var meaintertime: String = "", //测量间隔时间
    var meabaseth: String = "", //测量基准深度
    var dwonblocked: String = "",//下放堵转预判（0:关闭，1:开启）
    var untimenum: String = "",//堵转单位时间脉冲数
    var detectiontime: String = "",//堵转检测判断时间
    var detectionstart: String = "",//堵转检测起点
    var detectionend: String = "", //堵转检测终点
    var interval_compensation: String = "",//管口安全距离 h1
    var bottom_safe_distance: String = "", //管底安全距离
    var interval_fitting: String = "",//数据拟合区间h2
    var point_offset: String = "",//测点下移距离 h3
)