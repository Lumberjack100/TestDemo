package com.shmedo.mcloudapp.deviceconfig.ui.activity.das

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.shmedo.core.AppContants
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasCollectorSettingActivity
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasCollectorSettingFragment
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasCollectorSettingFragment

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/11/20<br></br>
 * 描述：     Das 采集器配置主页面
 */
class DasCollectorSettingActivity : BaseConfigFragmentContainerActivity() {
    private lateinit var collectorModel: String //采集器类型

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mToolbarTitle.text = "采集器配置"
    }

    override fun parseIntent() {
        super.parseIntent()
        collectorModel = intent.getStringExtra(AppContants.Extras.COLLECTOR_MODE) ?: ""
    }

    override fun initFragment(): Fragment =
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            NetDasCollectorSettingFragment.newInstance(deviceInfo)
        } else {
            BleDasCollectorSettingFragment.newInstance(collectorModel)
        }

    companion object {
        @JvmStatic
        fun startActivity(context: Context, deviceInfo: DeviceInfo?) {
            val intent = Intent(context, DasCollectorSettingActivity::class.java).apply {
                putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }

        @JvmStatic
        fun startActivity(context: Context, connectWay: Int, collectorModel: String?) {
            val intent = Intent(context, DasCollectorSettingActivity::class.java).apply {
                putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay)
                putExtra(AppContants.Extras.COLLECTOR_MODE, collectorModel)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }
}