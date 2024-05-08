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
            if (cmdContent.contains("\$\$")) {
                val tempCmdList = cmdContent.split("\r\n".toRegex()).filter { it.isNotEmpty() }
                if (tempCmdList.isNotEmpty()) {
                    tempCmdList.forEach { tempCmd ->
                        Timber.v(
                            "接收数据(拆分后): length=%s bytes;content: %s",
                            tempCmd.toByteArray().size,
                            tempCmd
                        )
                        var cmd = tempCmd
                        val index = cmd.lastIndexOf("\$\$")
                        if (index != -1) cmd = cmd.substring(index)
                        onResponseReceived(device, cmdResult = cmd)
                    }
                }
            } else if (cmdContent.contains("\$cmd")) {
                val index = cmdContent.lastIndexOf("\$cmd")
                if (index != -1)
                    onResponseReceived(device, cmdResult = cmdContent.substring(index))
            } else {
                onResponseReceived(device, cmdResult = cmdContent)
            }
        }
    }
}