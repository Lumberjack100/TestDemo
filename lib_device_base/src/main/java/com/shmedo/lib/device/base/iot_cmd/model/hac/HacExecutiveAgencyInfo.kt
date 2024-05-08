package com.shmedo.lib.device.base.iot_cmd.model.hac

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/29/20 <br></br>
 * 描述：     ADME 执行机构参数
 */
data class HacExecutiveAgencyInfo (
    var datatype: String = "", //数据解算方式（0:顶部固定法，1底部固定法）
    var datareply: String = "",//数据应答（0:关闭，1:启用）
    var datainval: String = "", //数据读取间隔
    var compensatetime : String = "",//测量补偿时间
    var driveaddress : String = "",//电机驱动器地址
    var downspeed : String = "",//电机下放速度
    var downwaitetime : String = "",//下放等待时间
    var upspeed : String = "",//电机上拉速度
    var measpacing : String = "",//测量间距
    var meaintertime : String = "",//测量间隔时间
    var interval_compensation : String = "",//距离补偿区间h1
    var interval_fitting : String = "",//数据拟合区间h2
    var point_offset : String = "",//测点偏移距离h3
            
       
)