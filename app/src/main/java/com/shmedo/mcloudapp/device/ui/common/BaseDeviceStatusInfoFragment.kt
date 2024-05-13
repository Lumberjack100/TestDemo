package com.shmedo.mcloudapp.device.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.formatDoubleValue
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentBaseDeviceStatusInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/5/13
 * 描述： TODO
 */
abstract class BaseDeviceStatusInfoFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentBaseDeviceStatusInfoBinding
    private lateinit var mStates: EmptyViewModel
    protected val iotParseManager: IOTParserManager by inject()
    protected val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.getDefault()))

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_base_device_status_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBaseDeviceStatusInfoBinding
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryStatusInfo()
        }
    }

    private fun initAdapter() {
        binding.recyclerview.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group_item)
            addType<DeviceStatusInfoBasicItem>(R.layout.item_device_status_info_basic_item)
            addType<DeviceStatusInfoSignalItem>(R.layout.item_device_status_info_signal_item)
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_device_status_info_group_item -> {
//                        Toaster.show("悬停条目")
                    }

                    else -> {

                    }
                }
            }

            // 可选项, 粘性监听器
            onHoverAttachListener = object : OnHoverAttachListener {
                override fun attachHover(v: View) {
                    ViewCompat.setElevation(v, 10F) // 悬停时显示阴影
                }

                override fun detachHover(v: View) {
                    ViewCompat.setElevation(v, 0F) // 非悬停时隐藏阴影
                }
            }
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    protected open fun queryStatusInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        val content: String = result.data
                        initStatusInfo(content)
                    }
                }
            }

            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        val content: String = result.data
                        initStatusInfo(content)
                    }
                }
            }

            else -> {}
        }
    }

    protected open fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val dataMap = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<Map<String, LB20SCurrentStateInfo>>(content)
                }
                if (dataMap.isNullOrEmpty()) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                val stateInfo = dataMap["000_1"]
                if (stateInfo == null) {
                    binding.refreshLayout.showEmpty()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()
                stateInfo.attach_data?.get("SN")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备SN", value = it))
                }
                stateInfo.attach_data?.get("imei")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备IMEI", value = it))
                }
                stateInfo.attach_data?.get("imsi")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备IMSI", value = it))
                }
                stateInfo.attach_data?.get("iccid")?.let {
                    groupList.add(DeviceStatusInfoBasicItem(name = "设备ICCID", value = it))
                }
                if (stateInfo.sw_version != IOTConstants.NULL_KEY) {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "固件版本",
                            value = stateInfo.sw_version
                        )
                    )
                }
                if (stateInfo.location != IOTConstants.NULL_KEY)
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "设备位置",
                            value = stateInfo.location
                        )
                    )
                if (stateInfo.ext_power_volt != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.ext_power_volt.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "外部电源电压",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "V",
                            colorRes = if (tempValue <= 5) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
                            )
                        )
                    )
                }
                groupList.add(
                    DeviceStatusInfoSignalItem(
                        name = "4G信号强度",
                        signalValue = stateInfo._4g_signal.let {
                            if (it <= 0)
                                it
                            else
                                it * 2 - 113
                        })
                )
                stateInfo.attach_data?.get("volumelevel")?.let { volumeLevelStr ->
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "音量等级",
                            value = if (volumeLevelStr == "0") "无" else if (volumeLevelStr == "1") "低" else if (volumeLevelStr == "2") "中" else if (volumeLevelStr == "3") "高" else "无"
                        )
                    )
                }
                groupList.add(DeviceStatusInfoGroupItem("太阳能控制器"))
                if (stateInfo.ext_power_volt != IOTConstants.NULL_KEY)
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "状态",
                            value = "正常",
                            colorRes = ColorUtils.getColor(R.color.device_online_platform)
                        )
                    )
                if (stateInfo.solar_volt != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.solar_volt.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "太阳能板电压",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "V",
                            colorRes = if (tempValue <= 5) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
                            )
                        )
                    )
                }
                if (stateInfo.battery_volt != IOTConstants.NULL_KEY) {
                    val tempValue = stateInfo.battery_volt.toDoubleOrNull() ?: 0.0
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "蓄电池电压",
                            value = formatDoubleValue(
                                tempValue,
                                decimalFormat,
                                "0"
                            ) + "V",
                            colorRes = if (tempValue <= 5) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                                R.color.text_color_3AD094
                            )
                        )
                    )
                }
                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

}