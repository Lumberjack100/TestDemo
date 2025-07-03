package com.shmedo.lib.ble.communicate.parser

import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import timber.log.Timber

/**
 * 指令终止符枚举
 */
enum class CommandTerminator(val bytes: ByteArray, val description: String) {
    IOT_COMMAND(byteArrayOf(38, 38), "物联网指令终止符(&&)"),
    MD_COMMAND(byteArrayOf(13, 10), "标准指令终止符(\\r\\n)")
}

/**
 * 优化后的数据包合并器
 *
 * 创建者：gonghe
 * 创建时间：2023/9/6
 * 描述：高效的BLE数据包合并处理，支持多种终止符和可配置日志
 */
class PacketMerger(
    private val terminators: List<CommandTerminator> = CommandTerminator.entries,
    private val enableDetailedLogging: Boolean = false,
    private val minPacketSize: Int = 2
) : DataMerger {

    companion object {
        private const val LOG_TAG = "PacketMerger"
    }

    /**
     * 合并数据包到输出流
     * A method that merges the last packet into the output message. All bytes from the lastPacket
     * are simply copied to the output stream until null is returned.
     * @param output     输出消息流，初始为空
     * @param lastPacket 最后一次读取/通知/指示操作接收到的数据
     * @param index      数据包索引，从0开始
     * @return true 如果消息完整，false 如果需要更多数据
     */
    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        // 空数据包处理
        if (lastPacket == null) {
            Timber.w("$LOG_TAG: 接收到空数据包")
            return false
        }

        // 条件性详细日志记录
        if (enableDetailedLogging || CommonMMKVOwner.isCommandDebugMode) {
            logPacketDetails(lastPacket, index)
        }

        // 写入数据到输出流
        output.write(lastPacket)

        // 调试模式快速返回
        if (CommonMMKVOwner.isCommandDebugMode) {
            Timber.d("$LOG_TAG: 调试模式，直接返回完整")
            return true
        }

        // 检查合并后的数据是否完整
        return checkDataCompleteness(output.toByteArray())
    }

    /**
     * 记录数据包详细信息
     */
    private fun logPacketDetails(lastPacket: ByteArray, index: Int) {
        try {
            val utf8Content = String(lastPacket, Charsets.UTF_8)
            Timber.d(
                "$LOG_TAG: 数据包[%d] - 长度=%d, \n内容=%s, \n内容(UTF-8)=%s",
                index,
                lastPacket.size,
                lastPacket.contentToString(),
                utf8Content
            )
        } catch (e: Exception) {
            Timber.w(e, "$LOG_TAG: 记录数据包详情时出错")
        }
    }

    /**
     * 检查数据完整性
     */
    private fun checkDataCompleteness(mergedData: ByteArray): Boolean {
        if (mergedData.size < minPacketSize) {
            Timber.v("$LOG_TAG: 数据长度不足 (%d < %d)", mergedData.size, minPacketSize)
            return false
        }

        // 高效的终止符检查
        return terminators.any { terminator ->
            mergedData.endsWith(terminator.bytes)
        }
    }

    /**
     * 优化的字节数组后缀检查扩展函数
     */
    private fun ByteArray.endsWith(suffix: ByteArray): Boolean {
        val suffixSize = suffix.size
        if (size < suffixSize) return false

        // 减少数组访问次数，提高性能
        val startIndex = size - suffixSize
        return (0 until suffixSize).all { i ->
            this[startIndex + i] == suffix[i]
        }
    }
}