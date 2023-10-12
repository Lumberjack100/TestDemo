package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRIOStatusInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRInterfaceStatusInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentMr702InterfaceStatusInfoBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.MRIOStatusItem
import com.shmedo.mcloudapp.device.model.MRInterfaceStatusHeader
import com.shmedo.mcloudapp.device.model.MRInterfaceStatusItem
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceInfoViewModel
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class MR702InterfaceStatusInfoFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702InterfaceStatusInfoBinding by lazy { getBinding() as FragmentMr702InterfaceStatusInfoBinding }
    private val mStates: MR702DeviceInfoViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_interface_status_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
        initSerialPortStatusAdapter()
        initAnalogInterfaceStatusAdapter()
        initIOStatusAdapter()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryInfo()
        }
    }

    private fun initSerialPortStatusAdapter() {
        binding.rvSerialPortStatus.setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(1f), ColorUtils.getColor(
                        R.color.divider_line_bg
                    )
                )
            )
            addType<MRInterfaceStatusHeader>(R.layout.item_mr702_device_info_interface_status_rv_header)
            addType<MRInterfaceStatusItem>(R.layout.item_mr702_device_info_interface_status_rv)
        }
    }

    private fun initAnalogInterfaceStatusAdapter() {
        binding.rvAnalogInterfaceStatus.setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(1f), ColorUtils.getColor(
                        R.color.divider_line_bg
                    )
                )
            )
            addType<MRInterfaceStatusHeader>(R.layout.item_mr702_device_info_interface_status_rv_header)
            addType<MRInterfaceStatusItem>(R.layout.item_mr702_device_info_interface_status_rv)
        }
    }

    private fun initIOStatusAdapter() {
        binding.rvIoStatus.grid(4).divider {
            setDrawable(R.drawable.divider_horizontal)
            orientation = DividerOrientation.GRID
            includeVisible = true
        }.setup { rv ->
            addType<MRIOStatusItem>(R.layout.item_mr702_device_info_io_status_rv)
        }
    }

    override fun lazyLoadData() {
//        binding.refreshLayout.autoRefresh()
        testSerialData()
        testIOData()
    }

    private fun queryInfo() {
        commandItems.clear()

        var entity = MRDeviceInfoEntity(pages = 3, label = 1)
        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)

        entity = MRDeviceInfoEntity(pages = 3, label = 2)
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isShowLoadingDialog = false)
    }

    override fun cancelNearbyCommunicationTimeoutJob(isDismissLoadingDialog: Boolean) {
        super.cancelNearbyCommunicationTimeoutJob(false)
        binding.refreshLayout.finish(false)
    }

    override fun showNearbyCommunicationTimeoutAlert(
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        Toaster.show("发送指令超时,请稍后尝试")
        binding.refreshLayout.finish(false)
    }

    override fun doNetDispatchFailed(cmdStr: String, errorMsg: String) {
        Toaster.show("下发指令失败: $errorMsg")
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        netIotCommandViewModel.processCmdResult()
    }

    override fun doCmdResponseResultError(errorMsg: String) {
        Toaster.show("指令响应错误: $errorMsg")
        binding.refreshLayout.finish(false)
    }

    override fun doCmdResponseResultTimeOut(errorMsg: String) {
        Toaster.show("指令响应超时: $errorMsg")
        binding.refreshLayout.finish(false)
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO -> {
                val result = iotParseManager.parse<MRDeviceInfo>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询接口状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList(isShowLoadingDialog = false)
                        if (result.data.label == "1") {
                            initSerialPortData(result.data.interfaceStatusInfo)
                            initAnalogInterfaceStatusInfo(result.data.interfaceStatusInfo)
                        } else {
                            binding.refreshLayout.finish()
                            initIOStatusInfo(result.data.ioStatusInfo)
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun initSerialPortData(interfaceStatusInfo: MRInterfaceStatusInfo) {
        val serialPortList = mutableListOf<MRInterfaceStatusItem>()
        try {
            serialPortList.add(
                MRInterfaceStatusItem(
                    name = "RS485-1",
                    value = if (interfaceStatusInfo.rs485_1 == "1") "正常" else "异常",
                    statusColorResId = if (interfaceStatusInfo.rs485_1 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.device_offline_platform
                    )
                )
            )
            serialPortList.add(
                MRInterfaceStatusItem(
                    name = "RS485-2",
                    value = if (interfaceStatusInfo.rs485_2 == "1") "正常" else "异常",
                    statusColorResId = if (interfaceStatusInfo.rs485_2 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.device_offline_platform
                    )
                )
            )
            serialPortList.add(
                MRInterfaceStatusItem(
                    name = "RS485-3",
                    value = if (interfaceStatusInfo.rs485_3 == "1") "正常" else "异常",
                    statusColorResId = if (interfaceStatusInfo.rs485_3 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.device_offline_platform
                    )
                )
            )
            serialPortList.add(
                MRInterfaceStatusItem(
                    name = "RS232-1",
                    value = if (interfaceStatusInfo.rs232_1 == "1") "正常" else "异常",
                    statusColorResId = if (interfaceStatusInfo.rs232_1 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.device_offline_platform
                    )
                )
            )
            serialPortList.add(
                MRInterfaceStatusItem(
                    name = "RS232-2",
                    value = if (interfaceStatusInfo.rs232_2 == "1") "正常" else "异常",
                    statusColorResId = if (interfaceStatusInfo.rs232_2 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.device_offline_platform
                    )
                )
            )
            binding.rvSerialPortStatus.models = serialPortList
            binding.rvSerialPortStatus.bindingAdapter.run {
                addHeader(MRInterfaceStatusHeader(), animation = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initAnalogInterfaceStatusInfo(interfaceStatusInfo: MRInterfaceStatusInfo) {
        val analogList = mutableListOf<MRInterfaceStatusItem>()
        try {
            var decimalFormat = DecimalFormat("#.###")
            if (interfaceStatusInfo.adc_a1.toDoubleOrNull() == null || interfaceStatusInfo.adc_a1.toDouble() < 4 || interfaceStatusInfo.adc_a1.toDouble() > 20) {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a1.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }

            if (interfaceStatusInfo.adc_a2.toDoubleOrNull() == null || interfaceStatusInfo.adc_a2.toDouble() < 4 || interfaceStatusInfo.adc_a2.toDouble() > 20) {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a2.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_a3.toDoubleOrNull() == null || interfaceStatusInfo.adc_a3.toDouble() < 4 || interfaceStatusInfo.adc_a3.toDouble() > 20) {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-3 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-3 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a3.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_a4.toDoubleOrNull() == null || interfaceStatusInfo.adc_a4.toDouble() < 4 || interfaceStatusInfo.adc_a4.toDouble() > 20) {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-4 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-4 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a4.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_v1.toDoubleOrNull() == null || interfaceStatusInfo.adc_v1.toDouble() < 0 || interfaceStatusInfo.adc_v1.toDouble() > 5) {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 0~5V",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 0~5V",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_v1.toDouble())}V",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_v2.toDoubleOrNull() == null || interfaceStatusInfo.adc_v2.toDouble() < 0 || interfaceStatusInfo.adc_v2.toDouble() > 5) {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 0~5V",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                analogList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 0~5V",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_v2.toDouble())}V",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            binding.rvAnalogInterfaceStatus.models = analogList
            binding.rvAnalogInterfaceStatus.bindingAdapter.run {
                addHeader(MRInterfaceStatusHeader(), animation = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initIOStatusInfo(ioStatusInfo: MRIOStatusInfo) {
        val ioList = mutableListOf<MRIOStatusItem>()
        ioList.add(
            MRIOStatusItem(
                name = "接口",
                textBold = true,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.mr_communication_data_table_row_header_backround)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "状态",
                textBold = true,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.mr_communication_data_table_row_header_backround)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "接口",
                textBold = true,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.mr_communication_data_table_row_header_backround)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "状态",
                textBold = true,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.mr_communication_data_table_row_header_backround)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K1",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k1,
                textBold = false,
                textColorResId = if (ioStatusInfo.k1 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN1",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in1,
                textBold = false,
                textColorResId = if (ioStatusInfo.in1 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K2",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k2,
                textBold = false,
                textColorResId = if (ioStatusInfo.k2 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN2",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in2,
                textBold = false,
                textColorResId = if (ioStatusInfo.in2 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K3",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k3,
                textBold = false,
                textColorResId = if (ioStatusInfo.k3 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN3",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in3,
                textBold = false,
                textColorResId = if (ioStatusInfo.in3 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K4",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k4,
                textBold = false,
                textColorResId = if (ioStatusInfo.k4 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN4",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in4,
                textBold = false,
                textColorResId = if (ioStatusInfo.in4 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K5",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k5,
                textBold = false,
                textColorResId = if (ioStatusInfo.k5 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN5",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in5,
                textBold = false,
                textColorResId = if (ioStatusInfo.in5 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K6",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k6,
                textBold = false,
                textColorResId = if (ioStatusInfo.k6 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN6",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in6,
                textBold = false,
                textColorResId = if (ioStatusInfo.in6 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K7",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k7,
                textBold = false,
                textColorResId = if (ioStatusInfo.k7 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN7",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in7,
                textBold = false,
                textColorResId = if (ioStatusInfo.in7 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "K8",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.k8,
                textBold = false,
                textColorResId = if (ioStatusInfo.k8 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "IN8",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.in8,
                textBold = false,
                textColorResId = if (ioStatusInfo.in8 == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "雨量",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.rain,
                textBold = false,
                textColorResId = if (ioStatusInfo.rain == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = "干节点",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioList.add(
            MRIOStatusItem(
                name = ioStatusInfo.dry,
                textBold = false,
                textColorResId = if (ioStatusInfo.dry == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                    R.color.title_text_color
                ),
                bgColorResId = ColorUtils.getColor(R.color.white)
            )
        )
        binding.rvIoStatus.models = ioList
    }

    companion object {
        fun newInstance() = MR702InterfaceStatusInfoFragment()
    }

    private fun testSerialData() {
        val interfaceStatusInfo = MRInterfaceStatusInfo(
            "1",
            "1",
            "0",
            "1",
            "0",
            "25",
            "5.123",
            "6.123",
            "7.123",
            "6",
            "2.1"
        )
        initSerialPortData(interfaceStatusInfo)
        initAnalogInterfaceStatusInfo(interfaceStatusInfo)
    }

    private fun testIOData() {
        val ioStatusInfo = MRIOStatusInfo(
            "1",
            "1",
            "0",
            "1",
            "0",
            "1",
            "1",
            "1",
            "1",
            "1",
            "0",
            "1",
            "0",
            "1",
            "1",
            "1",
            "1",
            "1",
        )
        initIOStatusInfo(ioStatusInfo)
    }
}