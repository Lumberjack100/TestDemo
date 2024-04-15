package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasOtherInfoViewModel : ViewModel() {
    val errNo = NonNullObservableField("0")//错误码
    val solarvolt = NonNullObservableField("") //太阳能板电压
    val batvolt = NonNullObservableField("") //蓄电池电压
    val solarpwr = NonNullObservableField("") //太阳能板功率
    val loadpwr = NonNullObservableField("") //负载功率

    val inthErrNo = NonNullObservableField("0")//错误码
    val inthTemp = NonNullObservableField("") //
    val inthHumi = NonNullObservableField("") //

    val outthErrNo = NonNullObservableField("0")//错误码
    val outthTemp = NonNullObservableField("") //
    val outthHumi = NonNullObservableField("") //
}