package com.shmedo.lib.cmd.base.iot_cmd.model.gt600

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2026/01/08
 * 描述：GT600 设备状态信息
 *
 * 对应 md_getExstatus 指令的完整响应数据结构
 *
 * 示例 JSON:
 * ```json
 * {
 *   "base": {
 *     "sn": "26T1001GT1",
 *     "iccid": "89861117090210926796",
 *     "imei": "863930051778086",
 *     "version": "3.2.2.3",
 *     "oem": "CommNav/LONGSUNG",
 *     "volt": 11.20
 *   },
 *   "storage": {
 *     "ram": "26%",
 *     "flash": "42%",
 *     "tfcard": "55%"
 *   },
 *   "net": {
 *     "4g": "on",
 *     "csq": 26,
 *     "isp": "CHN-CT",
 *     "type": "4G",
 *     "socket1": 2,
 *     "socket2": 2,
 *     "socket3": 1,
 *     "socket4": 0
 *   }
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class GT600StatusInfo(
    val base: GT600BaseBean? = null,      // 基本信息
    val net: GT600NetBean? = null          // 网络信息
)
