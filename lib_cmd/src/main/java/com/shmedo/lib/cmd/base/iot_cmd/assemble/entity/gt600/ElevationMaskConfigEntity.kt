package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/13
 * @desc: 截至高度角参数设置实体类
 *
 * 对应指令: md_elevation_mask
 * 查询发送: $cmd=md_elevation_mask&method=0
 * 设置发送: $cmd=md_elevation_mask&method=1&angle=15
 *
 * 参数说明:
 * - method: 方法类型
 *   - 0: 查询参数
 *   - 1: 设置参数
 * - angle: 截至高度角 (设置时必填)
 *   - 取值范围: 5-90°，间隔5°
 *   - 默认值: 15°
 */
@JsonClass(generateAdapter = true)
data class ElevationMaskConfigEntity(
    val method: String = "0",                    // 方法类型: 0-查询, 1-设置
    val angle: String = IOTConstants.NULL_KEY    // 截至高度角: 5-90°
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: method=0 或 method=1&angle=15
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
        fun createQueryEntity(): ElevationMaskConfigEntity {
            return ElevationMaskConfigEntity(method = "0")
        }

        /**
         * 创建设置参数实体
         * @param angleValue 截至高度角值: "5"-"90"，间隔5
         */
        fun createSetEntity(angleValue: String): ElevationMaskConfigEntity {
            return ElevationMaskConfigEntity(method = "1", angle = angleValue)
        }
    }
}
