package com.shmedo.mcloudapp.device.ui.m20s.fragment.deviceinfo

import androidx.fragment.app.Fragment
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BaseRunningDeviceInfoFragment
import com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo.M20BaseInfoFragment
import com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo.M20CommunicationInfoFragment
import com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo.M20SensorInfoFragment

/**
 * 创建者：gonghe
 * 创建时间：2024/4/25
 * 描述： TODO
 */
class M20SDeviceInfoFragment : BaseRunningDeviceInfoFragment() {
    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        return listOf<Fragment>(
            M20BaseInfoFragment.newInstance().apply {
                arguments = bundle
            },
            M20CommunicationInfoFragment.newInstance().apply {
                arguments = bundle
            },
            M20SensorInfoFragment.newInstance().apply {
                arguments = bundle
            }
        )
    }

    override fun initData() {
        super.initData()
        mStates.productType.set(if (productType == ProductType.GNSS_M_1) "型号：M20 (单北斗)" else "型号：M20 (全星座)")
    }
}