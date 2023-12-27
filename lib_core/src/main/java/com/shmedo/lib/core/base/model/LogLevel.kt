package com.shmedo.lib.core.base.model

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
     * Level used just for debugging purposes. It has the lowest importance level.
     */
    const val DEBUG = 0

    /**
     * Log entries with minor importance.
     */
    const val VERBOSE = 1

    /**
     * Default logging level for important entries.
     */
    const val INFO = 5

    /**
     * Log entries level for applications.
     */
    const val APPLICATION = 10

    /**
     * Log entries with high importance.
     */
    const val WARNING = 15

    /**
     * Log entries with very high importance, like errors.
     */
    const val ERROR = 20

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
}
