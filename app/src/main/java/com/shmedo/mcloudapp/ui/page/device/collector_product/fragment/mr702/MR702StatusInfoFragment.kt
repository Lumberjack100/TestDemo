package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDeviceInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDeviceInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRIOStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRInterfaceStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRModuleStatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDeviceStatusInfoStyle2Fragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/4
 * @desc: 遥测终端机状态信息
 *
 */
class MR702StatusInfoFragment : BaseDeviceStatusInfoStyle2Fragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbarViewModel.toolbarTitleText.set("状态信息")
    }

    override fun queryStatusInfo() {
        commandItems.clear()

        var entity = MRDeviceInfoEntity(pages = 1, label = 1)
        var command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)

        entity = MRDeviceInfoEntity(pages = 3, label = 1)
        command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)

        entity = MRDeviceInfoEntity(pages = 3, label = 2)
        command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)

        entity = MRDeviceInfoEntity(pages = 4, label = 1)
        command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DEVICE_BASE_INFO, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun <T> initStatusInfo(content: T) {
        val mRDeviceInfo: MRDeviceInfo = content as MRDeviceInfo
        if (mRDeviceInfo.pages == "1" && mRDeviceInfo.label == "1") {
            initBaseInfo(mRDeviceInfo.baseInfo)
        } else if (mRDeviceInfo.pages == "3" && mRDeviceInfo.label == "1") {
            initSerialPortData(mRDeviceInfo.interfaceStatusInfo)
        } else if (mRDeviceInfo.pages == "3" && mRDeviceInfo.label == "2") {
            initIOStatusInfo(mRDeviceInfo.ioStatusInfo)
        } else if (mRDeviceInfo.pages == "4" && mRDeviceInfo.label == "1") {
            initModuleStatusInfo(mRDeviceInfo.moduleStatusInfo)
        }
    }

    private fun initBaseInfo(baseInfo: MRBaseInfo) {
        launchWithViewLifecycle {
            try {
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                groupList.add(DeviceStatusInfoGroupItem("供电信息"))
                val externalVoltage = baseInfo.volt.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "外部电压",
                    value = if (externalVoltage == 0.0) "0" else baseInfo.volt.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "V",
                    textColorRes = if ((externalVoltage >= 9 && externalVoltage < 28) || externalVoltage == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    isBottomItem = true
                )

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("环境信息"))
                val internalTemp = baseInfo.temp.toDoubleOrNull() ?: Double.MAX_VALUE
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部温度",
                    value = baseInfo.temp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                    textColorRes = if ((internalTemp > -20 && internalTemp < 70) || internalTemp == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                        R.color.warn_FF9D00
                    ),
                    unit = "℃",
                )

                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "内部湿度",
                    value = baseInfo.hum.ifEmpty { AppContants.Companion.PLACE_HOLDER_VALUE },
                    unit = "%",
                    isBottomItem = true
                )

                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initSerialPortData(interfaceStatusInfo: MRInterfaceStatusInfo) {
        launchWithViewLifecycle {
            try {
                val groupList = mutableListOf<Any>()
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("串口信息"))
                interfaceStatusInfo.rs485_1.notNullKey {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "RS485-1",
                            value = if (it == "1") "正常" else "故障",
                            textColorRes = if (it == "1") 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    )
                }
                interfaceStatusInfo.rs485_2.notNullKey {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "RS485-2",
                            value = if (it == "1") "正常" else "故障",
                            textColorRes = if (it == "1") 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    )
                }
                interfaceStatusInfo.rs485_3.notNullKey {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "RS485-3",
                            value = if (it == "1") "正常" else "故障",
                            textColorRes = if (it == "1") 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    )
                }
                interfaceStatusInfo.rs232_1.notNullKey {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "RS232-1",
                            value = if (it == "1") "正常" else "故障",
                            textColorRes = if (it == "1") 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            )
                        )
                    )
                }
                interfaceStatusInfo.rs232_2.notNullKey {
                    groupList.add(
                        DeviceStatusInfoBasicItem(
                            name = "RS232-2",
                            value = if (it == "1") "正常" else "故障",
                            textColorRes = if (it == "1") 0 else ColorUtils.getColor(
                                R.color.error_FF4400
                            ),
                            isBottomItem = true
                        )
                    )
                }

                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("模拟量接口信息"))
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN1(4~20mA)",
                    value = interfaceStatusInfo.adc_a1,
                    defaultValue = "0",
                    downLimitValue = 4.0,
                    upLimitValue = 20.0,
                    digit = 3,
                    unit = "mA",
                )
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN2(4~20mA)",
                    value = interfaceStatusInfo.adc_a2,
                    defaultValue = "0",
                    downLimitValue = 4.0,
                    upLimitValue = 20.0,
                    digit = 3,
                    unit = "mA",
                )
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN3(4~20mA)",
                    value = interfaceStatusInfo.adc_a3,
                    defaultValue = "0",
                    downLimitValue = 4.0,
                    upLimitValue = 20.0,
                    digit = 3,
                    unit = "mA",
                )
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN4(4~20mA)",
                    value = interfaceStatusInfo.adc_a4,
                    defaultValue = "0",
                    downLimitValue = 4.0,
                    upLimitValue = 20.0,
                    digit = 3,
                    unit = "mA",
                )
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN5(0~5V)",
                    value = interfaceStatusInfo.adc_v1,
                    defaultValue = "0",
                    downLimitValue = 0.0,
                    upLimitValue = 5.0,
                    digit = 1,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN6(0~5V)",
                    value = interfaceStatusInfo.adc_v2,
                    defaultValue = "0",
                    downLimitValue = 0.0,
                    upLimitValue = 5.0,
                    digit = 1,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN7(0~5V)",
                    value = interfaceStatusInfo.adc_v3,
                    defaultValue = "0",
                    downLimitValue = 0.0,
                    upLimitValue = 5.0,
                    digit = 1,
                    unit = "V",
                )
                DeviceStatusInfoProcessor.addMR702SerialPortStatusInfoItem(
                    groupList,
                    name = "IN8(0~5V)",
                    value = interfaceStatusInfo.adc_v4,
                    defaultValue = "0",
                    downLimitValue = 0.0,
                    upLimitValue = 5.0,
                    digit = 1,
                    unit = "V",
                    isBottomItem = true
                )

                binding.recyclerview.bindingAdapter.apply {
                    mutable.addAll(groupList)
                    notifyItemRangeInserted(itemCount, groupList.size)
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initIOStatusInfo(ioStatusInfo: MRIOStatusInfo) {
        launchWithViewLifecycle {
            try {
                val groupList = mutableListOf<Any>()
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("开关量信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "K1",
                    value =  ioStatusInfo.k5.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k5 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "K2",
                    value =  ioStatusInfo.k6.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k6 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "K3",
                    value =  ioStatusInfo.k7.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k7 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "K4",
                    value =  ioStatusInfo.k8.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k8 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DO1",
                    value =  ioStatusInfo.k1.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k1 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DO2",
                    value =  ioStatusInfo.k2.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k2 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DO3",
                    value =  ioStatusInfo.k3.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k3 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DO4",
                    value =  ioStatusInfo.k4.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.k4 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DI1",
                    value =  ioStatusInfo.in1.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.in1 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DI2",
                    value =  ioStatusInfo.in2.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.in2 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DI3",
                    value =  ioStatusInfo.in3.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.in3 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "DI4",
                    value =  ioStatusInfo.in4.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.in4 == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "雨量",
                    value =  ioStatusInfo.rain.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.rain == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "干节点",
                    value =  ioStatusInfo.dry.compareAndReturn("1", "开启", "关闭"),
                    textColorRes = if (ioStatusInfo.dry == "1") ColorUtils.getColor(R.color.online_colorPrimary) else 0,
                    isBottomItem = true
                )

                binding.recyclerview.bindingAdapter.apply {
                    mutable.addAll(groupList)
                    notifyItemRangeInserted(itemCount, groupList.size)
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    private fun initModuleStatusInfo(moduleStatusInfo: MRModuleStatusInfo) {
        launchWithViewLifecycle {
            try {
                val groupList = mutableListOf<Any>()
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("模块信息"))
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "触摸屏",
                    value =  moduleStatusInfo.screen.compareAndReturn("1", "正常", "故障"),
                    textColorRes = if (moduleStatusInfo.screen == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "4G模块",
                    value =  moduleStatusInfo.datanet.compareAndReturn("1", "正常", "故障"),
                    textColorRes = if (moduleStatusInfo.datanet == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "北斗定位模块",
                    value =  moduleStatusInfo.beidou.compareAndReturn("1", "正常", "故障"),
                    textColorRes = if (moduleStatusInfo.beidou == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "有线网模块",
                    value =  moduleStatusInfo.wirednet.compareAndReturn("1", "正常", "故障"),
                    textColorRes = if (moduleStatusInfo.wirednet == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "FLASH模块",
                    value =  moduleStatusInfo.flash.compareAndReturn("1", "正常", "故障"),
                    textColorRes = if (moduleStatusInfo.flash == "1") 0 else ColorUtils.getColor(R.color.error_FF4400)
                )
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "EMMC存储模块",
                    value =  moduleStatusInfo.emmc.compareAndReturn("1", "正常", "故障"),
                    textColorRes = if (moduleStatusInfo.emmc == "1") 0 else ColorUtils.getColor(R.color.error_FF4400),
                    isBottomItem = true
                )

                binding.recyclerview.bindingAdapter.apply {
                    mutable.addAll(groupList)
                    notifyItemRangeInserted(itemCount, groupList.size)
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

}