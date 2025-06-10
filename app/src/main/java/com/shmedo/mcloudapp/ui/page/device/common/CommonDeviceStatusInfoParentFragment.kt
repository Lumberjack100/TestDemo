package com.shmedo.mcloudapp.ui.page.device.common

import androidx.fragment.app.Fragment
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.deviceinfo.BleDasBaseInfoFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.deviceinfo.BleDasCommunicationInfoFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.deviceinfo.BleDasSensorInfoFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.deviceinfo.DasBaseInfoFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.deviceinfo.DasCommunicationInfoFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.deviceinfo.DasSensorInfoFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/11
 * 描述： TODO
 */
class CommonDeviceStatusInfoParentFragment : BaseDeviceStatusInfoParentFragment() {
    override fun initData() {
        super.initData()
        mStates.productType.set("型号：${deviceInfo.deviceName}")
        when (productType) {
            ProductType.LB20S -> {
                tabs.clear()
                tabs.addAll(listOf("基本信息", "通讯状态", "网关信息"))
            }

            else -> {
                tabs.clear()
                tabs.addAll(listOf("基本信息", "通讯状态", "传感器信息"))
            }
        }
    }

    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        val fragmentList = mutableListOf<Fragment>()
        when (productType) {
            ProductType.COLLECTOR_R_1,
            ProductType.BHY,
            ProductType.DAS -> {
                fragmentList.add(
                    if (communicateWay is BleConnect) BleDasBaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                    else DasBaseInfoFragment.newInstance().apply { arguments = bundle }
                )
                fragmentList.add(
                    if (communicateWay is BleConnect) BleDasCommunicationInfoFragment.newInstance()
                        .apply {
                            arguments = bundle
                        } else DasCommunicationInfoFragment.newInstance()
                        .apply { arguments = bundle }
                )
                fragmentList.add(
                    if (communicateWay is BleConnect) BleDasSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    } else DasSensorInfoFragment.newInstance().apply { arguments = bundle }
                )
            }

            else -> {}
        }

        return fragmentList
    }
}