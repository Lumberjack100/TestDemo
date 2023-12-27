package com.shmedo.lib.core.data.dao

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blankj.utilcode.util.Utils
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
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
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionInfoDao(): SessionInfoDao

    abstract fun logInfoDao(): LogInfoDao

    private val _mIsDatabaseCreated = MutableResult<Boolean>()
    val mIsDatabaseCreated: Result<Boolean> = _mIsDatabaseCreated

    /**
     * Check whether the database already exists and expose it via [.getDatabaseCreated]
     */
    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated()
        }
    }

    private fun setDatabaseCreated() {
        _mIsDatabaseCreated.postValue(true)
    }

    companion object {
        @VisibleForTesting
        const val DATABASE_NAME = "mcloud-app-db"

        val INSTANCE: AppDatabase by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            buildDatabase().apply {
                updateDatabaseCreated(Utils.getApp())
            }
        }

        /**
         * Build the database. [Builder.build] only sets up the database configuration and
         * creates a new instance of the database.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase() =
            Room.databaseBuilder(Utils.getApp(), AppDatabase::class.java, DATABASE_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                .build()

    }


}