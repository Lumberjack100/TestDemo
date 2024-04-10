package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/4/10
 * 描述： TODO
 */
class BleDasHomeFragmentViewModel : CommonDeviceHomeViewModel() {
    val isActived = NonNullObservableField(false)//设备是否激活
    val activeStateText = NonNullObservableField("已激活")

}