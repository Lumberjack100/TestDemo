package com.shmedo.mcloudapp.model

import android.os.Parcelable
import androidx.databinding.BaseObservable
import com.shmedo.core.commonlib.utils.AppContants
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 * 创建时间：2025/1/21
 * 描述：一体化倾斜震动监测仪测量数据项，用于在 RecyclerView 中展示
 */
@Parcelize
data class UIMeasureDataItem(
    var xInitialAngle: String = AppContants.PLACE_HOLDER_VALUE,  //x 轴初始角度
    var yInitialAngle: String = AppContants.PLACE_HOLDER_VALUE,  //y 轴初始角度
    var zInitialAngle: String = AppContants.PLACE_HOLDER_VALUE,  //z 轴初始角度
    var xAngle: String = AppContants.PLACE_HOLDER_VALUE,  //x 轴角度
    var yAngle: String = AppContants.PLACE_HOLDER_VALUE,  //y 轴角度
    var zAngle: String = AppContants.PLACE_HOLDER_VALUE,  //z 轴角度
    var xAcc: String = AppContants.PLACE_HOLDER_VALUE,  //x 轴加速度
    var yAcc: String = AppContants.PLACE_HOLDER_VALUE,  //y 轴加速度
    var zAcc: String = AppContants.PLACE_HOLDER_VALUE,  //z 轴加速度
) : Parcelable, BaseObservable() {

    fun refreshStatus(xInitialAngle: String, yInitialAngle: String, zInitialAngle: String, xAngle: String, yAngle: String, zAngle: String, xAcc: String, yAcc: String, zAcc: String) {
        this.xInitialAngle = xInitialAngle
        this.yInitialAngle = yInitialAngle
        this.zInitialAngle = zInitialAngle
        this.xAngle = xAngle
        this.yAngle = yAngle
        this.zAngle = zAngle
        this.xAcc = xAcc
        this.yAcc = yAcc
        this.zAcc = zAcc
        notifyChange()
    }
}