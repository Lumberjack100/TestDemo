package com.shmedo.mcloudapp.bindadapter

import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ColorUtils
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.lib.cmd.base.iot_cmd.enums.SensorErrorType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DeviceStatusEnum
import com.shmedo.mcloudapp.ui.widget.AnimatedTextSwitcher
import com.shmedo.mcloudapp.ui.widget.SignalView

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
                view.text = "未启用"
                view.setTextColor(ColorUtils.getColor(R.color.offline_BABABA))
            }

            "1" -> {
                view.text = "已连接"
                view.setTextColor(ColorUtils.getColor(R.color.online_colorPrimary))
            }

            else -> {
                view.text = "未连接"
                view.setTextColor(ColorUtils.getColor(R.color.offline_BABABA))
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["bind_errno"], requireAll = false)
    fun setErrnoStatus(view: TextView, errno: String) {
        when (errno) {
            "1" -> {
                view.text = "正常"
                view.setTextColor(ColorUtils.getColor(R.color.online_colorPrimary))
            }

            else -> {
                view.text = "异常"
                view.setTextColor(ColorUtils.getColor(R.color.error_FF4400))
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["bind_sensor_errno"], requireAll = false)
    fun setSensorErrnoStatus(view: TextView, errno: String) {
        view.text = SensorErrorType.getErrorMessageByCode(errno.toString())
        when (errno) {
            "0" -> {
                view.setTextColor(ColorUtils.getColor(R.color.online_colorPrimary))
            }

            else -> {
                view.setTextColor(ColorUtils.getColor(R.color.error_FF4400))
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
            if (enabled) {
                if (enabledColorRes == 0) ColorUtils.getColor(R.color.title_text_color_black_90) else enabledColorRes
            } else {
                if (disabledColorRes == 0) ColorUtils.getColor(R.color.title_text_color_black_25) else disabledColorRes
            }
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
            if (visible) R.drawable.ic_arrow_right else 0,
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
    @BindingAdapter("bind_signal_value")
    fun setSignalLevelBydBmValue(view: SignalView, value: Int) {
        when (value) {
            in -110..-100 -> {
                view.setSignalLevel(1)
                view.setLevelColor(ColorUtils.getColor(R.color.warn_FF9D00))
            }

            in -99..-90 -> {
                view.setSignalLevel(2)
                view.setLevelColor(ColorUtils.getColor(R.color.warn_FF9D00))
            }

            in -89..-80 -> {
                view.setSignalLevel(3)
                view.setLevelColor(ColorUtils.getColor(R.color.online_colorPrimary))
            }

            in -79..-50 -> {
                view.setSignalLevel(4)
                view.setLevelColor(ColorUtils.getColor(R.color.online_colorPrimary))
            }

            else -> {
                view.setSignalLevel(0)
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
                textView.setTextColor(ColorUtils.getColor(R.color.error_FF4400))
            } else {
                textView.setTextColor(ColorUtils.getColor(R.color.online_colorPrimary))
            }
        } else {
            val volt = value.toDoubleOrNull() ?: 0.0
            if (volt <= 5) {
                textView.setTextColor(ColorUtils.getColor(R.color.error_FF4400))
            } else {
                textView.setTextColor(ColorUtils.getColor(R.color.online_colorPrimary))
            }
        }
    }

    @JvmStatic
    @BindingAdapter("progressDrawableReadingData")
    fun setProgressDrawableReadingData(progressBar: ProgressBar, isReadingData: Boolean) {
        val drawableResId =
            if (isReadingData) R.drawable.custom_progress_horizontal_blue else R.drawable.custom_progress_horizontal_green
        progressBar.progressDrawable = ContextCompat.getDrawable(progressBar.context, drawableResId)
    }


    @JvmStatic
    @BindingAdapter(
        value = ["animatedText", "deviceStatusCode"],
        requireAll = false
    )
    fun setAnimatedText(
        view: AnimatedTextSwitcher,
        text: String?,
        statusCode: String = DeviceStatusEnum.UNKNOWN.code
    ) {
        text?.let { view.setText(it, DeviceStatusEnum.valueByCode(statusCode)) }
    }

}