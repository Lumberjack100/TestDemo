package com.shmedo.core.data.repository

import com.shmedo.core.data.source.local.dao.BuiltinCommandDao
import com.shmedo.core.data.source.local.entity.BuiltinCommandEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令数据仓库
 */
class BuiltinCommandRepository(
    private val builtinCommandDao: BuiltinCommandDao
) {

    /**
     * 获取所有指令
     */
    suspend fun getAllCommands(): List<BuiltinCommandEntity> = withContext(Dispatchers.IO) {
        builtinCommandDao.getAllCommands()
    }

    /**
     * 获取所有分类
     */
    suspend fun getAllCategories(): List<String> = withContext(Dispatchers.IO) {
        builtinCommandDao.getAllCategories()
    }

    /**
     * 按分类获取指令
     */
    suspend fun getCommandsByCategory(category: String): List<BuiltinCommandEntity> = 
        withContext(Dispatchers.IO) {
            builtinCommandDao.getCommandsByCategory(category)
        }

    /**
     * 根据ID获取指令
     */
    suspend fun getCommandById(id: String): BuiltinCommandEntity? = withContext(Dispatchers.IO) {
        builtinCommandDao.getCommandById(id)
    }

    /**
     * 保存指令
     */
    suspend fun saveCommand(command: BuiltinCommandEntity) = withContext(Dispatchers.IO) {
        builtinCommandDao.insertCommand(command)
    }

    /**
     * 更新指令
     */
    suspend fun updateCommand(command: BuiltinCommandEntity) = withContext(Dispatchers.IO) {
        val updatedCommand = command.copy(updateTime = System.currentTimeMillis())
        builtinCommandDao.updateCommand(updatedCommand)
    }

    /**
     * 删除指令
     */
    suspend fun deleteCommand(command: BuiltinCommandEntity) = withContext(Dispatchers.IO) {
        builtinCommandDao.deleteCommand(command)
    }

    /**
     * 批量删除指令
     */
    suspend fun deleteCommands(commands: List<BuiltinCommandEntity>) = withContext(Dispatchers.IO) {
        val ids = commands.map { it.id }
        builtinCommandDao.deleteByIds(ids)
    }

    /**
     * 批量导入指令
     */
    suspend fun importCommands(commands: List<BuiltinCommandEntity>): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                val processedCommands = commands.map { command ->
                    command.copy(
                        id = UUID.randomUUID().toString(),
                        createTime = System.currentTimeMillis(),
                        updateTime = System.currentTimeMillis()
                    )
                }
                builtinCommandDao.batchInsert(processedCommands)
                Timber.i("成功导入 ${processedCommands.size} 条指令")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "导入指令失败")
                Result.failure(e)
            }
        }

    /**
     * 清空所有指令
     */
    suspend fun clearAllCommands() = withContext(Dispatchers.IO) {
        builtinCommandDao.deleteAllCommands()
    }

    /**
     * 获取指令总数
     */
    suspend fun getCommandCount(): Int = withContext(Dispatchers.IO) {
        builtinCommandDao.getCommandCount()
    }

    /**
     * 初始化默认指令
     */
    suspend fun initializeDefaultCommands() = withContext(Dispatchers.IO) {
        val count = builtinCommandDao.getCommandCount()
        if (count == 0) {
            val defaultCommands = createDefaultCommands()
            builtinCommandDao.batchInsert(defaultCommands)
            Timber.i("初始化 ${defaultCommands.size} 条默认指令")
        }
    }

    /**
     * 创建默认指令
     */
    private fun createDefaultCommands(): List<BuiltinCommandEntity> {
        return listOf(
            BuiltinCommandEntity(
                id = UUID.randomUUID().toString(),
                category = "通用指令",
                name = "获取设备状态",
                content = "\$cmd=getstatus",
                remark = "查询设备当前状态信息"
            ),
            BuiltinCommandEntity(
                id = UUID.randomUUID().toString(),
                category = "通用指令",
                name = "召测",
                content = "\$cmd=sample",
                remark = "召测设备数据"
            )
        )
    }
} 