package com.shmedo.mcloudapp.utils

import com.shmedo.core.data.source.local.entity.MonitoringDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Excel 导出工具类
 * 使用CSV格式导出数据，避免POI库的复杂依赖
 */
object ExcelExporter {
    
    /**
     * 导出监测数据到CSV文件（CSV可以被Excel直接打开）
     */
    suspend fun exportToExcel(
        dataList: List<MonitoringDataEntity>,
        deviceSn: String,
        outputDir: File
    ): File? = withContext(Dispatchers.IO) {
        try {
            // 生成文件名
            val fileName = "监测数据_${deviceSn}_${
                SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            }.csv"
            
            val file = File(outputDir, fileName)
            
            // 写入CSV文件
            FileWriter(file).use { writer ->
                // 写入BOM，确保Excel正确识别UTF-8编码
                writer.write("\uFEFF")
                
                // 写入标题行
                writer.append("序号,设备编号,时间,数据内容,备注\n")
                
                // 写入数据
                dataList.forEachIndexed { index, data ->
                    writer.append("${index + 1},")
                    writer.append("$deviceSn,")
                    writer.append("${data.timeStr},")
                    writer.append("\"${formatSensorData(data.sensorData)}\",")
                    writer.append("\n")
                }
            }
            
            return@withContext file
            
        } catch (e: Exception) {
            Timber.e(e, "导出Excel失败")
            return@withContext null
        }
    }
    
    /**
     * 格式化传感器数据
     */
    private fun formatSensorData(jsonData: String): String {
        return try {
            // 尝试格式化JSON数据
            val json = JSONObject(jsonData)
            // 将双引号替换为单引号，避免CSV格式问题
            json.toString().replace("\"", "'")
        } catch (e: Exception) {
            // 如果解析失败，返回原始数据
            jsonData.replace("\"", "'")
        }
    }
    
    /**
     * 导出为真正的Excel文件（需要添加Apache POI依赖）
     * 备用方案，如果需要更专业的Excel格式
     */
    suspend fun exportToRealExcel(
        dataList: List<MonitoringDataEntity>,
        deviceSn: String,
        outputDir: File
    ): File? = withContext(Dispatchers.IO) {
        // TODO: 如果需要真正的Excel格式，可以后续添加POI库实现
        // 目前使用CSV格式，Excel可以直接打开
        return@withContext exportToExcel(dataList, deviceSn, outputDir)
    }
} 