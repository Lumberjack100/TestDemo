package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/13
 * @desc: 功能开关参数设置实体类
 *
 * 对应指令: md_sensor
 * 查询发送: $cmd=md_sensor&method=0
 * 设置发送: $cmd=md_sensor&method=1&switch=0
 *
 * 参数说明:
 * - method: 方法类型
 *   - 0: 查询参数
 *   - 1: 设置参数
 * - switch: 功能开关 (设置时必填)
 *   - 0: 关
 *   - 1: 开
 */
@JsonClass(generateAdapter = true)
data class MdSensorConfigEntity(
    val method: String = "0",                    // 方法类型: 0-查询, 1-设置
    val switch: String = IOTConstants.NULL_KEY   // 功能开关: 0-关, 1-开
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: method=0 或 method=1&switch=1
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }

    companion object {
        /**
         * 创建查询参数实体
         */
        fun createQueryEntity(): MdSensorConfigEntity {
            return MdSensorConfigEntity(method = "0")
        }

        /**
         * 创建设置参数实体
         * @param switchValue 功能开关值: "0"-关, "1"-开
         */
        fun createSetEntity(switchValue: String): MdSensorConfigEntity {
            return MdSensorConfigEntity(method = "1", switch = switchValue)
        }
    }
}
