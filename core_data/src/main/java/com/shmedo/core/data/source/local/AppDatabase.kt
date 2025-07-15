package com.shmedo.core.data.source.local

import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shmedo.core.data.source.local.dao.LogItemDao
import com.shmedo.core.data.source.local.dao.LogSessionDao
import com.shmedo.core.data.source.local.entity.LogItem
import com.shmedo.core.data.source.local.entity.LogSession
import com.shmedo.core.data.source.local.entity.MonitoringDataEntity
import com.shmedo.core.data.source.local.entity.BuiltinCommandEntity
import com.shmedo.core.data.source.local.dao.MonitoringDataDao
import com.shmedo.core.data.source.local.dao.BuiltinCommandDao

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/27
 *
 * 描述： TODO
 *
 *
 */
@Database(
    entities = [
        LogSession::class,
        LogItem::class,
        MonitoringDataEntity::class,
        BuiltinCommandEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logSessionDao(): LogSessionDao
    abstract fun logItemDao(): LogItemDao
    abstract fun monitoringDataDao(): MonitoringDataDao
    abstract fun builtinCommandDao(): BuiltinCommandDao


    companion object {
        const val DATABASE_NAME = "mcloudApp.db"

        /**
         * 添加监测数据表
         */
        @VisibleForTesting
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS monitoring_data (
                        id TEXT PRIMARY KEY NOT NULL,
                        device_sn TEXT NOT NULL,
                        time_str TEXT NOT NULL,
                        sensor_data TEXT NOT NULL,
                        create_time INTEGER NOT NULL
                    )
                """)
            }
        }

        /**
         * 添加内置指令表
         */
        @VisibleForTesting
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS builtin_commands (
                        id TEXT PRIMARY KEY NOT NULL,
                        category TEXT NOT NULL,
                        name TEXT NOT NULL,
                        content TEXT NOT NULL,
                        remark TEXT NOT NULL DEFAULT '',
                        create_time INTEGER NOT NULL,
                        update_time INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}