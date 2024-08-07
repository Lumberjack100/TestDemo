package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/1/5
 * 描述： TODO
 */
@JsonClass(generateAdapter = true)
data class FirmWareInfo(
    val absolutePath: String, //可用下载连接  https://mdnetfile.shmedo.cn/202401/2211b00e-96dc-4f68-8965-41fddebae9d2.bin
    val companyID: Int, //  138
    val companyName: String, // 上海米度测控科技有限公司
    val fwMd5: String, //固件MD5值  83fc8d6764854f9d4f3d02078e9ccb56
    val fwName: String, //固件名称  MR701H-ZX-V23-4.1.10_M1
    val fwNote: String,//固件备注
    val fwPath: String, //固件路径  local-202401-2211b00e-96dc-4f68-8965-41fddebae9d2.bin
    val fwSize: Int, //固件大小(单位是字节)  266265
    val fwStatus: Int, //固件环境代码  0
    val fwVersion: String, // 4.1.10
    val id: Int, //固件ID  1409
    val productID: Int, //所属产品编号  556
    val productName: String, //所属产品名称  MR701H-沉降仪
    val productSource: Int, // 0
    val productToken: String, // MR701H-CJY
    val uploadTime: String, //上传时间, 精度: 秒 2024-01-04 10:12:04
    val uploadUser: String, //上传人  郭开启
    val uploadUserID: Int // 608
)