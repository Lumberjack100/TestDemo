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
        enabledColorRes: Int = 0,
        disabledColorRes: Int = 0
    ) {
        // 使用特定的 tag key 来存储原始颜色
        val originalColorKey = R.id.tag_original_text_color

        // 第一次调用时保存原始颜色
        if (textView.getTag(originalColorKey) == null) {
            textView.setTag(originalColorKey, textView.currentTextColor)
        }

        textView.isEnabled = enabled

        if (enabled) {
            // 启用状态：使用指定的启用颜色，如果未指定则恢复原始颜色
            val colorToUse = if (enabledColorRes != 0) {
                enabledColorRes
            } else {
                // 恢复原始颜色
                textView.getTag(originalColorKey) as Int
            }
            textView.setTextColor(colorToUse)
        } else {
            // 禁用状态：统一使用 title_text_color_black_25
            val disabledColor = if (disabledColorRes != 0) {
                disabledColorRes
            } else {
                ColorUtils.getColor(R.color.disabled_text_color_black_25)
            }
            textView.setTextColor(disabledColor)
        }
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