package com.shmedo.mcloudapp.device.bindadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ColorUtils
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.lib.device.base.iot_cmd.enums.SensorErrorType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.SignalView

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/20 <br/>
 * 描述：     TODO
 */
object IOTDeviceBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["dataCenterStatus"], requireAll = false)
    fun setDataCenterStatus(view: TextView, status: String) {
        when (status) {
            "0" -> {
                view.text = "未开启"
                view.setTextColor(ColorUtils.getColor(R.color.device_unopened_platform))
            }

            "1" -> {
                view.text = "在线"
                view.setTextColor(ColorUtils.getColor(R.color.device_online_platform))
            }

            "2" -> {
                view.text = "离线"
                view.setTextColor(ColorUtils.getColor(R.color.device_offline_platform))
            }

            else -> {
                view.text = "离线"
                view.setTextColor(ColorUtils.getColor(R.color.device_offline_platform))
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["bind_errno"], requireAll = false)
    fun setErrnoStatus(view: TextView, errno: String) {
        when (errno) {
            "1" -> {
                view.text = "正常"
                view.setTextColor(ColorUtils.getColor(R.color.device_online_platform))
            }

            else -> {
                view.text = "未接入"
                view.setTextColor(ColorUtils.getColor(R.color.device_offline_platform))
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["bind_sensor_errno"], requireAll = false)
    fun setSensorErrnoStatus(view: TextView, errno: String) {
        view.text = SensorErrorType.getErrorMessageByCode(errno.toString())
        when (errno) {
            "0" -> {
                view.setTextColor(ColorUtils.getColor(R.color.device_online_platform))
            }

            else -> {
                view.setTextColor(ColorUtils.getColor(R.color.device_offline_platform))
            }
        }
    }

    @JvmStatic
    @BindingAdapter(
        value = ["textViewEnabled", "enabledTextColor", "disabledTextColor"],
        requireAll = false
    )
    fun setTextViewEnabled(
        textView: TextView,
        enabled: Boolean,
        enabledColorRes: Int,
        disabledColorRes: Int
    ) {
        textView.isEnabled = enabled
        textView.setTextColor(
            if (enabled) ColorUtils.getColor(if (enabledColorRes == 0) R.color.title_text_color else enabledColorRes)
            else ColorUtils.getColor(if (disabledColorRes == 0) R.color.sub_title_text_color else disabledColorRes)
        )
    }

    @JvmStatic
    @BindingAdapter(
        value = ["rightArrowVisible"],
        requireAll = false
    )
    fun setTextViewRightArrowVisible(textView: TextView, visible: Boolean) {
        textView.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (visible) R.drawable.icon_arrow_right else 0,
            0
        )
    }

    @JvmStatic
    @BindingAdapter(
        value = ["checkedImmediatelyNoEvent"],
        requireAll = false
    )
    fun setCheckedImmediatelyNoEvent(switchButton: SwitchButton, checked: Boolean) {
        switchButton.setCheckedImmediatelyNoEvent(checked)
    }

    @JvmStatic
    @BindingAdapter("bind_level_color")
    fun setLevelColor(view: SignalView, color: Int) {
        view.setLevelColor(color)
    }

    @JvmStatic
    @BindingAdapter("bind_signal_level")
    fun setSignalLevel(view: SignalView, level: Int) {
        view.setSignalLevel(level)
    }

    @JvmStatic
    @BindingAdapter("bind_signal_value")
    fun setSignalLevelBydBmValue(view: SignalView, value: Int) {
        when (value) {
            -113, 85 -> view.setSignalLevel(0)
            in -110..-96 -> {
                view.setSignalLevel(1)
                view.setLevelColor(ColorUtils.getColor(R.color.orange))
            }

            in -95..-86 -> {
                view.setSignalLevel(2)
                view.setLevelColor(ColorUtils.getColor(R.color.orange))
            }

            in -85..-76 -> {
                view.setSignalLevel(3)
                view.setLevelColor(ColorUtils.getColor(R.color.text_color_3AD094))
            }

            in -75..-50 -> {
                view.setSignalLevel(4)
                view.setLevelColor(ColorUtils.getColor(R.color.text_color_3AD094))
            }
        }
    }

    @JvmStatic
    @BindingAdapter("bind_voltage_color")
    fun setVoltageColor(textView: TextView, value: String = "") {
        if (value.isNullOrEmpty()) {
            textView.setTextColor(textView.currentTextColor)
            return
        }
        if (value.contains("%")) {
            //移除 % 并转成 Double类型数值,如果值小于等于 10 textView 设置 R.color.device_offline_platform，否则 textView 设置 R.color.text_color_3AD094
            val volt = value.replace("%", "").toDoubleOrNull() ?: 0.0
            if (volt <= 10) {
                textView.setTextColor(ColorUtils.getColor(R.color.device_offline_platform))
            } else {
                textView.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094))
            }
        } else {
            val volt = value.toDoubleOrNull() ?: 0.0
            if (volt <= 5) {
                textView.setTextColor(ColorUtils.getColor(R.color.device_offline_platform))
            } else {
                textView.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094))
            }
        }
    }
}