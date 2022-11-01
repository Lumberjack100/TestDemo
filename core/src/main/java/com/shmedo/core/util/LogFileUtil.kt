package com.shmedo.core.util

import android.content.Context
import me.pqpo.librarylog4a.Log4a
import me.pqpo.librarylog4a.appender.FileAppender
import me.pqpo.librarylog4a.logger.AppenderLogger
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   dpc
 * 创建时间:  2019-12-18
 * 描述：    日志文件工具类
 */
object LogFileUtil {
    private const val DEFAULT_FORMAT = "yyyy_MM_dd"
    private const val SYSTEM = 1024
    private const val DIRECTORY_SIZE = 10

    /**
     * 创建 logcat文件
     *
     * @param file file
     * @return File
     */
    fun createLogFile(file: File, snNumber: String): File {
        if (file.exists()) { //存在
            if (file.isFile) {
                return createFile(file)
            } else if (file.isDirectory) {
                return createLogFile(
                    "${file.absolutePath}/mCloudLogFiles/$snNumber/",
                    fileName,
                    false
                )
            }
        } else {
            if (file.mkdirs()) {
                return createLogFile(file, snNumber)
            }
        }
        return file
    }

    /**
     * 创建log文件
     *
     * @param path       path
     * @param fileName   fileName
     * @param cleanCache cleanCache
     */
    private fun createLogFile(path: String, fileName: String, cleanCache: Boolean): File {
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // 是否删除缓存日志文件
        if (cleanCache) {
            computeSize(directory)
        }
        val file = File(directory, fileName)
        return createFile(file)
    }

    /**
     * 新建文件
     *
     * @param file
     * @return
     */
    private fun createFile(file: File): File {
        return if (file.exists()) {
            file
        } else {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            file
        }
    }

    /**
     * 获取缓存大小
     *
     * @param directory directory
     */
    private fun computeSize(directory: File) {
        var length = 0L
        if (directory.exists()) {
            for (file1 in directory.listFiles()) {
                length += file1.length()
            }
        }

        //限定大小 10M
        if (length / SYSTEM / SYSTEM >= DIRECTORY_SIZE) {
            for (file in directory.listFiles()) {
                file.delete()
            }
        }
    }

    /**
     * 文件名
     *
     * @return FileName
     */
    private val fileName: String
        private get() {
            val format: DateFormat = SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault())
            return format.format(Date(System.currentTimeMillis())) + ".txt"
        }

    fun getLogDir(context: Context): File {
        var log = context.getExternalFilesDir("logs") ?: File(context.filesDir, "logs")
        if (!log.exists()) {
            log.mkdir()
        }
        return log
    }

    @JvmStatic
    val logPath: String
        get() {
            var logPath = ""
            val logger = Log4a.getLogger()
            if (logger is AppenderLogger) {
                val appenderList = logger.appenderList
                for (appender in appenderList) {
                    if (appender is FileAppender) {
                        logPath = appender.logPath
                        break
                    }
                }
            }
            return logPath
        }
}