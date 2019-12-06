package com.shmedo.mcloudapp.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.logging
 * 创建者:   gonghe
 * 创建时间:  2019-09-03
 *
 */
public class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message,
                       @Nullable Throwable t) {

    }
}

