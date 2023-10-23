package com.shmedo.mcloudapp.device.bindadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ColorUtils
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.mcloudapp.R

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
                view.text = "已连接"
                view.setTextColor(ColorUtils.getColor(R.color.device_online_platform))
            }

            "2" -> {
                view.text = "未连接"
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
        value = ["checkedImmediatelyNoEvent"],
        requireAll = false
    )
    fun setCheckedImmediatelyNoEvent(view: SwitchButton, checked: Boolean) {
        view.isChecked = checked
    }
}