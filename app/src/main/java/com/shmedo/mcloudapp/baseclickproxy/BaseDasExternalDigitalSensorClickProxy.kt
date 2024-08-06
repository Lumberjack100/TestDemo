package com.shmedo.mcloudapp.baseclickproxy

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/25 <br/>
 * 描述：     TODO
 */
open class BaseDasExternalDigitalSensorClickProxy : BaseClickProxy() {

    /**
     * 阵列测斜仪选择物模型
     */
    open fun onModelSwitchClick() {}

    /**
     * 选择子雷达传感器类型
     */
    open fun onChildSensorTypeSwitchClick() {}

    open fun onTriggerTipBtnClick() {}

    /**
     * 修正值提示按钮
     */
    open fun onCorrectTipBtnClick() {}

    /**
     * 扩展1提示按钮
     */
    open fun onExtension1TipBtnClick() {}

    /**
     * 扩展2提示按钮
     */
    open fun onExtension2TipBtnClick() {}

    /**
     * 扩展3提示按钮
     */
    open fun onExtension3TipBtnClick() {}

    /**
     * 扩展4提示按钮
     */
    open fun onExtension4TipBtnClick() {}

    /**
     * 扩展5提示按钮
     */
    open fun onExtension5TipBtnClick() {}

    /**
     * 扩展1按钮
     */
    open fun onExtension1ButtonClick() {}

    override fun onSubmitButtonClick() {
        TODO("Not yet implemented")
    }

}