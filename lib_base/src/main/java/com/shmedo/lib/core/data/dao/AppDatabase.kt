package com.shmedo.lib.core.data.dao

import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.SessionInfo

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
        SessionInfo::class,
        LogInfo::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionInfoDao(): SessionInfoDao
    abstract fun logInfoDao(): LogInfoDao


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