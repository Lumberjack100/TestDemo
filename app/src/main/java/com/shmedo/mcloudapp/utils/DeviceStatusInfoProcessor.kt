package com.shmedo.mcloudapp.utils

import com.blankj.utilcode.util.ColorUtils
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.ext.notNullKey
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/5/14
 * 描述： TODO
 */
object DeviceStatusInfoProcessor {

    // 创建DecimalFormat的方法，确保线程安全
    private fun getDecimalFormat(digit: Int): DecimalFormat {
        return DecimalFormat("#.####", DecimalFormatSymbols(Locale.getDefault())).apply {
            //根据 digit 设置保留小数位数
            maximumFractionDigits = digit
        }
    }

    fun formatDoubleValue(value: String?, defaultValue: String, digit: Int = 2): String {
        // 使用getDecimalFormat方法创建DecimalFormat实例
        val decimalFormat = getDecimalFormat(digit)

        return value?.toDoubleOrNull()?.let { decimalFormat.format(it) } ?: defaultValue
    }

    fun addDeviceStatusInfoBasicItemFromString(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        unit: String = ""
    ) {
        value.notNullKey {
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = name,
                    value = "$it $unit"
                )
            )
        }
    }

    fun addDeviceStatusInfoBasicItemFromDouble(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        defaultValue: String = "0",
        digit: Int = 2,
        unit: String = ""
    ) {
        value.notNullKey {
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = name,
                    value = "${formatDoubleValue(it, defaultValue, digit)} $unit"
                )
            )
        }
    }

    fun addDeviceStatusInfoBatteryLevel(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        defaultValue: String = "0",
        thresHold: Double = 5.0,
        digit: Int = 2,
        unit: String = ""
    ) {
        value.notNullKey {
            val tempValue = formatDoubleValue(
                it,
                defaultValue,
                digit,
            )
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = name,
                    value = "$tempValue $unit",
                    textColorRes = if (tempValue.toDouble() <= thresHold)
                        ColorUtils.getColor(R.color.device_offline_platform)
                    else
                        ColorUtils.getColor(R.color.text_color_3AD094)
                )
            )
        }
    }


    fun addMR702SerialPortStatusInfoItem(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        defaultValue: String = "0",
        minThresHold: Double = 4.0,
        maxThresHold: Double = 20.0,
        digit: Int = 2,
        unit: String = ""
    ) {
        value.notNullKey {
            val tempValue = formatDoubleValue(
                it,
                defaultValue,
                digit,
            )
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = name,
                    value = "$tempValue $unit",
                    textColorRes = if (tempValue.toDouble() < minThresHold || tempValue.toDouble() > maxThresHold)
                        ColorUtils.getColor(R.color.device_offline_platform)
                    else
                        ColorUtils.getColor(R.color.text_color_3AD094)
                )
            )
        }
    }
}