package com.shmedo.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令实体类
 */
@Entity(tableName = "builtin_commands")
data class BuiltinCommandEntity(
    @PrimaryKey
    val id: String,
    val category: String,                                    // 指令分类
    val name: String,                                        // 指令名称
    val content: String,                                     // 指令内容
    val remark: String = "",                                 // 备注
    @ColumnInfo(name = "create_time") val createTime: Long = System.currentTimeMillis(),  // 创建时间
    @ColumnInfo(name = "update_time") val updateTime: Long = System.currentTimeMillis(),   // 更新时间
)