package com.shmedo.mcloudapp.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import com.shmedo.core.commonlib.utils.AppContants
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 * 创建时间：2025/1/21
 * 描述：一体式雨量计一体式雨量计测量数据项，用于在 RecyclerView 中展示
 */
@Parcelize
data class URMeasureDataItem(
    var rain24h: String = AppContants.PLACE_HOLDER_VALUE,  //24小时雨量
) : Parcelable, BaseObservable() {

    fun refreshStatus(rain24h: String) {
        this.rain24h = rain24h
        notifyChange()
    }
}