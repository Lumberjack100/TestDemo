package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/23 <br/>
 * 描述：     TODO
 */
@JsonClass(generateAdapter = true)
data class MRRS232Port2ParamEntity(
    var sw: String = "",//开关  1 开 0关
    var destaddr: String = IOTConstants.NULL_KEY,//目标卡号  北斗发送的目标地址，接收端的北斗卡号
    var baud: String = IOTConstants.NULL_KEY,//波特率  115200  支持2400-115200
    var databit: String = IOTConstants.NULL_KEY,//数据位   数字(5 6 7 8)
    var paritybit: String = IOTConstants.NULL_KEY,//校验位 0  NONE  1 ODD  2 EVEN  3 MARK 4 SPACE
    var stopbit: String = IOTConstants.NULL_KEY,//停止位   0: 1  1: 1.5  2: 2  或 数字(1 1.5 2)
    var linkid: String = IOTConstants.NULL_KEY,//数据中心号 1  链路号，0~4分别对应链路1~5
    var confirm: String = IOTConstants.NULL_KEY,//是否需要  1  1：不需要 2：需要
    var codetype: String = IOTConstants.NULL_KEY,//编码类型 3   1：汉字， 2：ASCII， 3：混编， 4：压缩汉字， 5：压缩ASCII， 目前只支持3，即混编
) {
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}