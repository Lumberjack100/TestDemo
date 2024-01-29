package com.shmedo.lib.ble.communicate.spec

import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import timber.log.Timber

class PacketMerger : DataMerger {

    /**
     * A method that merges the last packet into the output message. All bytes from the lastPacket
     * are simply copied to the output stream until null is returned.
     *
     * @param output     the stream for the output message, initially empty.
     * @param lastPacket the data received in the last read/notify/indicate operation.
     * @param index      an index of the packet, 0-based.
     * @return True,    if the message is complete, false if more data are expected.
     */
    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        if (lastPacket == null)
            return false

        Timber.e("merge: length=%s bytes;content: %s", lastPacket.size, String(lastPacket, Charsets.UTF_8))
//        Timber.e("merge: length=%s bytes;content: %s", lastPacket?.size ?: 0, lastPacket?.let { it.asList().toString() } ?: "Null")

        output.write(lastPacket)

        //每条响应命令结尾以&&(物联网指令)或\r\n(##指令)作为分隔符
        val mergeDataPacket = output.toByteArray()

        //每条响应命令结尾以&&(物联网指令)或\r\n(##指令)作为分隔符
        return  if(mergeDataPacket.size < 2)  false
        else
            (mergeDataPacket[mergeDataPacket.size - 1].toInt() == 38 && mergeDataPacket[mergeDataPacket.size - 2].toInt() == 38)
                    || (mergeDataPacket[mergeDataPacket.size - 1].toInt() == 10 && mergeDataPacket[mergeDataPacket.size - 2].toInt() == 13)
    }
}