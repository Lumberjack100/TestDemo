package com.shmedo.lib.cmd.base.iot_cmd.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/16/20 <br></br>
 * 描述：     服务器(数据链路)编号
 */

@Parcelize
sealed class ServerNumber(val centerId: Int) : Parcelable

data object ServerOne : ServerNumber(1)
data object ServerTwo : ServerNumber(2)
data object ServerThree : ServerNumber(3)
data object ServerFour : ServerNumber(4)
data object ServerFive : ServerNumber(5)