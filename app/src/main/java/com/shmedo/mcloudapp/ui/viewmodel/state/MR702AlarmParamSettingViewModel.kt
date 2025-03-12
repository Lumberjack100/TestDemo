package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.utils.CRC16
import timber.log.Timber

class MR702AlarmParamSettingViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    // 雨量报警参数
    val rainfallFunctionEnabled = NonNullObservableField(true) // 功能启用 0:关闭 1:开启
    val rainfallRelayK = NonNullObservableField(true) // 继电器K接口控制 0:关闭 1:开启
    val rainfallRS4853 = NonNullObservableField(true) // RS485-3接口 固定常开
    val rainfallHoldTime = NonNullObservableField("") // 报警持续时间（分钟）
    val rainfallGapTime = NonNullObservableField("") // 报警间隔时间（分钟）
    val rainfallClearGapTime = NonNullObservableField("") // 消警间隔时间（分钟）
    val rainfallBroadcastTimes = NonNullObservableField("3") // 播报次数

    // 雨量报警阈值
    val rainfallWarnLevel1 = NonNullObservableField("") // 一级报警阈值（毫米）
    val rainfallWarnLevel2 = NonNullObservableField("") // 二级报警阈值（毫米）
    val rainfallWarnLevel3 = NonNullObservableField("") // 三级报警阈值（毫米）
    val rainfallWarnLevel4 = NonNullObservableField("") // 四级报警阈值（毫米）

    // 雨量语音设置
    val rainfallVoiceIndex1 = NonNullObservableField("") // 一级报警语音编号
    val rainfallVoiceIndex2 = NonNullObservableField("") // 二级报警语音编号
    val rainfallVoiceIndex3 = NonNullObservableField("") // 三级报警语音编号
    val rainfallVoiceIndex4 = NonNullObservableField("") // 四级报警语音编号

    // 水位报警参数
    val waterLevelFunctionEnabled = NonNullObservableField(true) // 功能启用 0:关闭 1:开启
    val waterLevelRelayK = NonNullObservableField(true) // 继电器K接口控制 0:关闭 1:开启
    val waterLevelRS4853 = NonNullObservableField(true) // RS485-3接口 固定常开
    val waterLevelHoldTime = NonNullObservableField("") // 报警持续时间（分钟）
    val waterLevelGapTime = NonNullObservableField("") // 报警间隔时间（分钟）
    val waterLevelClearGapTime = NonNullObservableField("") // 消警间隔时间（分钟）
    val waterfallBroadcastTimes = NonNullObservableField("3") // 播报次数

    // 水位报警阈值
    val waterLevelWarnLevel1 = NonNullObservableField("") // 一级报警阈值（毫米）
    val waterLevelWarnLevel2 = NonNullObservableField("") // 二级报警阈值（毫米）
    val waterLevelWarnLevel3 = NonNullObservableField("") // 三级报警阈值（毫米）
    val waterLevelWarnLevel4 = NonNullObservableField("") // 四级报警阈值（毫米）

    // 水位语音设置
    val waterLevelVoiceIndex1 = NonNullObservableField("") // 一级报警语音编号
    val waterLevelVoiceIndex2 = NonNullObservableField("") // 二级报警语音编号
    val waterLevelVoiceIndex3 = NonNullObservableField("") // 三级报警语音编号
    val waterLevelVoiceIndex4 = NonNullObservableField("") // 四级报警语音编号


    /**
     * 设置报警阈值列表
     */
    fun setRainfallWarnLevels(warnLevelString: String) {
        val levels = warnLevelString.split(",")
        if (levels.size >= 4) {
            rainfallWarnLevel1.set(levels[0].toIntOrNull()?.div(100)?.toString() ?: "")
            rainfallWarnLevel2.set(levels[1].toIntOrNull()?.div(100)?.toString() ?: "")
            rainfallWarnLevel3.set(levels[2].toIntOrNull()?.div(100)?.toString() ?: "")
            rainfallWarnLevel4.set(levels[3].toIntOrNull()?.div(100)?.toString() ?: "")
        }
    }

    fun setWaterLevelWarnLevels(warnLevelString: String) {
        val levels = warnLevelString.split(",")
        if (levels.size >= 4) {
            waterLevelWarnLevel1.set(levels[0].toIntOrNull()?.div(100)?.toString() ?: "0")
            waterLevelWarnLevel2.set(levels[1].toIntOrNull()?.div(100)?.toString() ?: "0")
            waterLevelWarnLevel3.set(levels[2].toIntOrNull()?.div(100)?.toString() ?: "0")
            waterLevelWarnLevel4.set(levels[3].toIntOrNull()?.div(100)?.toString() ?: "0")
        }
    }

    // 辅助方法 - 转换报警阈值列表
    fun getRainfallWarnLevelString(): String {
        return "${
            rainfallWarnLevel1.get().toIntOrNull()?.times(100)?.toString() ?: "0"
        },${
            rainfallWarnLevel2.get().toIntOrNull()?.times(100)?.toString() ?: "0"
        },${
            rainfallWarnLevel3.get().toIntOrNull()?.times(100)?.toString() ?: "0"
        },${rainfallWarnLevel4.get().toIntOrNull()?.times(100)?.toString() ?: "0"}"
    }

    fun getWaterLevelWarnLevelString(): String {
        return "${
            waterLevelWarnLevel1.get().toIntOrNull()?.times(100)?.toString() ?: "0"
        },${
            waterLevelWarnLevel2.get().toIntOrNull()?.times(100)?.toString() ?: "0"
        },${
            waterLevelWarnLevel3.get().toIntOrNull()?.times(100)?.toString() ?: "0"
        },${waterLevelWarnLevel4.get().toIntOrNull()?.times(100)?.toString() ?: "0"}"
    }

    /**
     * 解析语音指令获取播报次数
     */
    fun getBroadcastTimes(voiceIndexString: String): String {
        if (voiceIndexString.isNotEmpty() && voiceIndexString.contains(",")) {
            val parts = voiceIndexString.split(",")
            if (parts.size >= 2 && parts[1].length >= 22) {
                // 从 A6030900090001040300A103390E 指令中提取 A1(语音编号) 03(播报次数) 部分
                val voiceCmd = parts[1]
                val broadcastTimesHex = voiceCmd.substring(22, 24)
                //转换为10进制
                val number = broadcastTimesHex.toIntOrNull(16) ?: 0

                return number.toString()
            }
        }
        return ""
    }

    /**
     * 解析语音指令获取编号
     */
    fun getVoiceIndexNumber(voiceIndexString: String): String {
        if (voiceIndexString.isNotEmpty() && voiceIndexString.contains(",")) {
            val parts = voiceIndexString.split(",")
            if (parts.size >= 2 && parts[1].length >= 22) {
                // 从 A6030900090001040300A103390E 指令中提取 A1(语音编号) 03(播报次数) 部分
                val voiceCmd = parts[1]
                val voiceNumberHex = voiceCmd.substring(20, 22)
                //转换为10进制
                val number = voiceNumberHex.toIntOrNull(16) ?: 0

                return number.toString()
            }
        }
        return ""
    }

    // 辅助方法 - 根据语音编号生成指令
    fun generateVoiceCommand(broadcastTimes: String, voiceNumber: String): String {
        // 根据模板生成指令
        val base = "A6030900090001040300"

        // 将播报次数转换为16进制, 并确保大写格式, 两位长度
        val broadcastTimesHex = broadcastTimes.toIntOrNull()?.toString(16)?.uppercase()?.padStart(2, '0') ?: "00"

        // 将语音编号转换为16进制, 并确保大写格式
        val voiceNumberHex = voiceNumber.toIntOrNull()?.toString(16)?.uppercase()?.padStart(2, '0') ?: "00"

        // 构建完整的命令字符串 (模板 + 语音编号)
        val fullCommand = base + voiceNumberHex + broadcastTimesHex

        // 计算 CRC-16 校验码
        val crc = CRC16.getCRC16(fullCommand)

        Timber.d("Voice Index Command: $fullCommand$crc")

        // 返回最终指令
        return "14,${fullCommand}${crc}"
    }

    // 固定的语音报警应答指令
    val defaultRespCommand = "13,A603080009000104030098D9FB"
}