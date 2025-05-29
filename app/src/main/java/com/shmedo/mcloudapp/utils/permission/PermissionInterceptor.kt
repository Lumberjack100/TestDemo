package com.shmedo.mcloudapp.utils.permission

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.blankj.utilcode.util.StringUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.OnPermissionInterceptor
import com.hjq.permissions.OnPermissionPageCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.shmedo.lib.core.R
import timber.log.Timber


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/7/25 <br/>
 * 描述：     TODO
 */
class PermissionInterceptor : OnPermissionInterceptor {

//    override fun launchPermissionRequest(
//        activity: Activity,
//        allPermissions: MutableList<String>,
//        callback: OnPermissionCallback?
//    ) {
//        val deniedPermissions: List<String> = XXPermissions.getDenied(activity, allPermissions)
//        val message: String = StringUtils.getString(
//            R.string.common_permission_message,
//            PermissionNameConvert.getPermissionString(deniedPermissions)
//        )
//
//        MessageDialog.show(
//            StringUtils.getString(R.string.common_permission_description),
//            message,
//            StringUtils.getString(R.string.common_permission_granted)
//        )
//            .setOkButton { dialog, v ->
//                super.launchPermissionRequest( activity,allPermissions,callback)
//
//                false
//            }
//    }


    override fun grantedPermissionRequest(
        activity: Activity,
        allPermissions: MutableList<String>,
        grantedPermissions: MutableList<String>,
        allGranted: Boolean,
        callback: OnPermissionCallback?
    ) {
        callback?.onGranted(grantedPermissions, allGranted)
    }

    override fun deniedPermissionRequest(
        activity: Activity,
        allPermissions: MutableList<String>,
        deniedPermissions: MutableList<String>,
        doNotAskAgain: Boolean,
        callback: OnPermissionCallback?
    ) {
        callback?.onDenied(deniedPermissions, doNotAskAgain)

        if (doNotAskAgain) {
            if (deniedPermissions.size == 1 && Permission.ACCESS_MEDIA_LOCATION == deniedPermissions[0]) {
                Toaster.show(R.string.common_permission_media_location_hint_fail)
                return
            }
            showPermissionSettingDialog(activity, allPermissions, deniedPermissions, callback)
            return
        }

        if (deniedPermissions.size == 1) {
            val deniedPermission = deniedPermissions[0]
            val backgroundPermissionOptionLabel = getBackgroundPermissionOptionLabel(activity)

            if (Permission.ACCESS_BACKGROUND_LOCATION == deniedPermission) {
                Toaster.show(
                    StringUtils.getString(
                        R.string.common_permission_background_location_fail_hint,
                        backgroundPermissionOptionLabel
                    )
                )
                return
            }

            if (Permission.BODY_SENSORS_BACKGROUND == deniedPermission) {
                Toaster.show(
                    StringUtils.getString(
                        R.string.common_permission_background_sensors_fail_hint,
                        backgroundPermissionOptionLabel
                    )
                )
                return
            }
        }

        val permissionNames: List<String> =
            PermissionNameConvert.permissionsToNames(deniedPermissions)
        val message: String = if (permissionNames.isNotEmpty()) {
            StringUtils.getString(
                R.string.common_permission_fail_assign_hint,
                PermissionNameConvert.listToString(permissionNames)
            )
        } else {
            StringUtils.getString(R.string.common_permission_fail_hint)
        }

        Toaster.show(message)
    }

    override fun finishPermissionRequest(
        activity: Activity,
        allPermissions: MutableList<String>,
        skipRequest: Boolean,
        callback: OnPermissionCallback?
    ) {
        super.finishPermissionRequest(activity, allPermissions, skipRequest, callback)
    }

    private fun showPermissionSettingDialog(
        activity: Activity?,
        allPermissions: List<String>,
        deniedPermissions: List<String>,
        callback: OnPermissionCallback?
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return
        }

        val permissionNames: List<String> =
            PermissionNameConvert.permissionsToNames(deniedPermissions)

        val message: String = if (permissionNames.isEmpty()) {
            StringUtils.getString(R.string.common_permission_manual_fail_hint)
        } else {
            if (deniedPermissions.size == 1) {
                val deniedPermission = deniedPermissions[0]
                if (Permission.ACCESS_BACKGROUND_LOCATION == deniedPermission) {
                    StringUtils.getString(
                        R.string.common_permission_manual_assign_fail_background_location_hint,
                        getBackgroundPermissionOptionLabel(activity)
                    )
                } else if (Permission.BODY_SENSORS_BACKGROUND == deniedPermission) {
                    StringUtils.getString(
                        R.string.common_permission_manual_assign_fail_background_sensors_hint,
                        getBackgroundPermissionOptionLabel(activity)
                    )
                }
            }
            StringUtils.getString(
                R.string.common_permission_manual_assign_fail_hint,
                PermissionNameConvert.listToString(permissionNames)
            )
        }
        (activity as AppCompatActivity).showWarn(
            message,
            "授权提醒",
            positiveButtonText = "前往授权",
            positiveAction = {
                Timber.e("showPermissionSettingDialog positiveAction click")

                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                XXPermissions.startPermissionActivity(
                    activity, deniedPermissions, object : OnPermissionPageCallback {
                        override fun onGranted() {
                            callback?.onGranted(allPermissions, true)
                        }

                        override fun onDenied() {
//                            showPermissionSettingDialog(
//                                activity,
//                                allPermissions,
//                                XXPermissions.getDenied(activity, allPermissions),
//                                callback
//                            )
                        }
                    })
            })

    }

    /**
     * 获取后台权限的《始终允许》选项的文案
     */
    private fun getBackgroundPermissionOptionLabel(context: Context): String? {
        var backgroundPermissionOptionLabel = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            backgroundPermissionOptionLabel =
                context.packageManager.backgroundPermissionOptionLabel.toString()
        }
        if (TextUtils.isEmpty(backgroundPermissionOptionLabel)) {
            backgroundPermissionOptionLabel =
                context.getString(R.string.common_permission_background_default_option_label)
        }
        return backgroundPermissionOptionLabel
    }

    private fun AppCompatActivity.showWarn(
        message: String,
        title: String = "温馨提示",
        positiveButtonText: String = "确定",
        positiveAction: () -> Unit = {},
        negativeButtonText: String = "",
        negativeAction: () -> Unit = {}
    ) {
        MaterialDialog(this).cancelable(true).lifecycleOwner(this).show {
            title(text = title)
            message(text = message)
            positiveButton(text = positiveButtonText) {
                positiveAction.invoke()
            }
            if (negativeButtonText.isNotEmpty()) {
                negativeButton(text = negativeButtonText) {
                    negativeAction.invoke()
                }
            }
        }
    }
}