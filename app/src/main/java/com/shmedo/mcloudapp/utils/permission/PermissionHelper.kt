package com.shmedo.mcloudapp.utils.permission

import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.shmedo.lib.core.R
import com.shmedo.mcloudapp.extensions.showMessage

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/12/5 <br/>
 * 描述：     TODO
 */
object PermissionHelper {
    const val REQUEST_CODE_SCAN = 0x1008
    const val REQUEST_CODE_QUICK_CONFIG_SCAN = 0x1009  // 新增：快速配置专用扫码
    


    fun isLocationEnabled(): Boolean {
        val lm = Utils.getApp().applicationContext.getSystemService(LocationManager::class.java)
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    fun showGPSSettingDialog(
        activity: AppCompatActivity, launcher: ActivityResultLauncher<Intent>
    ) {
        activity.apply {
            showMessage(
                StringUtils.getString(R.string.permission_request_location_hardware),
                "权限申请",
                positiveButtonText = "去设置",
                negativeButtonText = "取消",
                positiveAction = {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    launcher.launch(intent)
                })
        }
    }
}