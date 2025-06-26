package com.shmedo.core.commonlib.mmkv


/**
 * 创建者：gonghe
 * 创建时间：2024/3/26
 * 描述： TODO
 */
object ADMEMMKVOwner : MMKVOwner(mmapID = "adme_settings") {
    var autoLastMotorDropSpeed by mmkvString(default = "")//ADME 自动测孔深上一次电机下放速度

    var manualLastMotorPullUpSpeed by mmkvString(default = "")//ADME 手动测孔深上一次电机上拉速度

    var manualLastMotorPullUpDistance by mmkvString(default = "")//ADME 手动测孔深上一次电机上拉距离

    var manualLastMotorDropSpeed by mmkvString(default = "")//ADME 手动测孔深上一次电机下放速度

    var manualLastMotorDropDistance by mmkvString(default = "")//ADME 手动测孔深上一次电机下放距离



}
