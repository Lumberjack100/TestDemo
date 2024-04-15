package com.shmedo.lib.device.base.md_cmd.parser.das

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.VersionMessageInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/15
 * 描述： TODO
 *
 *
 * （1）SN号
 * （2）固件版本
 * （3）生产日期
 * 示例：
 * $$040,150000L,DAS-LF-V3.0.2,170817
 */
class MDVersionMessageInfoParser: MDCommandParser<VersionMessageInfo> {

    override fun parseInstance(values: List<String>): VersionMessageInfo {
        return VersionMessageInfo(
            productID = values.getOrNull(1) ?: "",
            firmwareVersion = values.getOrNull(2) ?: "",
            produceDate = values.getOrNull(3) ?: ""
        )
    }

    override fun commandType(): MDCommandType = MDCommandType.VERSION_MESSAGE

}