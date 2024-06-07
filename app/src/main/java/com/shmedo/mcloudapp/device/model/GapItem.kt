package com.shmedo.mcloudapp.device.model

import com.blankj.utilcode.util.ConvertUtils

/**
 * 创建者：gonghe
 * 创建时间：2024/5/15
 * 描述： TODO
 */
data class GapItem(
    val colorRes: Int = 0,
    val height: Int = ConvertUtils.dp2px(10f),
)
