package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import androidx.fragment.app.Fragment
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.ui.common.BaseDeviceStatusInfoParentFragment

class MR702DeviceStatusInfoParentFragment : BaseDeviceStatusInfoParentFragment() {
    override fun initData() {
        tabs.clear()
        tabs.addAll(listOf("基本信息", "运行状态", "接口状态", "模块状态"))
        super.initData()
    }

    override fun initTabFragment(): List<Fragment> {
        val bundle = BaseIOTDeviceFragment.newBundleArguments(
            productType,
            communicateWay,
            deviceInfo,
            bleDevice
        )
        return listOf<Fragment>(
            MR702BaseInfoFragment.newInstance().apply {
                arguments = bundle
            },
            MR702RunningStatusInfoFragment.newInstance().apply {
                arguments = bundle
            },
            MR702PortStatusInfoFragment.newInstance().apply {
                arguments = bundle
            },
            MR702ModuleStatusInfoFragment.newInstance().apply {
                arguments = bundle
            }
        )
    }
}