package com.shmedo.mcloudapp.device.ui.m20.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentM20BaseInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRRunningDataItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.M20BaseInfoViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class M20BaseInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM20BaseInfoBinding
    private lateinit var mStates: M20BaseInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_base_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM20BaseInfoBinding
        initRefresh()
        initRunningDataAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryBaseInfo()
        }
    }

    private fun initRunningDataAdapter() {
        binding.rvRunningData.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<MRRunningDataItem>(R.layout.item_mr702_device_info_running_data)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
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
                        val errMsg = "查询基本信息出错: ${result.message}"
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

    private fun initStatusInfo(content: String) {
        val decimalFormat = DecimalFormat("#.###")
        try {
            val commonCurrentStateInfo =
                MoshiUtil.fromJson<CommonCurrentStateInfo>(content) ?: return

            initRunningData(commonCurrentStateInfo)
            mStates.wrapStateInfo.set(commonCurrentStateInfo)
            mStates.wrapStateInfo.get().apply {
                inner_power_volt = commonCurrentStateInfo.inner_power_volt.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                ext_power_volt = commonCurrentStateInfo.ext_power_volt.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                solar_volt = commonCurrentStateInfo.solar_volt.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                battery_volt = commonCurrentStateInfo.battery_volt.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                supply_power = commonCurrentStateInfo.supply_power.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                consume_power = commonCurrentStateInfo.consume_power.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                temp = commonCurrentStateInfo.temp.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                humidity = commonCurrentStateInfo.humidity.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                temp_out = commonCurrentStateInfo.temp_out.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
                humidity_out = commonCurrentStateInfo.humidity_out.toDoubleOrNull()
                    ?.let {
                        decimalFormat.format(it)
                    } ?: "--"
            }
            mStates.wrapStateInfo.notifyChange()

            checkDeviceNormal(commonCurrentStateInfo.self_check)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun checkDeviceNormal(selfCheck: String) {
        //"self_check": "GPS:1,eMMC:1,4g:1,RTC:1,solar485:0,G-Sensor:1,BT:1,GNSS:1,QMC:0,SHT21:1,product_time:20240411"
        //解析 self_check,根据逗号分隔，取出各个传感器的状态
        val selfCheckArray = selfCheck.split(",")
        for (item in selfCheckArray) {
            val sensor = item.split(":")
            when (sensor[0].uppercase()) {
                "BT" -> {//0：关闭或异常 1：正常未连接 2：正常已连接
                    if (sensor[1] == "0") {
                        mStates.isDeviceNormal.set("0")
                        break
                    }
                }

                "PRODUCT_TIME" -> {

                }

                else -> {
                    if (sensor[1] == "0") {
                        mStates.isDeviceNormal.set("0")
                        break
                    }
                }
            }
        }

        mStates.isDeviceNormal.set("1")
    }

    private fun initRunningData(commonCurrentStateInfo: CommonCurrentStateInfo) {
        try {
            val list = mutableListOf<MRRunningDataItem>()
            if (commonCurrentStateInfo.worktime.isNotEmpty() && commonCurrentStateInfo.worktime != "--") {
                list.add(
                    MRRunningDataItem(
                        "运行时间(小时)",
                        decimalFormat.format(commonCurrentStateInfo.worktime.toDouble() / 3600)
                    )
                )
            }

            if (commonCurrentStateInfo.emmc_storage.isNotEmpty() && commonCurrentStateInfo.emmc_storage != "--") {
                val storages = commonCurrentStateInfo.emmc_storage.split(",")
                if (storages.size == 2) {
                    val free = storages[0].toDoubleOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: ""

                    val total = storages[1].toDoubleOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: ""

                    list.add(
                        MRRunningDataItem(
                            "存储状态",
                            "${free}/${total}",
                            )
                    )
                }
            }
            binding.rvRunningData.models = list
            if (list.isNotEmpty())
                mStates.isRunningDataVisible.set(true)

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        fun newInstance() = M20BaseInfoFragment()
    }
}