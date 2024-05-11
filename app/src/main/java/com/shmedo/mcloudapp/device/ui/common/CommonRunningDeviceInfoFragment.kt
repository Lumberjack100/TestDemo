package com.shmedo.mcloudapp.device.ui.common

import androidx.fragment.app.Fragment
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.LR200BaseInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.LR200SensorInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.UDProductSensorInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.UIProductSensorInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.UProductBaseInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.UProductCommunicationInfoFragment
import com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo.URProductSensorInfoFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/5/11
 * 描述： TODO
 */
class CommonRunningDeviceInfoFragment : BaseRunningDeviceInfoFragment() {
    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        val centerNum = if (productType == ProductType.LR200) 4 else 3
        return listOf<Fragment>(
            when (productType) {
                ProductType.LR200 -> {//米度一体式裂缝计
                    LR200BaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                }

                else -> {
                    UProductBaseInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                }
            },
            UProductCommunicationInfoFragment.newInstance().apply {
                arguments = UProductCommunicationInfoFragment.newBundleArguments(
                    centerNum,
                    productType,
                    communicateWay,
                    deviceInfo,
                    bleDevice,
                    statusBarColor
                )
            },
            when (productType) {
                ProductType.U_D_1,//水位
                ProductType.U_D_2 -> {//泥位
                    UDProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                }

                ProductType.U_I_1 -> {//倾斜仪
                    UIProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                }

                ProductType.U_R_1 -> {//一体化雨量计
                    URProductSensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                }

                ProductType.LR200 -> {//米度一体式裂缝计
                    LR200SensorInfoFragment.newInstance().apply {
                        arguments = bundle
                    }
                }


                else -> URProductSensorInfoFragment.newInstance().apply {
                    arguments = bundle
                }
            }
        )
    }
}