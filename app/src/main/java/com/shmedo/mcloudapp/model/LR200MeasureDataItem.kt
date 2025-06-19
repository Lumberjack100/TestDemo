package com.shmedo.mcloudapp.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import com.shmedo.core.commonlib.utils.AppContants
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 * 创建时间：2025/1/21
 * 描述：LR200 测量数据项，用于在 RecyclerView 中展示
 */
@Parcelize
data class LR200MeasureDataItem(
    var xAngle: String = AppContants.PLACE_HOLDER_VALUE,  //x 轴角度
    var yAngle: String = AppContants.PLACE_HOLDER_VALUE,  //y 轴角度
    var zAngle: String = AppContants.PLACE_HOLDER_VALUE,  //z 轴角度
    var lFInitial: String = AppContants.PLACE_HOLDER_VALUE,  //初始测量值
    var lFCurrent: String = AppContants.PLACE_HOLDER_VALUE,  //实时测量值
    var lFCumulative: String = AppContants.PLACE_HOLDER_VALUE,  //累计变化量
) : Parcelable, BaseObservable() {

    fun refreshStatus(xAngle: String, yAngle: String, zAngle: String, lFInitial: String, lFCurrent: String, lFCumulative: String) {
        this.xAngle = xAngle
        this.yAngle = yAngle
        this.zAngle = zAngle
        this.lFInitial = lFInitial
        this.lFCurrent = lFCurrent
        this.lFCumulative = lFCumulative
        notifyChange()
    }
}