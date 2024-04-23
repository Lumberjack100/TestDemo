package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRBaseInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMr702BaseInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceInfoViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import java.text.DecimalFormat

class MR702BaseInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702BaseInfoBinding
    private lateinit var mStates: MR702DeviceInfoViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_base_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702BaseInfoBinding
        initRefresh()
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

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
        launchWithViewLifecycle {
            if (communicateWay is BleConnect)
                return@launchWithViewLifecycle

            val deviceDetailInfo =
                deviceRequestViewModel.getDeviceDetailInfo(deviceInfo.deviceToken) { error: Throwable ->
                } ?: return@launchWithViewLifecycle

            mStates.wrapBaseInfo.get().apply {
                regcode = deviceDetailInfo.deviceInfo.productKey
                regtime = deviceDetailInfo.deviceInfo.createTime
            }
            mStates.wrapBaseInfo.notifyChange()
        }
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
        commandItems.clear()

        val entity = MRDeviceInfoEntity(pages = 1, label = 1)
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO
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

            else -> {}
        }
    }

    private fun initBaseInfo(baseInfo: MRBaseInfo) {
        val decimalFormat = DecimalFormat("#.#")
        try {
            mStates.wrapBaseInfo.get().apply {
                productname = deviceInfo.productName
                producttype = deviceInfo.deviceName
                if (deviceInfo.productKey.isNotEmpty())
                    regcode = deviceInfo.productKey
                if (deviceInfo.createTime.isNotEmpty())
                    regtime = deviceInfo.createTime
                sn = deviceInfo.deviceToken
                ver = baseInfo.ver
                imei = baseInfo.imei
                temp = baseInfo.temp.toDoubleOrNull()?.let {
                    decimalFormat.format(it)
                } ?: ""
                hum = baseInfo.hum.toDoubleOrNull()?.let {
                    decimalFormat.format(it)
                } ?: ""
                volt = baseInfo.volt.toDoubleOrNull()?.let {
                    decimalFormat.format(it)
                } ?: ""
                csq = when (baseInfo.csq.toInt()) {
                    1 -> "优"
                    2 -> "良好"
                    3 -> "较差"
                    else -> "未知"
                }
                local = baseInfo.local
            }
            mStates.wrapBaseInfo.notifyChange()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        fun newInstance() = MR702BaseInfoFragment()
    }
}