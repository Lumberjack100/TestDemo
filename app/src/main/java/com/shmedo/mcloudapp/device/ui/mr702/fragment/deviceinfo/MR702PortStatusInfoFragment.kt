package com.shmedo.mcloudapp.device.ui.mr702.fragment.deviceinfo

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRIOStatusInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRInterfaceStatusInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentMr702PortStatusInfoBinding
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.MRIOStatusItem
import com.shmedo.mcloudapp.device.model.MRInterfaceStatusItem
import com.shmedo.mcloudapp.device.model.RVEmptyHeader
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceStatusInfoParentViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat

class MR702PortStatusInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702PortStatusInfoBinding
    private lateinit var mStates: MR702DeviceStatusInfoParentViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_port_status_info, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702PortStatusInfoBinding
        initRefresh()
        initSerialPortStatusAdapter()
        initIOStatusAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryInfo()
        }
    }

    private fun initSerialPortStatusAdapter() {
        binding.rvSerialPortStatus.linear().setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(1f), ColorUtils.getColor(
                        R.color.divider_line_bg
                    )
                )
            )
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<RVEmptyHeader>(R.layout.item_mr702_device_info_port_status_rv_header)
            addType<MRInterfaceStatusItem>(R.layout.item_mr702_device_info_port_status_rv)
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
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        commandItems.clear()

        var entity = MRDeviceInfoEntity(pages = 3, label = 1)
        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)

        entity = MRDeviceInfoEntity(pages = 3, label = 2)
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DEVICE_BASE_INFO, entity)
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
                        val errMsg = "查询接口状态出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        if (result.data.label == "1") {
                            initSerialPortData(result.data.interfaceStatusInfo)
                        } else {
                            binding.refreshLayout.finish()
                            initIOStatusInfo(result.data.ioStatusInfo)
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initSerialPortData(interfaceStatusInfo: MRInterfaceStatusInfo) {
        try {
            val serialPortList = mutableListOf<Any>()

            serialPortList.add(DeviceStatusInfoGroupItem("串口状态"))
            serialPortList.add(RVEmptyHeader())
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
            serialPortList.add(DeviceStatusInfoGroupItem("模拟量接口状态"))
            serialPortList.add(RVEmptyHeader())

            val decimalFormat = DecimalFormat("#.###")
            if (interfaceStatusInfo.adc_a1.toDoubleOrNull() == null || interfaceStatusInfo.adc_a1.toDouble() < 4 || interfaceStatusInfo.adc_a1.toDouble() > 20) {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a1.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }

            if (interfaceStatusInfo.adc_a2.toDoubleOrNull() == null || interfaceStatusInfo.adc_a2.toDouble() < 4 || interfaceStatusInfo.adc_a2.toDouble() > 20) {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a2.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_a3.toDoubleOrNull() == null || interfaceStatusInfo.adc_a3.toDouble() < 4 || interfaceStatusInfo.adc_a3.toDouble() > 20) {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-3 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-3 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a3.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_a4.toDoubleOrNull() == null || interfaceStatusInfo.adc_a4.toDouble() < 4 || interfaceStatusInfo.adc_a4.toDouble() > 20) {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-4 4~20mA",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-4 4~20mA",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_a4.toDouble())}mA",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_v1.toDoubleOrNull() == null || interfaceStatusInfo.adc_v1.toDouble() < 0 || interfaceStatusInfo.adc_v1.toDouble() > 5) {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 0~5V",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-1 0~5V",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_v1.toDouble())}V",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            if (interfaceStatusInfo.adc_v2.toDoubleOrNull() == null || interfaceStatusInfo.adc_v2.toDouble() < 0 || interfaceStatusInfo.adc_v2.toDouble() > 5) {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 0~5V",
                        value = "异常",
                        statusColorResId = ColorUtils.getColor(R.color.device_offline_platform)
                    )
                )
            } else {
                serialPortList.add(
                    MRInterfaceStatusItem(
                        name = "ADC-2 0~5V",
                        value = "${decimalFormat.format(interfaceStatusInfo.adc_v2.toDouble())}V",
                        statusColorResId = ColorUtils.getColor(R.color.device_online_platform)
                    )
                )
            }
            serialPortList.add(DeviceStatusInfoGroupItem("开关量状态"))
            binding.rvSerialPortStatus.models = serialPortList
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
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
        fun newInstance() = MR702PortStatusInfoFragment()
    }
}