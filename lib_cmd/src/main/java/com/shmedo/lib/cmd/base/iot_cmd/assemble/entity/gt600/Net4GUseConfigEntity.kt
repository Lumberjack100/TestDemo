package com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.JsonClass

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: 4G 网络使用开关设置实体类
 *
 * 对应指令: md_setnet4guse
 * 发送示例: $cmd=md_setnet4guse&use=1
 *
 * 参数说明:
 * - use: 4G 功能开关状态
 *   - 0: 关闭4G功能
 *   - 1: 开启4G功能
 */
@JsonClass(generateAdapter = true)
data class Net4GUseConfigEntity(
    val use: String = IOTConstants.NULL_KEY  // 4G开关状态: 0-关闭, 1-开启
) {
    /**
     * 将实体转换为指令参数字符串
     * @return 格式如: use=1
     */
    fun toCommandString(): String {
        val jsonMap = MoshiUtil.toJsonMap(this)

        return jsonMap.entries
            .filterNot { it.value == IOTConstants.NULL_KEY }
            .joinToString("&") { "${it.key}=${it.value}" }
    }
}
