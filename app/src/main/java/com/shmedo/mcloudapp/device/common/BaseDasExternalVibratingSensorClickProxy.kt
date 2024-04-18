package com.shmedo.mcloudapp.device.common

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/25 <br/>
 * 描述：     TODO
 */
open class BaseDasExternalVibratingSensorClickProxy : BaseClickProxy() {

    /**
     * 传感器类型选择
     */
    open fun onSensorSwitchClick() {}

    /**
     *
     */
    open fun onChannelSwitchClick() {}

    override fun onSubmitButtonClick() {
        TODO("Not yet implemented")
    }

}