package com.shmedo.mcloudapp.device.common

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
open class BaseAdmeExecutiveAgencyClickProxy : BaseClickProxy() {
    open fun onMeasureMethodClick() {}

    open fun onDataSettlementMethodClick() {}

    open fun onDataResponseClick() {}

    open fun onMeasIntervalPerRoundsClick() {}

    override fun onSubmitButtonClick() {}
}