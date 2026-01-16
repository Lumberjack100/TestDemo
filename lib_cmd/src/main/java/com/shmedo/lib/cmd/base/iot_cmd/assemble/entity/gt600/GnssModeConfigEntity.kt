package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/14
 * @desc: GNSS工作模式（站点类型）设置实体类
 *
 * 对应指令: md_gnssmode
 * 查询发送: $cmd=md_gnssmode&method=0
 * 设置发送: $cmd=md_gnssmode&method=1&station=0
 *
 * 参数说明:
 * - method: 方法类型
 *   - 0: 查询参数
 *   - 1: 设置参数
 * - station: 站点类型 (设置时必填)
 *   - 0: 基站
 *   - 1: 测站
 */
@JsonClass(generateAdapter = true)
data class GnssModeConfigEntity(
    val method: String = "0",                        // 方法类型: 0-查询, 1-设置
    val station: String = IOTConstants.NULL_KEY      // 站点类型: 0-基站, 1-测站
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: method=0 或 method=1&station=1
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
        fun createQueryEntity(): GnssModeConfigEntity {
            return GnssModeConfigEntity(method = "0")
        }

        /**
         * 创建设置参数实体
         * @param stationValue 站点类型值: "0"-基站, "1"-测站
         */
        fun createSetEntity(stationValue: String): GnssModeConfigEntity {
            return GnssModeConfigEntity(method = "1", station = stationValue)
        }
    }
}
