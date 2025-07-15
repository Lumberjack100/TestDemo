package com.shmedo.mcloudapp.utils

import com.shmedo.core.model.BuiltinCommandInfo
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令 CSV 解析器
 */
object BuiltinCommandCsvParser {
    
    /**
     * 从 InputStream 解析 CSV 文件
     */
    fun parseFromInputStream(inputStream: InputStream): List<BuiltinCommandInfo> {
        val commands = mutableListOf<BuiltinCommandInfo>()
        
        try {
            BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                var line: String?
                var lineNumber = 0
                
                while (reader.readLine().also { line = it } != null) {
                    lineNumber++
                    
                    // 跳过空行和注释行
                    if (line.isNullOrBlank() || line!!.startsWith("#")) {
                        continue
                    }
                    
                    // 跳过标题行
                    if (lineNumber == 1 && isHeaderLine(line!!)) {
                        continue
                    }
                    
                    try {
                        val command = parseLine(line!!)
                        if (command != null) {
                            commands.add(command)
                        }
                    } catch (e: Exception) {
                        Timber.w("解析第 $lineNumber 行失败: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "解析 CSV 文件失败")
        }
        
        return commands
    }
    
    /**
     * 解析单行 CSV 数据
     */
    private fun parseLine(line: String): BuiltinCommandInfo? {
        val columns = parseCsvLine(line)
        
        if (columns.size < 3) {
            Timber.w("CSV 行格式不正确，至少需要3列: $line")
            return null
        }
        
        return BuiltinCommandInfo(
            id = UUID.randomUUID().toString(),
            category = columns[0].trim(),
            name = columns[1].trim(),
            content = columns[2].trim(),
            remark = if (columns.size > 3) columns[3].trim() else ""
        )
    }
    
    /**
     * 解析 CSV 行，处理逗号和引号
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            
            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // 双引号转义
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        
        result.add(current.toString())
        return result
    }
    
    /**
     * 检查是否为标题行
     */
    private fun isHeaderLine(line: String): Boolean {
        val lowerLine = line.lowercase()
        return lowerLine.contains("指令分类") || 
               lowerLine.contains("category") ||
               lowerLine.contains("指令名称") ||
               lowerLine.contains("name")
    }
    
    /**
     * 生成 CSV 模板内容
     */
    fun generateTemplate(): String {
        return """指令分类,指令名称,指令内容,备注
通用指令,获取设备状态,${'$'}cmd=getstatus,查询设备当前状态信息
通用指令,获取工作模式,${'$'}cmd=getworkmode,查询设备当前工作模式
通用指令,召测,${'$'}cmd=sample,召测设备数据
通用指令,重启设备,${'$'}cmd=restart_device,重启设备
调试指令,开启调试模式,${'$'}cmd=md_setlogoutput&level=2&mode=1,开启设备调试日志输出
调试指令,关闭调试模式,${'$'}cmd=md_setlogoutput&level=0&mode=0,关闭设备调试日志输出
MD指令,查询状态,##9050,MD指令查询设备状态
MD指令,低功耗模式,##9100${','} 1,设置设备进入低功耗模式"""
    }
    
    /**
     * 将指令列表转换为 CSV 格式
     */
    fun commandsToCSV(commands: List<BuiltinCommandInfo>): String {
        val sb = StringBuilder()
        sb.appendLine("指令分类,指令名称,指令内容,备注")
        
        commands.forEach { command ->
            sb.appendLine("${escapeCsvField(command.category)},${escapeCsvField(command.name)},${escapeCsvField(command.content)},${escapeCsvField(command.remark)}")
        }
        
        return sb.toString()
    }
    
    /**
     * 转义 CSV 字段
     */
    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
} 