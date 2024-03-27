package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeExecutiveAgencyViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isPullUpZeroSpeedSupport = NonNullObservableField(false)

    val measureMethodText = NonNullObservableField("")
    val measureMethod = NonNullObservableField(0)//测量方式 （0:实时测量，1:整时整点测量，2:定时定点测量)
    val dataSettlementMethod = NonNullObservableField("")//数据解算方式
    val dataResponse = NonNullObservableField("")//数据应答（0:关闭，1:启用）
    val waitingIntervalPerRound = NonNullObservableField("")//每轮等待时间
    val measurementIntervalPerRound = NonNullObservableField("")//每轮测量间隔
    val modifiedDate = NonNullObservableField("")//修改日期
    val intervalDays = NonNullObservableField("")//间隔时间
    val startTimePerRound = NonNullObservableField("")//每轮开始时间
    val dataReadingInterval = NonNullObservableField("")//数据读取间隔
    val measurementCompensationTime = NonNullObservableField("")//测量补偿时间
    val inclinometerTubeHoleDepth = NonNullObservableField("")//测斜管孔深(m)
    val motorDriveAddress = NonNullObservableField("")//电机驱动器地址
    val decentralizationSpeed = NonNullObservableField("")//下放速度(r/min)
    val decentralizationWaitingTime = NonNullObservableField("")//下放等待时间(min)
    val pullUpSpeed = NonNullObservableField("")//电机上拉速度
    val pullUpZeroSpeed = NonNullObservableField("")//上拉归零速度
    val measuringDistance = NonNullObservableField("")//测量间距
    val measurementIntervalTime = NonNullObservableField("")//测量间隔时间
    val measuringReferenceDepth = NonNullObservableField("")//测量基准深度
    val intervalCompensation = NonNullObservableField("")//管口安全距离h1
    val bottomSafetyDistance = NonNullObservableField("")//管底安全距离
    val intervalFitting = NonNullObservableField("")//数据拟合区间h2
    val pointOffset = NonNullObservableField("")//测点偏移距离h3
}