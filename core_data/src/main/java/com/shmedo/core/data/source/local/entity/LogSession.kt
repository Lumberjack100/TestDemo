package com.shmedo.core.data.source.local.entity

import android.os.Parcelable
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
@Entity(tableName = "sessions")
data class LogSession(
    @PrimaryKey
    val id: String = "",
    var key: String = "",//
    var name: String = "",//
    @ColumnInfo(name = "create_by") var createBy: String = "",//创建人
    @ColumnInfo(name = "create_date") var createDate: String,//
    @ColumnInfo(name = "create_time") var createTime: String,//
) : Parcelable
