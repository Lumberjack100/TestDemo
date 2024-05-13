package com.shmedo.mcloudapp.device.model

import androidx.databinding.BaseObservable
import com.drake.brv.item.ItemHover

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceStatusInfoGroupItem(
    val name: String = "",
) : BaseObservable(), ItemHover {

    override var itemHover: Boolean = true

}