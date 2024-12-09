package com.shmedo.core.data.source.local.entity

import android.util.Log

/**
 * 创建者：gonghe
 *
 *
 * 创建时间：2023/12/27
 *
 *
 * 描述： TODO
 */
object LogLevel {

    /**
     * Log entries with minor importance.
     */
    private const val VERBOSE = 0

    /**
     * Level used just for debugging purposes.
     */
    private const val DEBUG = 1

    /**
     * Default logging level for important entries.
     */
    private const val INFO = 2

    /**
     * Log entries with high importance.
     */
    private const val WARNING = 3

    /**
     * Log entries with very high importance, like errors.
     */
    private const val ERROR = 4

    /**
     * The Log [LogLevel] and [android.util.Log] are not compatible.
     * Level has additional [.APPLICATION] level, while Log the
     * [android.util.Log.ASSERT]. Also, the [android.util.Log.WARN] has
     * the same value as [.INFO]. Therefore, a translation needs to be done to
     * log a message using [android.util.Log] priorities.
     *
     * @param priority the [android.util.Log] priority.
     * @return the [LogLevel] matching given priority.
     */
    fun fromPriority(priority: Int): Int {
        return when (priority) {
            Log.VERBOSE -> VERBOSE
            Log.DEBUG -> DEBUG
            Log.INFO -> INFO
            Log.WARN -> WARNING
            Log.ERROR, Log.ASSERT -> ERROR
            else -> priority    // In case the Level was used, for example APPLICATION.
        }
    }

    fun getTag(level: Int): String {
        return when (level) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR, Log.ASSERT -> "E"
            else -> ""    // In case the Level was used, for example APPLICATION.
        }
    }
}
