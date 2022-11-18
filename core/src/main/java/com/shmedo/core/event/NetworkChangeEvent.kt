package com.shmedo.core.event

import com.blankj.utilcode.util.NetworkUtils.NetworkType

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 创建者:   gonghe
 * 创建时间:  2019-10-18
 *
 */
class NetworkChangeEvent(//是否存在网络
    val isConnected: Boolean, val networkType: NetworkType
) : MessageEvent()