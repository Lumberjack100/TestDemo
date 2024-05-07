package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.u_product.URCurrentStateInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentUIProductSensorInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.UIProductSensorInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 倾斜仪传感器状态
 *
 */
class UIProductSensorInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUIProductSensorInfoBinding
    private lateinit var mStates: UIProductSensorInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_u_i_product_sensor_info, BR.stateVM, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUIProductSensorInfoBinding
        refreshLayout = binding.refreshLayout
        initRefresh()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryInfo()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryInfo() {
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

            else -> {}
        }
    }

    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val urCurrentStateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<URCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                val uRSensorInfoList = urCurrentStateInfo.attach_data
                if (uRSensorInfoList.isNullOrEmpty()) {
                    return@launchWithViewLifecycle
                }
                uRSensorInfoList.forEach { info ->
                    when (info.key) {
                        "memsstatus" -> {
                            mStates.memsErrNo.set(info.value)
                        }

                        "initAngle"->{
                            if (info.value == IOTConstants.NULL_KEY) {
                                mStates.memsInitialAxisX.set(IOTConstants.NULL_KEY)
                                mStates.memsInitialAxisY.set(IOTConstants.NULL_KEY)
                                mStates.memsInitialAxisZ.set(IOTConstants.NULL_KEY)
                            } else {
                                //根据逗号分隔
                                val initAngle = info.value.split(",")
                                if (initAngle.isNotEmpty())
                                    mStates.memsInitialAxisX.set(initAngle[0].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                                if (initAngle.size > 1)
                                    mStates.memsInitialAxisY.set(initAngle[1].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                                if (initAngle.size > 2)
                                    mStates.memsInitialAxisZ.set(initAngle[2].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                            }
                        }
                        "angle"->{
                            if (info.value == IOTConstants.NULL_KEY) {
                                mStates.memsAxisXCurrent.set(IOTConstants.NULL_KEY)
                                mStates.memsAxisYCurrent.set(IOTConstants.NULL_KEY)
                                mStates.memsAxisZCurrent.set(IOTConstants.NULL_KEY)
                            } else {
                                val angle = info.value.split(",")
                                if (angle.isNotEmpty())
                                    mStates.memsAxisXCurrent.set(angle[0].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                                if (angle.size > 1)
                                    mStates.memsAxisYCurrent.set(angle[1].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                                if (angle.size > 2)
                                    mStates.memsAxisZCurrent.set(angle[2].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                            }
                        }

                        "acc"->{
                            if (info.value == IOTConstants.NULL_KEY) {
                                mStates.memsAxisXAcceleration.set(IOTConstants.NULL_KEY)
                                mStates.memsAxisYAcceleration.set(IOTConstants.NULL_KEY)
                                mStates.memsAxisZAcceleration.set(IOTConstants.NULL_KEY)
                            } else {
                                val acc = info.value.split(",")
                                if (acc.isNotEmpty())
                                    mStates.memsAxisXAcceleration.set(acc[0].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                                if (acc.size > 1)
                                    mStates.memsAxisYAcceleration.set(acc[1].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                                if (acc.size > 2)
                                    mStates.memsAxisZAcceleration.set(acc[2].toDoubleOrNull()
                                        ?.let {
                                            decimalFormat.format(it)
                                        } ?: IOTConstants.NULL_KEY)
                            }
                        }

                        "worktime" -> {
                            mStates.runTime.set(info.value.toIntOrNull()?.let {
                                val day = it / (24 * 60 * 60)
                                val hour = (it % (24 * 60 * 60)) / (60 * 60)
                                val minute = (it % (60 * 60)) / 60
                                "${day}天${hour}时${minute}分"
                            } ?: IOTConstants.NULL_KEY)
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    companion object {
        fun newInstance() = UIProductSensorInfoFragment()
    }
}