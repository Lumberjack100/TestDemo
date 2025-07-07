package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.utils.CRC16
import timber.log.Timber

class MR702RS485Port3AcousticOpticalAlarmParamViewModel : ViewModel() {

    //通用参数
    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")//数据位
    val checkBit = NonNullObservableField("")//校验位
    val stopBit = NonNullObservableField("")//停止位

    // 水位报警参数
    val waterLevelFunctionEnabled = NonNullObservableField(true) // 功能启用 0:关闭 1:开启
    val waterLevelRelayK = NonNullObservableField(true) // 继电器K接口控制 0:关闭 1:开启

    // 水位报警阈值
    val waterLevelTriggerValue1 = NonNullObservableField("") // 一级报警阈值（毫米）
    val waterLevelTriggerValue2 = NonNullObservableField("") // 二级报警阈值（毫米）
    val waterLevelTriggerValue3 = NonNullObservableField("") // 三级报警阈值（毫米）
    val waterLevelTriggerValue4 = NonNullObservableField("") // 四级报警阈值（毫米）

    // 水位报警持续时间
    val waterLevelHoldTime1 = NonNullObservableField("") // 一级报警持续时间（分钟）
    val waterLevelHoldTime2 = NonNullObservableField("") // 二级报警持续时间（分钟）
    val waterLevelHoldTime3 = NonNullObservableField("") // 三级报警持续时间（分钟）
    val waterLevelHoldTime4 = NonNullObservableField("") // 四级报警持续时间（分钟）

    // 水位报警间隔时间
    val waterLevelGapTime1 = NonNullObservableField("") // 一级报警间隔时间（分钟）
    val waterLevelGapTime2 = NonNullObservableField("") // 二级报警间隔时间（分钟）
    val waterLevelGapTime3 = NonNullObservableField("") // 三级报警间隔时间（分钟）
    val waterLevelGapTime4 = NonNullObservableField("") // 四级报警间隔时间（分钟）

    // 水位语音设置
    val waterLevelVoiceIndex1 = NonNullObservableField("") // 一级报警语音编号
    val waterLevelVoiceIndex2 = NonNullObservableField("") // 二级报警语音编号
    val waterLevelVoiceIndex3 = NonNullObservableField("") // 三级报警语音编号
    val waterLevelVoiceIndex4 = NonNullObservableField("") // 四级报警语音编号


    /**
     * 设置报警阈值列表
     */
    fun setWaterLevelWarnLevels(warnLevelString: String) {
        val levels = warnLevelString.split(",")
        if (levels.size >= 4) {
            waterLevelTriggerValue1.set(levels[0].toDoubleOrNull()?.div(100)?.toString() ?: "0")
            waterLevelTriggerValue2.set(levels[1].toDoubleOrNull()?.div(100)?.toString() ?: "0")
            waterLevelTriggerValue3.set(levels[2].toDoubleOrNull()?.div(100)?.toString() ?: "0")
            waterLevelTriggerValue4.set(levels[3].toDoubleOrNull()?.div(100)?.toString() ?: "0")
        }
    }

    // 辅助方法 - 转换报警阈值列表
    fun getWaterLevelWarnLevelString(): String {
        return "${
            waterLevelTriggerValue1.get().toDoubleOrNull()?.times(100)?.toString() ?: "0"
        },${
            waterLevelTriggerValue2.get().toDoubleOrNull()?.times(100)?.toString() ?: "0"
        },${
            waterLevelTriggerValue3.get().toDoubleOrNull()?.times(100)?.toString() ?: "0"
        },${waterLevelTriggerValue4.get().toDoubleOrNull()?.times(100)?.toString() ?: "0"}"
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
    fun generateVoiceCommand(broadcastTimes: String = "255", voiceNumber: String): String {
        // 根据模板生成指令
        val base = "A6030900090001040300"

        // 将播报次数转换为16进制, 并确保大写格式, 两位长度
        val broadcastTimesHex =
            broadcastTimes.toIntOrNull()?.toString(16)?.uppercase()?.padStart(2, '0') ?: "00"

        // 将语音编号转换为16进制, 并确保大写格式
        val voiceNumberHex =
            voiceNumber.toIntOrNull()?.toString(16)?.uppercase()?.padStart(2, '0') ?: "00"

        // 构建完整的指令字符串 (模板 + 语音编号)
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