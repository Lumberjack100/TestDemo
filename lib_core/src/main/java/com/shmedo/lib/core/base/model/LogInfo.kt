package com.shmedo.lib.core.base.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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
data class LogInfo(
    @PrimaryKey
    val id: String =  UUID.randomUUID().toString(),
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "log_level") val logLevel: Int = LogLevel.DEBUG,
    var data: String = "",//
    @ColumnInfo(name = "create_date") var createDate: String,//
    @ColumnInfo(name = "create_time") var createTime: String,//
) : Parcelable
