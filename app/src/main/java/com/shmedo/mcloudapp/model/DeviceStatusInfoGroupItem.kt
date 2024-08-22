package com.shmedo.mcloudapp.model

import androidx.databinding.BaseObservable
import com.drake.brv.item.ItemHover
import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceStatusInfoGroupItem(
    val name: String = "",
    val iconResId: Int = R.drawable.ic_mr702_device_info_serial_port_status,
    val hover: Boolean = true
) : BaseObservable(), ItemHover {

    override var itemHover: Boolean = hover

}