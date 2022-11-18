package com.shmedo.mcloudapp.deviceconfig.ui.activity.das

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.shmedo.core.AppContants
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetFixedPointReportingFragment

class FixedPointReportingActivity : BaseConfigFragmentContainerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mToolbarTitle.text = "定时定点上报"
    }

    override fun initFragment(): Fragment =
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            NetFixedPointReportingFragment.newInstance(deviceInfo)
        } else {
            fragment
        }

    companion object {
        @JvmStatic
        fun startActivity(context: Context, deviceInfo: DeviceInfo?) {
            val intent = Intent(context, FixedPointReportingActivity::class.java).apply {
                putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }

}