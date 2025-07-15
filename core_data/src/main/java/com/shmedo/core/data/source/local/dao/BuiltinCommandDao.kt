package com.shmedo.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shmedo.core.data.source.local.entity.BuiltinCommandEntity

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令DAO
 */
@Dao
interface BuiltinCommandDao {

    @Query("SELECT * FROM builtin_commands ORDER BY category, name")
    suspend fun getAllCommands(): List<BuiltinCommandEntity>

    @Query("SELECT DISTINCT category FROM builtin_commands ORDER BY category")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM builtin_commands WHERE category = :category ORDER BY name")
    suspend fun getCommandsByCategory(category: String): List<BuiltinCommandEntity>

    @Query("SELECT * FROM builtin_commands WHERE id = :id")
    suspend fun getCommandById(id: String): BuiltinCommandEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: BuiltinCommandEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun batchInsert(commands: List<BuiltinCommandEntity>)

    @Update
    suspend fun updateCommand(command: BuiltinCommandEntity)

    @Delete
    suspend fun deleteCommand(command: BuiltinCommandEntity)

    @Query("DELETE FROM builtin_commands WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM builtin_commands")
    suspend fun deleteAllCommands()

    @Query("SELECT COUNT(*) FROM builtin_commands")
    suspend fun getCommandCount(): Int
} 