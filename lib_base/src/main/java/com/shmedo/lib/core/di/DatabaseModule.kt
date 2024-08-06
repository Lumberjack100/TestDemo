package com.shmedo.lib.core.di

import androidx.room.Room
import com.shmedo.lib.core.data.dao.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * 创建者：gonghe
 * 创建时间：2024/3/22
 * 描述： TODO
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
//            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    single { get<AppDatabase>().sessionInfoDao() }
    single { get<AppDatabase>().logInfoDao() }
}