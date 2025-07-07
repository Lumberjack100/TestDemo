package com.shmedo.lib.ble.communicate.parser

import android.bluetooth.BluetoothDevice
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import timber.log.Timber

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/6
 *
 * 描述：优化后的指令数据回调处理器，使用策略模式和完善的错误处理
 */
abstract class CommandDataCallback(
    private val parsers: List<CommandParserStrategy> = listOf(
        IoTCommandParser(),
        StandardCommandParser(),
        DefaultCommandParser()
    )
) : ProfileReadResponse(), CommandCallback {
    
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)
        
        try {
            // 数据有效性检查
            if (!isValidData(device, data)) return
            
            // 提取字符串内容
            val cmdContent = extractStringContent(data) ?: return
            
            // 记录接收日志
            logReceivedData(data.size(), cmdContent)
            
            // 调试模式快速返回
            if (CommonMMKVOwner.isCommandDebugMode) {
                onResponseReceived(device, cmdResult = cmdContent)
                return
            }
            
            // 使用策略模式解析和处理指令
            parseAndProcessCommands(device, cmdContent)
            
        } catch (e: Exception) {
            Timber.e(e, "处理接收数据时发生异常")
        }
    }
    
    /**
     * 检查数据是否有效
     */
    private fun isValidData(device: BluetoothDevice, data: Data): Boolean {
        if (data.size() < 2 && !CommonMMKVOwner.isCommandDebugMode) {
            onInvalidDataReceived(device, data)
            return false
        }
        return true
    }
    
    /**
     * 提取字符串内容
     */
    private fun extractStringContent(data: Data): String? {
        return try {
            data.getStringValue(0)
        } catch (e: Exception) {
            Timber.e(e, "提取字符串内容失败")
            null
        }
    }
    
    /**
     * 记录接收数据日志
     */
    private fun logReceivedData(dataSize: Int, content: String) {
        Timber.v("接收数据: length=%d bytes; content: %s", dataSize, content)
    }
    
    /**
     * 使用策略模式解析和处理指令
     */
    private fun parseAndProcessCommands(device: BluetoothDevice, cmdContent: String) {
        val parser = findSuitableParser(cmdContent)
        
        try {
            val commands = parser.parse(cmdContent)
            
            if (commands.isEmpty()) {
                Timber.w("解析器未产生任何指令: %s", cmdContent)
                return
            }
            
            commands.forEach { cmd ->
                if (cmd.isNotBlank()) {
                    Timber.v("处理解析后指令: %s", cmd)
                    onResponseReceived(device, cmdResult = cmd)
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "解析指令失败: %s", cmdContent)
        }
    }
    
    /**
     * 查找合适的解析器
     */
    private fun findSuitableParser(content: String): CommandParserStrategy {
        return parsers.firstOrNull { it.canParse(content) } 
            ?: parsers.last() // 使用默认解析器作为后备
    }
}