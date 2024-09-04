package com.shmedo.mcloudapp.model

import com.drake.brv.item.ItemHover

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/27
 *
 * 描述： 悬停头部
 *
 *
 */
data class HoverHeaderModel(
    val title: String = "",
) : ItemHover {
    override var itemHover: Boolean = true
}