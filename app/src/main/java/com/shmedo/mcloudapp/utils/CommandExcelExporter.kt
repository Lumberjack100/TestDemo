package com.shmedo.mcloudapp.utils

import com.shmedo.mcloudapp.ui.viewmodel.state.CommandExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 指令执行结果Excel导出工具
 * 
 * @author: gonghe
 * @time: 2025/1/13
 * @desc: 将指令执行结果导出为Excel可读的CSV格式
 */
object CommandExcelExporter {
    
    /**
     * 导出指令执行结果到CSV文件
     * 
     * @param results 执行结果列表
     * @param deviceSn 设备SN号
     * @param outputDir 输出目录
     * @return 生成的文件，失败返回null
     */
    suspend fun exportToExcel(
        results: List<CommandExecutionResult>,
        deviceSn: String,
        outputDir: File
    ): File? = withContext(Dispatchers.IO) {
        try {
            // 确保输出目录存在
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            // 生成文件名
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val fileName = "指令配置结果_${deviceSn}_$timestamp.csv"
            val file = File(outputDir, fileName)
            
            // 写入CSV文件
            FileWriter(file).use { writer ->
                // 写入BOM，确保Excel正确识别UTF-8编码
                writer.write("\uFEFF")
                
                // 写入标题行
                writer.append("序号,设备SN号,下发指令,响应内容,执行状态,执行时间\n")
                
                // 写入数据
                results.forEachIndexed { index, result ->
                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date(result.timestamp))
                    
                    writer.append("${index + 1},")
                    writer.append("${escapeCsvField(deviceSn)},")
                    writer.append("${escapeCsvField(result.command)},")
                    writer.append("${escapeCsvField(result.response)},")
                    writer.append("${if (result.success) "成功" else "失败"},")
                    writer.append("$timeStr")
                    writer.append("\n")
                }
                
                // 添加统计信息
                val successCount = results.count { it.success }
                val failedCount = results.count { !it.success }
                writer.append("\n")
                writer.append("统计信息,,,,,\n")
                writer.append("总计,${results.size}条,成功,${successCount}条,失败,${failedCount}条\n")
            }
            
            Timber.i("Excel导出成功: $file")
            return@withContext file
            
        } catch (e: Exception) {
            Timber.e(e, "导出Excel失败")
            return@withContext null
        }
    }
    
    /**
     * 转义CSV字段中的特殊字符
     */
    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
    
    /**
     * 生成简化的执行报告（用于用户查看）
     */
    suspend fun generateSimpleReport(
        results: List<CommandExecutionResult>,
        deviceSn: String
    ): String = withContext(Dispatchers.Default) {
        val sb = StringBuilder()
        
        sb.appendLine("=== 指令执行报告 ===")
        sb.appendLine("设备SN: $deviceSn")
        sb.appendLine("执行时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        sb.appendLine()
        
        val successCount = results.count { it.success }
        val failedCount = results.count { !it.success }
        
        sb.appendLine("执行统计:")
        sb.appendLine("  总数: ${results.size}")
        sb.appendLine("  成功: $successCount")
        sb.appendLine("  失败: $failedCount")
        sb.appendLine()
        
        if (failedCount > 0) {
            sb.appendLine("失败指令:")
            results.filter { !it.success }.forEach { result ->
                sb.appendLine("  • ${result.command.take(50)}")
                sb.appendLine("    错误: ${result.response}")
            }
        }
        
        return@withContext sb.toString()
    }
}
