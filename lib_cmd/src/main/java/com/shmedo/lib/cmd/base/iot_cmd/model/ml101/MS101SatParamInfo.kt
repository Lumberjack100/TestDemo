package com.shmedo.lib.cmd.base.iot_cmd.model.ml101

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：MS101 卫通参数信息数据模型
 *
 * 对应 MD_CFG_SAT_PARAM (md_cfgsatparam) 指令的响应数据结构
 *
 * 指令格式：
 * 发送：$cmd=md_cfgsatparam&method=0
 *
 * 应答示例：
 * ```
 * $cmd=md_cfgsatparam&method=0&status=1&sn=0001836&cesq=-10&csq=-66&waitnum=240&cregmode=0&cpsmmode=0&cpsmlevel=9&svmdmode=0&cclrmode=0
 * ```
 *
 * 字段说明（页面展示）：
 * - status: 接入状态（0-已接入，1-未接入）
 * - sn: 产品序列号
 * - cesq: 卫星信号质量
 *   (1) 差：小于-10（不含-10）
 *   (2) 一般：-10~-6
 *   (3) 较好：-5~0
 *   (4) 好：大于 0（不含 0）
 *   (5) 无：等于-128
 * - csq: 环境噪声信号强度
 * - waitnum: 待发数据数量（值 0-480）
 */
@JsonClass(generateAdapter = true)
data class MS101SatParamInfo(
    /** 接入状态：0-已接入，1-未接入 */
    val status: Int = 0,

    /** 产品序列号 */
    val sn: String = IOTConstants.NULL_KEY,

    /** 卫星信号质量 */
    val cesq: String = IOTConstants.NULL_KEY,

    /** 环境噪声信号强度 */
    val csq: String = IOTConstants.NULL_KEY,

    /** 待发数据数量，值范围 0-480 */
    val waitnum: Int = 0
) {
    /**
     * 获取卫星信号质量等级描述
     *
     * 信号质量等级判断规则：
     * - 差：cesq < -10
     * - 一般：-10 <= cesq <= -6
     * - 较好：-5 <= cesq <= 0
     * - 好：cesq > 0
     * - 无：cesq == -128
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
     * 注意：status 字段的含义与直觉相反
     * - status = 0 表示未接入
     * - status = 1 表示已接入
     *
     * @return true-已接入，false-未接入
     */
    fun isConnected(): Boolean = status == 1
}
