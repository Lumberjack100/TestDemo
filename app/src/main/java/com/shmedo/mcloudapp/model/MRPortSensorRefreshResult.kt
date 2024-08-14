package com.shmedo.mcloudapp.model

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/25 <br/>
 * 描述：     TODO
 */
sealed class MRPortSensorRefreshResult

data object MRRS485Port1 : MRPortSensorRefreshResult()
data object MRRS485Port2 : MRPortSensorRefreshResult()