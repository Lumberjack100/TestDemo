package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/6
 *
 * 描述： TODO
 *
 *
 */
class MR702EquipmentOperationViewModel : CommandResponseViewModel() {
    //人工置数
    val isManualSetting = NonNullObservableField(false)//是否正在下发人工置数指令
    val observationTime = NonNullObservableField("")//观测时间
    val unit = NonNullObservableField("")//单位

    //参数导出
    val isParamExporting = NonNullObservableField(false)//是否正在下发参数导出指令
}