package com.shmedo.mcloudapp.utils.permission

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.OnPermissionInterceptor
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionGroups
import com.hjq.permissions.permission.PermissionNames
import com.hjq.permissions.permission.base.IPermission
import com.hjq.toast.Toaster
import com.shmedo.lib.core.R
import com.shmedo.mcloudapp.extensions.showWarn

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/7/25 <br/>
 * 描述：     TODO
 */
class PermissionInterceptor : OnPermissionInterceptor {

    override fun onRequestPermissionEnd(
        activity: Activity,
        skipRequest: Boolean,
        requestList: List<IPermission>,
        grantedList: List<IPermission>,
        deniedList: List<IPermission>,
        callback: OnPermissionCallback?
    ) {
        callback?.onResult(grantedList, deniedList)

        if (deniedList.isEmpty()) {
            return
        }

        val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(activity, deniedList)
        val permissionHint = generatePermissionHint(activity, deniedList, doNotAskAgain)

        if (!doNotAskAgain) {
            // 如果没有勾选不再询问选项，就弹 Toast 提示给用户
            Toaster.show(permissionHint)
            return
        }

        // 如果勾选了不再询问选项，就弹 Dialog 引导用户去授权
        showPermissionSettingDialog(activity, requestList, deniedList, callback, permissionHint)
    }

    private fun showPermissionSettingDialog(
        activity: Activity,
        requestList: List<IPermission>,
        deniedList: List<IPermission>,
        callback: OnPermissionCallback?,
        permissionHint: String
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        val dialogTitle = activity.getString(R.string.common_permission_alert)
        val confirmButtonText = activity.getString(R.string.common_permission_go_to_authorization)

        (activity as AppCompatActivity).showWarn(
            dialogTitle,
            permissionHint,
            positiveButtonText = confirmButtonText,
            positiveAction = {
                XXPermissions.startPermissionActivity(activity, deniedList, object : OnPermissionCallback {
                    override fun onResult(grantedList: List<IPermission>, deniedList: List<IPermission>) {
                        val latestDeniedList = XXPermissions.getDeniedPermissions(activity, requestList)
                        val allGranted = latestDeniedList.isEmpty()

                        if (!allGranted) {
                            // 递归显示对话框，让提示用户授权，只不过对话框是可取消的，用户不想授权了，随时可以点击返回键或者对话框蒙层来取消显示
                            showPermissionSettingDialog(
                                activity,
                                requestList,
                                latestDeniedList,
                                callback,
                                generatePermissionHint(activity, latestDeniedList, true)
                            )
                            return
                        }

                        // 用户全部授权了，回调成功给外层监听器，免得用户还要再发起权限申请
                        callback?.onResult(requestList, latestDeniedList)
                    }
                })
            })
    }

