package com.shmedo.mcloudapp.device.bindadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ColorUtils
import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/20 <br/>
 * 描述：     TODO
 */
object IOTDeviceBindingAdapter {
    @JvmStatic
    @BindingAdapter(value = ["linkStatus"], requireAll = false)
    fun linkStatus(view: TextView, status: String) {
        when (status) {
            "0" -> {
                view.text = "未开启"
                view.setTextColor(ColorUtils.getColor(R.color.device_unopened_platform))
            }

            "1" -> {
                view.text = "已上线"
                view.setTextColor(ColorUtils.getColor(R.color.device_online_platform))
            }

            "2" -> {
                view.text = "未上线"
                view.setTextColor(ColorUtils.getColor(R.color.device_offline_platform))
            }
        }
    }
}