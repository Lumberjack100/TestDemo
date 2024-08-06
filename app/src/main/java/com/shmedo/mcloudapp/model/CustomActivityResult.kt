package com.shmedo.mcloudapp.model

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 * 创建时间：2024/4/28
 * 描述： TODO
 */
@Parcelize
data class CustomActivityResult(
    val requestCode: Int,
    val resultCode: Int,
    val data: Intent?
): Parcelable
