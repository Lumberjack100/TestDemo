package com.shmedo.lib.cmd.base.iot_cmd.model.ml101

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101 扩展串口参数信息数据模型 (RS485/RS232)
 *
 * 对应 MD_CFG_EXT_SERIAL_PARAM (md_cfgextserialparam) 指令的响应数据结构
 *
 * 指令格式：
 * 发送：$cmd=md_cfgextserialparam&method=0
 *
 * 应答示例：
 * ```
 * $cmd=md_cfgextserialparam&method=0&rs485_sw=1&rs485_mode=2&rs485_baud=9600
 * &rs485_paritybit=N&rs485_dataBit=8&rs485_stopbit=1&rs232_sw=1&rs232_mode=2
 * &rs232_baud=115200&rs232_paritybit=N&rs232_dataBit=8&rs232_stopbit=1
 * ```
 *
 * 字段说明：
 * @property rs485_sw RS485 开关：0-开, 1-关
 * @property rs485_mode RS485 功能模式：0-关闭输出, 1-日志输出, 2-数据透传
 * @property rs485_baud RS485 波特率：9600/57600/115200
 * @property rs485_paritybit RS485 校验位：N(none)/O(odd)/E(even)/M(mark)/S(space)
 * @property rs485_dataBit RS485 数据位：5-8
 * @property rs485_stopbit RS485 停止位：1/1.5/2
 * @property rs232_sw RS232 开关：0-开, 1-关
 * @property rs232_mode RS232 功能模式：0-关闭输出, 1-日志输出, 2-数据透传
 * @property rs232_baud RS232 波特率：9600/57600/115200
 * @property rs232_paritybit RS232 校验位：N(none)/O(odd)/E(even)/M(mark)/S(space)
 * @property rs232_dataBit RS232 数据位：5-8
 * @property rs232_stopbit RS232 停止位：1/1.5/2
 */
data class ML101ExtSerialParamInfo(
    // ==================== RS485 参数 ====================

    /** RS485 开关：0-开, 1-关 */
    val rs485_sw: Int = 0,

    /** RS485 功能模式：0-关闭输出, 1-日志输出, 2-数据透传 */
    val rs485_mode: Int = 2,

    /** RS485 波特率：9600/57600/115200，默认 115200 */
    val rs485_baud: Int = 115200,

    /** RS485 校验位：N(none)/O(odd)/E(even)/M(mark)/S(space)，默认 N */
    val rs485_paritybit: String = "N",

    /** RS485 数据位：5-8，默认 8 */
    val rs485_dataBit: Int = 8,

    /** RS485 停止位：1/1.5/2，默认 1 */
    val rs485_stopbit: String = "1",

    // ==================== RS232 参数 ====================

    /** RS232 开关：0-开, 1-关 */
    val rs232_sw: Int = 0,

    /** RS232 功能模式：0-关闭输出, 1-日志输出, 2-数据透传 */
    val rs232_mode: Int = 2,

    /** RS232 波特率：9600/57600/115200，默认 115200 */
    val rs232_baud: Int = 115200,

    /** RS232 校验位：N(none)/O(odd)/E(even)/M(mark)/S(space)，默认 N */
    val rs232_paritybit: String = "N",

    /** RS232 数据位：5-8，默认 8 */
    val rs232_dataBit: Int = 8,

    /** RS232 停止位：1/1.5/2，默认 1 */
    val rs232_stopbit: String = "1"
) {
    /**
     * RS485 是否开启
     * 开关值：0-关, 1-开
     * @return true-开启, false-关闭
     */
    fun isRs485Enabled(): Boolean = rs485_sw == 1

    /**
     * RS232 是否开启
     * 开关值：0-关, 1-开
     * @return true-开启, false-关闭
     */
    fun isRs232Enabled(): Boolean = rs232_sw == 1

    /**
     * 获取 RS485 功能模式文本
     * @return 功能模式描述文本
     */
    fun getRs485ModeText(): String = when (rs485_mode) {
        0 -> "关闭输出"
        1 -> "日志输出"
        2 -> "数据透传"
        else -> "未知"
    }

    /**
     * 获取 RS232 功能模式文本
     * @return 功能模式描述文本
     */
    fun getRs232ModeText(): String = when (rs232_mode) {
        0 -> "关闭输出"
        1 -> "日志输出"
        2 -> "数据透传"
        else -> "未知"
    }

    /**
     * 获取校验位文本
     * @param paritybit 校验位代码
     * @return 校验位描述文本
     */
    companion object {
        fun getParityBitText(paritybit: String): String = when (paritybit.uppercase()) {
            "N" -> "NONE"
            "O" -> "ODD"
            "E" -> "EVEN"
            "M" -> "MARK"
            "S" -> "SPACE"
            else -> paritybit
        }
    }
}
