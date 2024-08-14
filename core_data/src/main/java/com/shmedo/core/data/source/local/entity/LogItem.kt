package com.shmedo.core.data.source.local.entity

import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/27
 *
 * 描述： TODO
 *
 *
 */
@kotlinx.parcelize.Parcelize
@Entity(tableName = "log_info")
data class LogItem(
    @PrimaryKey
    val id: String = "",
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "log_level") val logLevel: Int = Log.DEBUG,
    var data: String = "",//
    @ColumnInfo(name = "create_date") var createDate: String,//
    @ColumnInfo(name = "create_time") var createTime: String,//
) : Parcelable