    /**
     * 生成权限提示文案
     */
    private fun generatePermissionHint(
        activity: Activity,
        deniedList: List<IPermission>,
        doNotAskAgain: Boolean
    ): String {
        val deniedPermissionCount = deniedList.size
        var deniedLocationPermissionCount = 0
        var deniedSensorsPermissionCount = 0
        var deniedHealthPermissionCount = 0

        for (deniedPermission in deniedList) {
            val permissionGroup = deniedPermission.permissionGroup
            if (TextUtils.isEmpty(permissionGroup)) {
                continue
            }
            when (permissionGroup) {
                PermissionGroups.LOCATION -> deniedLocationPermissionCount++
                PermissionGroups.SENSORS -> deniedSensorsPermissionCount++
                else -> if (XXPermissions.isHealthPermission(deniedPermission)) {
                    deniedHealthPermissionCount++
                }
            }
        }

        if (deniedLocationPermissionCount == deniedPermissionCount && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (deniedLocationPermissionCount == 1) {
                when {
                    XXPermissions.equalsPermission(deniedList[0], PermissionNames.ACCESS_BACKGROUND_LOCATION) -> {
                        return activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_location_background),
                            getBackgroundPermissionOptionLabel(activity)
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            XXPermissions.equalsPermission(deniedList[0], PermissionNames.ACCESS_FINE_LOCATION) -> {
                        // 如果请求的定位权限中，既包含了精确定位权限，又包含了模糊定位权限或者后台定位权限，
                        // 但是用户只同意了模糊定位权限的情况或者后台定位权限，并没有同意精确定位权限的情况，就提示用户开启确切位置选项
                        // 需要注意的是 Android 12 才将模糊定位权限和精确定位权限的授权选项进行分拆，之前的版本没有区分得那么仔细
                        return activity.getString(
                            R.string.common_permission_fail_hint_3,
                            activity.getString(R.string.common_permission_location_fine),
                            activity.getString(R.string.common_permission_location_fine_option)
                        )
                    }
                }
            } else {
                if (XXPermissions.containsPermission(deniedList, PermissionNames.ACCESS_BACKGROUND_LOCATION)) {
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        XXPermissions.containsPermission(deniedList, PermissionNames.ACCESS_FINE_LOCATION)) {
                        activity.getString(
                            R.string.common_permission_fail_hint_2,
                            activity.getString(R.string.common_permission_location),
                            getBackgroundPermissionOptionLabel(activity),
                            activity.getString(R.string.common_permission_location_fine_option)
                        )
                    } else {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_location),
                            getBackgroundPermissionOptionLabel(activity)
                        )
                    }
                }
            }
        } else if (deniedSensorsPermissionCount == deniedPermissionCount && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (deniedPermissionCount == 1) {
                if (XXPermissions.equalsPermission(deniedList[0], PermissionNames.BODY_SENSORS_BACKGROUND)) {
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_health_data_background),
                            activity.getString(R.string.common_permission_health_data_background_option)
                        )
                    } else {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_body_sensors_background),
                            getBackgroundPermissionOptionLabel(activity)
                        )
                    }
                }
            } else {
                if (doNotAskAgain) {
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_health_data),
                            activity.getString(R.string.common_permission_allow_all_option)
                        )
                    } else {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_body_sensors),
                            getBackgroundPermissionOptionLabel(activity)
                        )
                    }
                }
            }
        } else if (deniedHealthPermissionCount == deniedPermissionCount && Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            when (deniedPermissionCount) {
                1 -> {
                    when {
                        XXPermissions.equalsPermission(deniedList[0], PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND) -> {
                            return activity.getString(
                                R.string.common_permission_fail_hint_3,
                                activity.getString(R.string.common_permission_health_data_background),
                                activity.getString(R.string.common_permission_health_data_background_option)
                            )
                        }
                        XXPermissions.equalsPermission(deniedList[0], PermissionNames.READ_HEALTH_DATA_HISTORY) -> {
                            return activity.getString(
                                R.string.common_permission_fail_hint_3,
                                activity.getString(R.string.common_permission_health_data_past),
                                activity.getString(R.string.common_permission_health_data_past_option)
                            )
                        }
                    }
                }
                2 -> {
                    when {
                        XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_HISTORY) &&
                                XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND) -> {
                            return activity.getString(
                                R.string.common_permission_fail_hint_3,
                                activity.getString(R.string.common_permission_health_data_past) +
                                        activity.getString(R.string.common_permission_and) +
                                        activity.getString(R.string.common_permission_health_data_background),
                                activity.getString(R.string.common_permission_health_data_past_option) +
                                        activity.getString(R.string.common_permission_and) +
                                        activity.getString(R.string.common_permission_health_data_background_option)
                            )
                        }
                        XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_HISTORY) -> {
                            return activity.getString(
                                R.string.common_permission_fail_hint_2,
                                activity.getString(R.string.common_permission_health_data) +
                                        activity.getString(R.string.common_permission_and) +
                                        activity.getString(R.string.common_permission_health_data_past),
                                activity.getString(R.string.common_permission_allow_all_option),
                                activity.getString(R.string.common_permission_health_data_background_option)
                            )
                        }
                        XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND) -> {
                            return activity.getString(
                                R.string.common_permission_fail_hint_2,
                                activity.getString(R.string.common_permission_health_data) +
                                        activity.getString(R.string.common_permission_and) +
                                        activity.getString(R.string.common_permission_health_data_background),
                                activity.getString(R.string.common_permission_allow_all_option),
                                activity.getString(R.string.common_permission_health_data_background_option)
                            )
                        }
                    }
                }
                else -> {
                    if (XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_HISTORY) &&
                        XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND)) {
                        return activity.getString(
                            R.string.common_permission_fail_hint_2,
                            activity.getString(R.string.common_permission_health_data) +
                                    activity.getString(R.string.common_permission_and) +
                                    activity.getString(R.string.common_permission_health_data_past) +
                                    activity.getString(R.string.common_permission_and) +
                                    activity.getString(R.string.common_permission_health_data_background),
                            activity.getString(R.string.common_permission_allow_all_option),
                            activity.getString(R.string.common_permission_health_data_past_option) +
                                    activity.getString(R.string.common_permission_and) +
                                    activity.getString(R.string.common_permission_health_data_background_option)
                        )
                    }
                }
            }
            return activity.getString(
                R.string.common_permission_fail_hint_1,
                activity.getString(R.string.common_permission_health_data),
                activity.getString(R.string.common_permission_allow_all_option)
            )
        }

        return activity.getString(
            if (doNotAskAgain) R.string.common_permission_fail_assign_hint_1
            else R.string.common_permission_fail_assign_hint_2,
            PermissionConverter.getNickNamesByPermissions(activity, deniedList)
        )
    }

    /**
     * 获取后台权限的《始终允许》选项的文案
     */
    private fun getBackgroundPermissionOptionLabel(context: Context): String {
        val packageManager = context.packageManager
        if (packageManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val backgroundPermissionOptionLabel = packageManager.backgroundPermissionOptionLabel
            if (!TextUtils.isEmpty(backgroundPermissionOptionLabel)) {
                return backgroundPermissionOptionLabel.toString()
            }
        }

        return context.getString(R.string.common_permission_allow_all_the_time_option)
    }
}