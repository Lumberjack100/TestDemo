package com.shmedo.mcloudapp.device.ui.das.fragment.deviceinfo

import androidx.fragment.app.Fragment
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BaseRunningDeviceInfoFragment

class DasDeviceInfoFragment : BaseRunningDeviceInfoFragment() {
    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        return listOf<Fragment>(
            DasBaseInfoFragment.newInstance().apply {
                arguments = bundle
            },
            DasCommunicationInfoFragment.newInstance().apply {
                arguments = bundle
            },
            DasSensorInfoFragment.newInstance().apply {
                arguments = bundle
            }
        )
    }
}