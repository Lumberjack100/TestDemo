package com.shmedo.lib.ble.communicate.parser

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import timber.log.Timber

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/6
 *
 * 描述： TODO
 *
 *
 */
abstract class CommandDataCallback : ProfileReadResponse(), CommandCallback {
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)
        if (data.size() < 2) {
            onInvalidDataReceived(device, data)
            return
        }

        val result = data.getStringValue(0)
        result?.let { cmdContent ->
            Timber.v(
                "接收数据: length=%s bytes;content: %s",
                data.size(),
                cmdContent
            )
            if (cmdContent.contains("$$")) {
                val tempCmdList = cmdContent.split("\r\n".toRegex()).filter { it.isNotEmpty() }
                if (tempCmdList.isNotEmpty()) {
                    val cmdList = mutableListOf<String>()
                    tempCmdList.forEach { it ->
                        Timber.v(
                            "接收数据(拆分后): length=%s bytes;content: %s",
                            it.toByteArray().size,
                            it
                        )
                        var cmd = it
                        val index = cmd.lastIndexOf("$$")
                        if (index != -1) {
                            cmd = cmd.substring(index)
                        }
                        cmdList.add(cmd)
                        //TODO
//                        onResponseReceived(device, cmd)
                    }
                    onResponseReceived(device, cmdResultList = cmdList)
                }
            } else if (cmdContent.contains("\$cmd")) {
                val index = cmdContent.lastIndexOf("\$cmd")
                onResponseReceived(device, cmdContent.substring(index))
            } else {
                onResponseReceived(device, cmdContent)
            }
        }
    }
}