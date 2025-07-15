package com.shmedo.core.data.mapper


import com.shmedo.core.data.source.local.entity.BuiltinCommandEntity
import com.shmedo.core.model.BuiltinCommandInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/3/27
 * 描述：
 */
fun List<BuiltinCommandInfo>.asEntity(): List<BuiltinCommandEntity> = map { item ->
    item.asEntity()
}

fun List<BuiltinCommandEntity>.asDomain(): List<BuiltinCommandInfo> = map { logItemEntity ->
    logItemEntity.asDomain()
}

fun BuiltinCommandInfo.asEntity(): BuiltinCommandEntity = BuiltinCommandEntity(
    id = id,
    category = category,
    name = name,
    content = content,
    remark = remark,
    createTime = createTime,
    updateTime = updateTime
)

fun BuiltinCommandEntity.asDomain(): BuiltinCommandInfo = BuiltinCommandInfo(
    id = id,
    category = category,
    name = name,
    content = content,
    remark = remark,
    createTime = createTime,
    updateTime = updateTime
)
