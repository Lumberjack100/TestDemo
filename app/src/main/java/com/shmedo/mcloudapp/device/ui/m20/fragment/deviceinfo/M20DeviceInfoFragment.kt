package com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo

import androidx.fragment.app.Fragment
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BaseRunningDeviceInfoFragment

class M20DeviceInfoFragment : BaseRunningDeviceInfoFragment() {
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
}