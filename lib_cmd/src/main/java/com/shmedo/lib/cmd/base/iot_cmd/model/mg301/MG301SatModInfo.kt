package com.shmedo.lib.cmd.base.iot_cmd.model.mg301

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/19
 * 描述：MG301 卫通模块状态信息数据模型
 *
 * 对应 MD_GET_SATMOD (md_getsatmod) 指令的响应数据结构
 *
 * 指令格式：
 * 发送：$cmd=md_getsatmod
 *
 * 应答示例：
 * ```
 * $cmd=md_getsatmod&status=1&sn=0001836&cesq=-10&csq=-66&waitnum=200
 * ```
 *
 * 字段说明：
 * - status: 接入状态（0-未接入，1-已接入）
 * - sn: 产品序列号
 * - cesq: 卫星信号质量
 *   (1) 差：小于-10（不含-10）
 *   (2) 一般：-10~-6
 *   (3) 较好：-5~0
 *   (4) 好：大于 0（不含 0）
 *   (5) 无：等于-128
 * - csq: 环境噪声信号强度
 * - waitnum: 待发数据数量（值 1-480）
 */
@JsonClass(generateAdapter = true)
data class MG301SatModInfo(
    /** 接入状态：0-未接入，1-已接入 */
    val status: Int = 0,

    /** 产品序列号 */
    val sn: String = IOTConstants.NULL_KEY,

    /** 卫星信号质量 */
    val cesq: String = IOTConstants.NULL_KEY,

    /** 环境噪声信号强度 */
    val csq: String = IOTConstants.NULL_KEY,

    /** 待发数据数量，值范围 1-480 */
    val waitnum: Int = 0
) {
    /**
     * 获取卫星信号质量等级描述
     *
     * @return 信号质量等级文本：差/一般/较好/好/无
     */
    fun getSignalQualityLevel(): String {
        val cesqValue = cesq.toIntOrNull() ?: -128
        return when {
            cesqValue == -128 -> "无"
            cesqValue < -10 -> "差"
            cesqValue in -10..-6 -> "一般"
            cesqValue in -5..0 -> "较好"
            cesqValue > 0 -> "好"
            else -> "无"
        }
    }

    /**
     * 判断是否已接入
     *
     * @return true-已接入，false-未接入
     */
    fun isConnected(): Boolean = status == 1
}
