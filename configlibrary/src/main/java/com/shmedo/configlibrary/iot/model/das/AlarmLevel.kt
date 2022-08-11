package com.shmedo.configlibrary.iot.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/3/23 <br></br>
 * 描述：     声光报警器级别
 */
class AlarmLevel {
    var type: String = "" //传感器类型  1:雨量计,2:倾角计,3:主传感器

    var level1 : String = ""//无报警

    var level2 : String = ""//蓝色一级

    var level3 : String = ""//黄色二级

    var level4 : String = ""//橙色三级

    var level5 : String = ""//红色四级
}