package com.shmedo.lib.core.base.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/11
 *
 * 描述： TODO
 *
 *
 */
@JsonClass(generateAdapter = true)
data class DeviceBackupInfo(
    val backupTime: String, // 2023-12-08 17:21:48
    val backupType: Int, // 1
    val backupTypeStr: String, // 人员手动
    val backupUserName: String, // 梅春阳
    val backupVersion: String, // 2341.12.08.17.20
    val companyID: Int, // 209
    val deviceID: Int, // 219725
    val fileAbsolutePath: String, // https://mdnetfile.shmedo.cn/202312/9a4d721a-a909-41db-a416-2a4900f2c802.bin
    val filePath: String, // local-202312-9a4d721a-a909-41db-a416-2a4900f2c802.bin
    val id: Int, // 375
    val note: String,
    val productID: Int, // 451
    val productName: String // 水文协议测试产品
)