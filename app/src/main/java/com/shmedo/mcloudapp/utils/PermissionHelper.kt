package com.shmedo.mcloudapp.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.showMessage

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/12/5 <br/>
 * 描述：     TODO
 */
object PermissionHelper {

    val foregroundLocationPermissions: Array<String>
        get() {
            return arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }

    val backgroundLocationPermissions: Array<String>
        get() {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            }
        }

    /**
     * Checks for required permissions.
     *
     * @return True if permissions are already granted, false otherwise.
     */
    fun isLocationPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            Utils.getApp().applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    }

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