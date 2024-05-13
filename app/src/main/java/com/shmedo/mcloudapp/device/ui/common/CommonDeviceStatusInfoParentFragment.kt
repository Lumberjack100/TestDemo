package com.shmedo.mcloudapp.device.ui.common

import androidx.fragment.app.Fragment
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.deviceinfo.BleDasBaseInfoFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.deviceinfo.BleDasCommunicationInfoFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.deviceinfo.BleDasSensorInfoFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo.DasBaseInfoFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo.DasCommunicationInfoFragment
import com.shmedo.mcloudapp.device.ui.lb20s.fragment.deviceinfo.LB20SBaseInfoFragment
import com.shmedo.mcloudapp.device.ui.lb20s.fragment.deviceinfo.LB20SGatewayInfoFragment
import com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo.M20BaseInfoFragment
import com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo.M20CommunicationInfoFragment
import com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo.M20SensorInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.LR200BaseInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.LR200SensorInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.UDProductSensorInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.UIProductSensorInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.UProductBaseInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.URProductSensorInfoFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/11
 * 描述： TODO
 */
class CommonDeviceStatusInfoParentFragment : BaseDeviceStatusInfoParentFragment() {
    override fun initData() {
        super.initData()
        mStates.productType.set(if (productType == ProductType.GNSS_M_1) "型号：M20 (单北斗)" else if (productType == ProductType.GNSS_M_2) "型号：M20 (全星座)" else "型号：${deviceInfo.deviceName}")
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
            ProductType.LR200 -> {//米度一体式裂缝计
                fragmentList.add(
                    LR200BaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    CommonCommunicationInfoFragment.newInstance().apply {
                        arguments = CommonCommunicationInfoFragment.newBundleArguments(
                            centerNum = 4,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            statusBarColor
                        )
                    }
                )
                fragmentList.add(
                    LR200SensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
            }

            ProductType.U_D_1,//水位
            ProductType.U_D_2 -> {//泥位
                fragmentList.add(
                    UProductBaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    CommonCommunicationInfoFragment.newInstance().apply {
                        arguments = CommonCommunicationInfoFragment.newBundleArguments(
                            centerNum = 3,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            statusBarColor
                        )
                    }
                )
                fragmentList.add(
                    UDProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
            }

            ProductType.U_I_1 -> {//倾斜仪
                fragmentList.add(
                    UProductBaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    CommonCommunicationInfoFragment.newInstance().apply {
                        arguments = CommonCommunicationInfoFragment.newBundleArguments(
                            centerNum = 3,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            statusBarColor
                        )
                    }
                )
                fragmentList.add(
                    UIProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
            }

            ProductType.U_R_1 -> {//一体化雨量计
                fragmentList.add(
                    UProductBaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    CommonCommunicationInfoFragment.newInstance().apply {
                        arguments = CommonCommunicationInfoFragment.newBundleArguments(
                            centerNum = 3,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            statusBarColor
                        )
                    }
                )
                fragmentList.add(
                    URProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
            }

            ProductType.M20, ProductType.GNSS_M_1, ProductType.GNSS_M_2 -> {//M20
                fragmentList.add(
                    M20BaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    M20CommunicationInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    M20SensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
            }

            ProductType.DAS -> {//M20
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
                    } else UDProductSensorInfoFragment.newInstance().apply { arguments = bundle }
                )
            }

            ProductType.LB20S -> {//预警广播
                fragmentList.add(
                    LB20SBaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    CommonCommunicationInfoFragment.newInstance().apply {
                        arguments = CommonCommunicationInfoFragment.newBundleArguments(
                            centerNum = 3,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            statusBarColor
                        )
                    }
                )
                fragmentList.add(
                    LB20SGatewayInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
            }

            else -> {
                fragmentList.add(
                    UProductBaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
                fragmentList.add(
                    CommonCommunicationInfoFragment.newInstance().apply {
                        arguments = CommonCommunicationInfoFragment.newBundleArguments(
                            centerNum = 3,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice,
                            statusBarColor
                        )
                    }
                )
                fragmentList.add(
                    URProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                )
            }
        }

        return fragmentList
    }
}