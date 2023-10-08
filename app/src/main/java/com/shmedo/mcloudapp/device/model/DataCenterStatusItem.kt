package com.shmedo.mcloudapp.device.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   配置模块
 */
@Parcelize
data class DataCenterStatusItem(
    val centerid: Int = 1,
    val name: String = "",
    val status: String = "",
): Parcelable
