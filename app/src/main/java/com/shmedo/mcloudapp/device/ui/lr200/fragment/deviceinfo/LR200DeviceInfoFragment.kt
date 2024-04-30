package com.shmedo.mcloudapp.device.ui.lr200.fragment.deviceinfo

import androidx.fragment.app.Fragment
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BaseRunningDeviceInfoFragment

class LR200DeviceInfoFragment : BaseRunningDeviceInfoFragment() {
    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        return listOf<Fragment>(
            LR200BaseInfoFragment.newInstance().apply {
                arguments = bundle
            },
            LR200CommunicationInfoFragment.newInstance().apply {
                arguments = bundle
            },
            LR200SensorInfoFragment.newInstance().apply {
                arguments = bundle
            }
        )
    }
}