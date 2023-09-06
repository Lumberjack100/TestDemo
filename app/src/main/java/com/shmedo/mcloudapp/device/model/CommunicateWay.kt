package com.shmedo.mcloudapp.device.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/6
 *
 * 描述： TODO
 *
 *
 */
@Parcelize
sealed class CommunicateWay : Parcelable

object NetPlatformConnect : CommunicateWay()

object BleConnect : CommunicateWay()

object TcpConnect : CommunicateWay()

object UsbSerial : CommunicateWay()
