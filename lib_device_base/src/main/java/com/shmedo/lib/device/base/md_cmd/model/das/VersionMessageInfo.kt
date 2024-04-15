package com.shmedo.lib.device.base.md_cmd.model.das

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： 版本信息实体类
 *
 * （1）SN号
 * （2）固件版本
 * （3）生产日期
 * 示例：
 * $$040,150000L,DAS-LF-V3.0.2,170817
 */
data class VersionMessageInfo(
    val productID: String = "",
    val firmwareVersion: String = "",
    val produceDate: String = "",
)
