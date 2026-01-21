package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.ml101

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：MS101 卫通参数配置实体
 *
 * 用于构建卫通参数设置指令 (md_cfgsatparam, method=1)
 *
 * 指令示例：
 * $cmd=md_cfgsatparam&method=1&cregmode=0&cpsmmode=0&cpsmlevel=9&svmdmode=0&cclrmode=0
 *
 * 字段说明：
 * @param method 功能码：1-设置
 * @param cregmode 卫星联网状态上报模式：0-关闭, 1-开启, 2-开启并上报数据帧号
 * @param cpsmmode 卫星休眠模式：0-不休眠, 1-定时休眠, 2-自动休眠
 * @param cpsmlevel 休眠等级：1-9, cpsmmode=0 时无效
 * @param svmdmode 数据存储溢出处理：0-停止接收, 1-循环覆盖
 * @param cclrmode 待发数据处理：-1-全部删除, 0-不删除, 1~480-指定删除帧号
 */
@JsonClass(generateAdapter = true)
data class MS101SatParamEntity(
    /** 功能码：1-设置 */
    val method: String = "1",

    /**
     * 卫星联网状态上报模式
     * - 0: 关闭联网状态上报
     * - 1: 开启联网状态上报
     * - 2: 开启联网数据上报，并在数据发送成功后上报数据帧编号
     */
    val cregmode: String = IOTConstants.NULL_KEY,

    /**
     * 卫星休眠模式
     * - 0: 不休眠
     * - 1: 定时休眠
     * - 2: 自动（有待发数据时定时休眠，无待发数据时一直休眠）
     */
    val cpsmmode: String = IOTConstants.NULL_KEY,

    /**
     * 卫星休眠模式等级
     * 取值范围: 1-9
     * 当 cpsmmode=0 时无效
     */
    val cpsmlevel: String = IOTConstants.NULL_KEY,

    /**
     * 数据存储溢出处理
     * - 0: 停止接收（存储满后不再接收新数据）
     * - 1: 循环覆盖（存储器满后，覆盖最早的数据）
     */
    val svmdmode: String = IOTConstants.NULL_KEY,

    /**
     * 删除存储区待发数据
     * - -1: 全部删除
     * - 0: 不删除
     * - 1~480: 指定删除，删除第 m 帧待发数据
     */
    val cclrmode: String = IOTConstants.NULL_KEY
) {
    /**
     * 将实体转换为指令字符串
     *
     * 过滤掉值为 NULL_KEY 的字段，将剩余字段拼接为 key=value 格式
     *
     * @return 指令字符串，如 "method=1&cregmode=0&cpsmmode=0&cpsmlevel=9&svmdmode=0&cclrmode=0"
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
