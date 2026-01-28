package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.ml101

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101 扩展串口参数配置实体 (RS485/RS232)
 *
 * 用于构建扩展串口参数设置指令 (md_cfgextserialparam, method=1)
 *
 * 指令示例（RS485）：
 * $cmd=md_cfgextserialparam&method=1&rs485_sw=1&rs485_mode=2&rs485_baud=9600
 * &rs485_paritybit=N&rs485_dataBit=8&rs485_stopbit=1
 *
 * 指令示例（RS232）：
 * $cmd=md_cfgextserialparam&method=1&rs232_sw=1&rs232_mode=2&rs232_baud=115200
 * &rs232_paritybit=N&rs232_dataBit=8&rs232_stopbit=1
 *
 * 字段说明：
 * @property method 功能码：1-设置
 * @property rs485_sw RS485 开关：0-开, 1-关（仅 RS485 使用）
 * @property rs485_mode RS485 功能模式：0-关闭输出, 1-日志输出, 2-数据透传（仅 RS485 使用）
 * @property rs485_baud RS485 波特率（仅 RS485 使用）
 * @property rs485_paritybit RS485 校验位（仅 RS485 使用）
 * @property rs485_dataBit RS485 数据位（仅 RS485 使用）
 * @property rs485_stopbit RS485 停止位（仅 RS485 使用）
 * @property rs232_sw RS232 开关：0-开, 1-关（仅 RS232 使用）
 * @property rs232_mode RS232 功能模式：0-关闭输出, 1-日志输出, 2-数据透传（仅 RS232 使用）
 * @property rs232_baud RS232 波特率（仅 RS232 使用）
 * @property rs232_paritybit RS232 校验位（仅 RS232 使用）
 * @property rs232_dataBit RS232 数据位（仅 RS232 使用）
 * @property rs232_stopbit RS232 停止位（仅 RS232 使用）
 */
@JsonClass(generateAdapter = true)
data class ML101ExtSerialParamEntity(
    /** 功能码：1-设置 */
    val method: String = "1",

    // ==================== RS485 参数 ====================

    /** RS485 开关：0-开, 1-关 */
    val rs485_sw: String = IOTConstants.NULL_KEY,

    /** RS485 功能模式：0-关闭输出, 1-日志输出, 2-数据透传 */
    val rs485_mode: String = IOTConstants.NULL_KEY,

    /** RS485 波特率：9600/57600/115200 */
    val rs485_baud: String = IOTConstants.NULL_KEY,

    /** RS485 校验位：N(none)/O(odd)/E(even)/M(mark)/S(space) */
    val rs485_paritybit: String = IOTConstants.NULL_KEY,

    /** RS485 数据位：5-8 */
    val rs485_dataBit: String = IOTConstants.NULL_KEY,

    /** RS485 停止位：1/1.5/2 */
    val rs485_stopbit: String = IOTConstants.NULL_KEY,

    // ==================== RS232 参数 ====================

    /** RS232 开关：0-开, 1-关 */
    val rs232_sw: String = IOTConstants.NULL_KEY,

    /** RS232 功能模式：0-关闭输出, 1-日志输出, 2-数据透传 */
    val rs232_mode: String = IOTConstants.NULL_KEY,

    /** RS232 波特率：9600/57600/115200 */
    val rs232_baud: String = IOTConstants.NULL_KEY,

    /** RS232 校验位：N(none)/O(odd)/E(even)/M(mark)/S(space) */
    val rs232_paritybit: String = IOTConstants.NULL_KEY,

    /** RS232 数据位：5-8 */
    val rs232_dataBit: String = IOTConstants.NULL_KEY,

    /** RS232 停止位：1/1.5/2 */
    val rs232_stopbit: String = IOTConstants.NULL_KEY
) {
    /**
     * 将实体转换为指令字符串
     *
     * 过滤掉值为 NULL_KEY 的字段，将剩余字段拼接为 key=value 格式
     *
     * @return 指令字符串，如 "method=1&rs485_sw=0&rs485_mode=2&..."
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }

    companion object {
        /**
         * 创建 RS485 配置实体
         *
         * @param sw 开关：0-开, 1-关
         * @param mode 功能模式：0-关闭输出, 1-日志输出, 2-数据透传
         * @param baud 波特率
         * @param paritybit 校验位
         * @param dataBit 数据位
         * @param stopbit 停止位
         * @return RS485 配置实体
         */
        fun createRS485Entity(
            sw: String,
            mode: String,
            baud: String,
            paritybit: String,
            dataBit: String,
            stopbit: String
        ): ML101ExtSerialParamEntity {
            return ML101ExtSerialParamEntity(
                method = "1",
                rs485_sw = sw,
                rs485_mode = mode,
                rs485_baud = baud,
                rs485_paritybit = paritybit,
                rs485_dataBit = dataBit,
                rs485_stopbit = stopbit
            )
        }

        /**
         * 创建 RS232 配置实体
         *
         * @param sw 开关：0-开, 1-关
         * @param mode 功能模式：0-关闭输出, 1-日志输出, 2-数据透传
         * @param baud 波特率
         * @param paritybit 校验位
         * @param dataBit 数据位
         * @param stopbit 停止位
         * @return RS232 配置实体
         */
        fun createRS232Entity(
            sw: String,
            mode: String,
            baud: String,
            paritybit: String,
            dataBit: String,
            stopbit: String
        ): ML101ExtSerialParamEntity {
            return ML101ExtSerialParamEntity(
                method = "1",
                rs232_sw = sw,
                rs232_mode = mode,
                rs232_baud = baud,
                rs232_paritybit = paritybit,
                rs232_dataBit = dataBit,
                rs232_stopbit = stopbit
            )
        }
    }
}
