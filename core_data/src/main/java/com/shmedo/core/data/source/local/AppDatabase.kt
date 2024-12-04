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
        LogItem::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logSessionDao(): LogSessionDao
    abstract fun logItemDao(): LogItemDao


    companion object {
        const val DATABASE_NAME = "mcloudApp.db"

        /**
         *
         */
        @VisibleForTesting
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }
    }
}