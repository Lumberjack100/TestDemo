package com.shmedo.mcloudapp.model

import com.drake.brv.item.ItemHover
import com.shmedo.core.model.EmptyInfo

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/12 <br/>
 * 描述：     TODO
 */
data class DeviceSensorDataHeaderItem(
    val name: String = "",
    val hover: Boolean = true
) : EmptyInfo(), ItemHover {

    override var itemHover: Boolean = hover
}