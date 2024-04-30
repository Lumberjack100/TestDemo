package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import androidx.fragment.app.Fragment
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BaseRunningDeviceInfoFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/4/25
 * 描述： TODO
 */
class UProductDeviceInfoFragment : BaseRunningDeviceInfoFragment() {
    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        return listOf<Fragment>(
            UProductBaseInfoFragment.newInstance().apply {
                arguments = bundle
            },
            UProductCommunicationInfoFragment.newInstance().apply {
                arguments = UProductCommunicationInfoFragment.newBundleArguments(
                    3,
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

                ProductType.U_R_1 -> {//雨量计
                    URProductSensorInfoFragment.newInstance().apply {
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