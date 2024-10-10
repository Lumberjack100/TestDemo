package com.shmedo.mcloudapp.utils

import com.blankj.utilcode.util.ColorUtils
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import java.text.DecimalFormat
import kotlin.math.min

/**
 * 创建者：gonghe
 * 创建时间：2024/5/14
 * 描述： 设备状态信息项处理器
 */
object DeviceStatusInfoProcessor {

    //创建DecimalFormat的方法，确保线程安全
    private fun getDecimalFormat(digit: Int): DecimalFormat {
        return DecimalFormat().apply {
            if (digit == 0) {
                applyPattern("0")
            } else {
                applyPattern("#.${"#".repeat(digit)}")
            }
        }
    }

    fun formatDoubleValue(value: String?, defaultValue: String = "", digit: Int = 2): String {
        // 使用getDecimalFormat方法创建DecimalFormat实例
        val decimalFormat = getDecimalFormat(digit)

        return value?.toDoubleOrNull()?.let { decimalFormat.format(it) } ?: defaultValue
    }

    /**
     * 添加设备状态信息基本项, 字符串直接显示
     * @param name 名称
     * @param value 值
     * @param unit 单位
     * @param textColorRes 字体颜色
     */
    fun addDeviceStatusInfoBasicItemFromString(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        unit: String = "",
        textColorRes: Int = 0,
        isBottomItem: Boolean = false,
    ) {
        value.notNullKey(action = {
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = name,
                    value = it.compareAndReturn(AppContants.PLACE_HOLDER_VALUE, it, "$it$unit"),
                    textColorRes = textColorRes,
                    isBottomItem = isBottomItem
                )
            )
        })
    }

    /**
     * 添加设备状态信息基本项, Double 数值需要处理小数点
     * @param name 名称
     * @param value 值
     * @param defaultValue 默认值
     * @param digit 小数点位数
     * @param unit 单位
     *
     */
    fun addDeviceStatusInfoBasicItemFromDouble(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        defaultValue: String = "0",
        digit: Int = 2,
        unit: String = "",
        isBottomItem: Boolean = false,
    ) {
        value.notNullKey {
            groupList.add(
                DeviceStatusInfoBasicItem(
                    name = name,
                    value = "${formatDoubleValue(it, defaultValue, digit)}$unit",
                    isBottomItem = isBottomItem
                )
            )
        }
    }

    /**
     * 添加设备状态信息基本项，适用于电池电量、电压等，需要特殊处理字体颜色
     * @param name 名称
     * @param value 值
     * @param defaultValue 默认值
     * @param downLimitValue 低于此值时字体颜色变红
     * @param digit 小数点位数
     * @param unit 单位
     */
    fun addDeviceStatusInfoBatteryLevel(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        defaultValue: String = "0",
        downLimitValue: Double = 5.0,
        digit: Int = 2,
        unit: String = "",
        isBottomItem: Boolean = false,
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
                    value = "$tempValue$unit",
                    textColorRes = if (tempValue.toDouble() <= downLimitValue)
                        ColorUtils.getColor(R.color.error_FF4400)
                    else
                        ColorUtils.getColor(R.color.online_colorPrimary),
                    isBottomItem = isBottomItem,
                )
            )
        }
    }

    /**
     * 添加设备状态信息基本项，适用于MR702系列串口状态信息，需要特殊处理字体颜色
     * @param name 名称
     * @param value 值
     * @param defaultValue 默认值
     * @param downLimitValue 低于此值时字体颜色变红
     * @param upLimitValue 高于此值时字体颜色变红
     * @param digit 小数点位数
     * @param unit 单位
     */
    fun addMR702SerialPortStatusInfoItem(
        groupList: MutableList<Any>,
        name: String,
        value: String,
        defaultValue: String = "0",
        downLimitValue: Double = 4.0,
        upLimitValue: Double = 20.0,
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
                    value = "$tempValue$unit",
                    textColorRes = if (tempValue.toDouble() < downLimitValue || tempValue.toDouble() > upLimitValue)
                        ColorUtils.getColor(R.color.error_FF4400)
                    else
                        ColorUtils.getColor(R.color.online_colorPrimary)
                )
            )
        }
    }

    /**
     * 将毫秒转换为合适的时间格式
     */
    fun millis2FitTimeSpan(millis: Long, precis: Int): String {
        val units = arrayOf("天", "小时", "分钟", "秒", "毫秒")
        val unitLen = intArrayOf(86400000, 3600000, 60000, 1000, 1)

        if (millis < 0 || precis <= 0) return "--"
        val precision = min(precis.toDouble(), 5.0).toInt()
        var millisecond = millis
        if (millisecond == 0L || millisecond < unitLen[precision - 1]) return "0${units[precision - 1]}"

        val sb = StringBuilder()
        for (i in 0 until precision) {
            if (millisecond >= unitLen[i]) {
                val mode = millisecond / unitLen[i]
                millisecond -= mode * unitLen[i]
                sb.append(mode).append(units[i])
            }
        }
        return sb.toString()
    }
}