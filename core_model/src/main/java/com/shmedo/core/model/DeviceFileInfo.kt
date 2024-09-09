package com.shmedo.core.model

import com.squareup.moshi.JsonClass

/**
 * 创建者：gonghe
 * 创建时间：2024/9/3
 * 描述：
 */
@JsonClass(generateAdapter = true)
data class DeviceFileInfo(
    val fileID: String,//文件ID  "116154"
    val fileName: String,//文件名称  "20240903094150.jpeg"
    val fileType: String,//文件类型枚举 1.图片 2.文本 3.压缩包 4.二进制
    val fileTypeName: String,//文件类型名称   "图片"
    val fileSize: String,//文件大小   "63.36KB"
    val fileMd5: String,//文件MD5  "4b1ccc94a2a23f7f9ead4fc05fe23cca"
    val filePath: String,//文件真实地址  "https://mdnet-normal.oss-cn-hangzhou.aliyuncs.com/ali-oss-820c4790-6424-4db3-afb4-95630b5274ec?Expires=1725334143&OSSAccessKeyId=LTAI5DRlaZP1R7Kr&Signature=Nhe58BVPGj6CROQLM0vYf8aMoSU%3D"
    val sourceType: String,//文件来源 文件来源 0:设备上传 1:指令上传
    val uploadTime: String,//时间  "2024-09-03 09:42:17"
) : EmptyInfo()
