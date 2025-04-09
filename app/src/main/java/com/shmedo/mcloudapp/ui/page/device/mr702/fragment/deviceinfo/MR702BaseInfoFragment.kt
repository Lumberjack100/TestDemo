package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.deviceinfo

import android.util.Log
import com.drake.brv.utils.models
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber

class MR702BaseInfoFragment : BaseDeviceStatusInfoFragment() {
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel

    override fun initViewModel() {
        super.initViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun queryStatusInfo() {
        commandItems.clear()

        val entity = MRDeviceInfoEntity(pages = 1, label = 1)
        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询设备基本信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBaseInfo(result.data.baseInfo)
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initBaseInfo(stateInfo: MRBaseInfo) {
        launchWithViewLifecycle {
            try {
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "产品名称",
                    value = deviceInfo.productName,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "产品型号",
                    value = deviceInfo.deviceName,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备SN",
                    value = deviceInfo.deviceToken,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "MAC",
                    value = AppContants.PLACE_HOLDER_VALUE,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "软件版本",
                    value = stateInfo.ver,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "SIM卡号",
                    value = stateInfo.iccid,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "IMEI",
                    value = stateInfo.imei,
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "温度",
                    value = stateInfo.temp,
                    defaultValue = "0",
                    digit = 2,
                    unit = "℃",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromDouble(
                    groupList,
                    name = "湿度",
                    value = stateInfo.hum,
                    defaultValue = AppContants.PLACE_HOLDER_VALUE,
                    digit = 2,
                    unit = "%",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBatteryLevel(
                    groupList,
                    name = "外部供电电压",
                    value = stateInfo.volt,
                    defaultValue = AppContants.PLACE_HOLDER_VALUE,
                    downLimitValue = 5.0,
                    digit = 2,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "4G信号强度",
                    value = when (stateInfo.csq.toInt()) {
                        1 -> "优"
                        2 -> "良好"
                        3 -> "较差"
                        else -> "未知"
                    },
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "设备位置",
                    value = stateInfo.local,
                )

                deviceRequestViewModel.getDeviceDetailInfo(deviceInfo.deviceToken) { error: Throwable ->
                    addDeviceLogItem(Log.ERROR, error.errorMsg)
                }?.let { deviceDetailInfo ->
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "注册时间",
                        value = deviceDetailInfo.deviceInfo.createTime,
                    )
                }

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    companion object {
        fun newInstance() = MR702BaseInfoFragment()
    }
}