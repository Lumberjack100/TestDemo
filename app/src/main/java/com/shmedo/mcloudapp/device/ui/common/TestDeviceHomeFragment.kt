package com.shmedo.mcloudapp.device.ui.common

import com.drake.brv.utils.models
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.CommonModule
import com.shmedo.mcloudapp.device.model.ConfigModule

/**
 * 创建者：gonghe
 * 创建时间：2024/3/8
 * 描述： TODO
 */
class TestDeviceHomeFragment : UniversalDeviceHomeFragment() {

    override fun updateConfigModuleData() {
        val moduleList = arrayListOf<ConfigModule>()
        moduleList.add(
            ConfigModule(
                CommonModule(
                    "指令调试",
                    "调试日志输出",
                    R.drawable.ic_device_running_info,
                    navId = R.id.action_global_to_commandDebug
                )
            )
        )

        binding.rvModule.models = moduleList
    }
}