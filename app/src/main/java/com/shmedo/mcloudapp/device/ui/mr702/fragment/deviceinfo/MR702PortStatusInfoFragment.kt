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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRIOStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRInterfaceStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.databinding.FragmentMr702PortStatusInfoBinding
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.device.model.MRIOStatusItem
import com.shmedo.mcloudapp.device.model.RVEmptyItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DeviceStatusInfoParentViewModel
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import org.koin.android.ext.android.inject
import timber.log.Timber

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
            if (isBleDisconnected()) {
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
            addType<RVEmptyItem>(R.layout.item_mr702_device_info_port_status_rv_header)
            addType<DeviceStatusInfoBasicItem>(R.layout.item_mr702_device_info_port_status_rv)
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
            serialPortList.add(RVEmptyItem())
            interfaceStatusInfo.rs485_1.notNullKey {
                serialPortList.add(
                    DeviceStatusInfoBasicItem(
                        name = "RS485-1",
                        value = if (it == "1") "正常" else "异常",
                        textColorRes = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
            }
            interfaceStatusInfo.rs485_2.notNullKey {
                serialPortList.add(
                    DeviceStatusInfoBasicItem(
                        name = "RS485-2",
                        value = if (it == "1") "正常" else "异常",
                        textColorRes = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
            }
            interfaceStatusInfo.rs485_3.notNullKey {
                serialPortList.add(
                    DeviceStatusInfoBasicItem(
                        name = "RS485-3",
                        value = if (it == "1") "正常" else "异常",
                        textColorRes = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
            }
            interfaceStatusInfo.rs232_1.notNullKey {
                serialPortList.add(
                    DeviceStatusInfoBasicItem(
                        name = "RS232-1",
                        value = if (it == "1") "正常" else "异常",
                        textColorRes = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
            }
            interfaceStatusInfo.rs232_2.notNullKey {
                serialPortList.add(
                    DeviceStatusInfoBasicItem(
                        name = "RS232-2",
                        value = if (it == "1") "正常" else "异常",
                        textColorRes = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                            R.color.device_offline_platform
                        )
                    )
                )
            }
            serialPortList.add(DeviceStatusInfoGroupItem("模拟量接口状态"))
            serialPortList.add(RVEmptyItem())

            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "4~20mA  IN1",
                value = interfaceStatusInfo.adc_a1,
                defaultValue = "0",
                minThresHold = 4.0,
                maxThresHold = 20.0,
                digit = 3,
                unit = "mA",
            )
            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "4~20mA  IN2",
                value = interfaceStatusInfo.adc_a2,
                defaultValue = "0",
                minThresHold = 4.0,
                maxThresHold = 20.0,
                digit = 3,
                unit = "mA",
            )
            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "4~20mA  IN3",
                value = interfaceStatusInfo.adc_a3,
                defaultValue = "0",
                minThresHold = 4.0,
                maxThresHold = 20.0,
                digit = 3,
                unit = "mA",
            )
            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "4~20mA  IN4",
                value = interfaceStatusInfo.adc_a4,
                defaultValue = "0",
                minThresHold = 4.0,
                maxThresHold = 20.0,
                digit = 3,
                unit = "mA",
            )
            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "0~5V  IN5",
                value = interfaceStatusInfo.adc_v1,
                defaultValue = "0",
                minThresHold = 0.0,
                maxThresHold = 5.0,
                digit = 1,
                unit = "V",
            )
            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "0~5V  IN6",
                value = interfaceStatusInfo.adc_v2,
                defaultValue = "0",
                minThresHold = 0.0,
                maxThresHold = 5.0,
                digit = 1,
                unit = "V",
            )
            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "0~5V  IN7",
                value = interfaceStatusInfo.adc_v3,
                defaultValue = "0",
                minThresHold = 0.0,
                maxThresHold = 5.0,
                digit = 1,
                unit = "V",
            )
            DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                serialPortList,
                name = "0~5V  IN8",
                value = interfaceStatusInfo.adc_v4,
                defaultValue = "0",
                minThresHold = 0.0,
                maxThresHold = 5.0,
                digit = 1,
                unit = "V",
            )
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
        ioStatusInfo.k5.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "K2",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.k6.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "K3",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.k7.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "K4",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.k8.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "DO1",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.k1.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "DO2",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.k2.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "DO3",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.k3.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "DO4",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.k4.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }

        ioList.add(
            MRIOStatusItem(
                name = "DI1",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.in1.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "DI2",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.in2.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "DI3",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.in3.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }

        ioList.add(
            MRIOStatusItem(
                name = "DI4",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.in4.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "雨量",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.rain.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }
        ioList.add(
            MRIOStatusItem(
                name = "干节点",
                textBold = false,
                textColorResId = ColorUtils.getColor(R.color.title_text_color),
                bgColorResId = ColorUtils.getColor(R.color.gray_f5f6f8)
            )
        )
        ioStatusInfo.dry.notNullKey {
            ioList.add(
                MRIOStatusItem(
                    name = if (it == "1") "开启" else "关闭",
                    textBold = false,
                    textColorResId = if (it == "1") ColorUtils.getColor(R.color.device_online_platform) else ColorUtils.getColor(
                        R.color.title_text_color
                    ),
                    bgColorResId = ColorUtils.getColor(R.color.white)
                )
            )
        }

        binding.rvIoStatus.models = ioList
    }

    companion object {
        fun newInstance() = MR702PortStatusInfoFragment()
    }
}