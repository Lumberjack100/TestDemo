package com.shmedo.lib.ble.communicate.parser

import android.bluetooth.BluetoothDevice

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/6
 *
 * 描述：优化后的指令响应处理器，支持多种结果类型
 */
class CommandResponse(
    parsers: List<CommandParserStrategy> = listOf(
        StandardIOTCommandParser(),
        MDCommandParser(),
        DefaultCommandParser()
    ),
) : CommandDataCallback(parsers) {
    /**
     * 最新的响应内容
     */
    var latestResponse: String = ""
        private set

    override fun onResponseReceived(device: BluetoothDevice, cmdResult: String) {
        latestResponse = cmdResult
    }
}