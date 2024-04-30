package com.shmedo.mcloudapp.device.ui.das.fragment.ble.deviceinfo

import androidx.fragment.app.Fragment
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BaseRunningDeviceInfoFragment

class BleDasDeviceInfoFragment : BaseRunningDeviceInfoFragment() {
    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        return listOf<Fragment>(
            BleDasBaseInfoFragment.newInstance().apply {
                arguments = bundle
            },
            BleDasCommunicationInfoFragment.newInstance().apply {
                arguments = bundle
            },
            BleDasSensorInfoFragment.newInstance().apply {
                arguments = bundle
            }
        )
    }
}