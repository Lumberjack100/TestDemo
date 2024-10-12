package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AdmeHacMeasuringDataViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val equipmodel = NonNullObservableField("0")//电机工作标识  0：停止  1：正常 2: 异常
    val address = NonNullObservableField("")//MAC 地址
    val holeno = NonNullObservableField("")//孔号
    val areano = NonNullObservableField("")//区号
    val realHoleDepth = NonNullObservableField("")//测斜管孔深阈值
    val recommendHoleDepth = NonNullObservableField("")//推荐孔深

    val decentralizationWaitingTime = NonNullObservableField("")//下放等待时间(s)
    val dataSettlementMethod = NonNullObservableField("")//数据解算方式
    val isSingleWayTest = NonNullObservableField(false)//单向测量
    val isCheckReverse = NonNullObservableField(false)//测斜仪反转自检

    val isRunButtonEnable = NonNullObservableField(true)//测量按钮是否可用
    val runButtonText = NonNullObservableField("正向测量")//
}