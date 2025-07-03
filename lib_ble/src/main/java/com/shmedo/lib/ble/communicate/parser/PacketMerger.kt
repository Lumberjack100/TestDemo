package com.shmedo.lib.ble.communicate.parser

import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import timber.log.Timber

enum class CommandTerminator(val bytes: ByteArray) {
    IOT_COMMAND(byteArrayOf(38, 38)), //每条响应指令结尾以 && (物联网指令)
    MD_COMMAND(byteArrayOf(13, 10)) //每条响应指令结尾以 \r\n (##指令)
}

class PacketMerger(
    private val terminators: List<CommandTerminator> = CommandTerminator.entries
) : DataMerger {

    /**
     * A method that merges the last packet into the output message. All bytes from the lastPacket
     * are simply copied to the output stream until null is returned.
     *
     * @param output     the stream for the output message, initially empty.
     * @param lastPacket the data received in the last read/notify/indicate operation.
     * @param index      an index of the packet, 0-based.
     * @return      true if the message is complete, false if more data are expected.
     */
    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        if (lastPacket == null) return false

        // 打印 lastPacket 的详细信息
        Timber.e(
            "lastPacket bytes: bytes length=%s; bytes content: %s \n UTF-8 content: %s",
            lastPacket.size,
            lastPacket.asList().toString(),
            String(lastPacket, Charsets.UTF_8)
        )

        output.write(lastPacket)

        // 如果命令是调试模式，则直接返回 true
        if (CommonMMKVOwner.isCommandDebugMode) return true

        // 合并数据包
        val mergeDataPacket = output.toByteArray()
        if (mergeDataPacket.size < 2) return false

        return terminators.any { terminator ->
            mergeDataPacket.endsWith(terminator.bytes)
        }
    }

    private fun ByteArray.endsWith(suffix: ByteArray): Boolean {
        if (size < suffix.size) return false
        return suffix.indices.all { i ->
            this[size - suffix.size + i] == suffix[i]
        }
    }
}