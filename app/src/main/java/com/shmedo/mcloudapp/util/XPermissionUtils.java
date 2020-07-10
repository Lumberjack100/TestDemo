package com.shmedo.mcloudapp.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  eMeas
 * 包名：    com.shmedo.emeas.util
 * 文件名:   XPermissionUtils
 * 创建者:   dpc
 * 创建时间:  2017/7/18 上午9:16
 * 描述：    6.0权限工具类
 */

public class XPermissionUtils {

    public static final int REQUEST_CODE_OPEN_APPLICATION_SETTING = 0x0010;

    public static final int REQUEST_CODE_SCAN = 0x1008;

    private static int mRequestCode = -1;

    public static void requestPermissionsResult(Activity activity, int requestCode
            , String[] permission, OnPermissionListener callback) {
        requestPermissions(activity, requestCode, permission, callback);
    }

    public static void requestPermissionsResult(android.app.Fragment fragment, int requestCode
            , String[] permission, OnPermissionListener callback) {
        requestPermissions(fragment, requestCode, permission, callback);
    }

    public static void requestPermissionsResult(Fragment fragment, int requestCode
            , String[] permission, OnPermissionListener callback) {
        requestPermissions(fragment, requestCode, permission, callback);
    }

    /**
     * 请求权限处理
     *
     * @param object      activity or fragment
     * @param requestCode 请求码
     * @param permissions 需要请求的权限
     * @param callback    结果回调
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static void requestPermissions(Object object, int requestCode
            , String[] permissions, OnPermissionListener callback) {

        checkCallingObjectSuitability(object);
        mOnPermissionListener = callback;

        List<String> deniedPermissions = getDeniedPermissions(getContext(object), permissions);
        if (!deniedPermissions.isEmpty()) {
            mRequestCode = requestCode;
            if (object instanceof Activity) {
                ((Activity) object).requestPermissions(deniedPermissions
                        .toArray(new String[deniedPermissions.size()]), requestCode);
            } else if (object instanceof android.app.Fragment) {
                ((android.app.Fragment) object).requestPermissions(deniedPermissions
                        .toArray(new String[deniedPermissions.size()]), requestCode);
            } else if (object instanceof Fragment) {
                ((Fragment) object).requestPermissions(deniedPermissions
                        .toArray(new String[deniedPermissions.size()]), requestCode);
            } else {
                mRequestCode = -1;
            }
        } else {
            if (mOnPermissionListener != null)
                mOnPermissionListener.onPermissionGranted();
        }
    }

    /**
     * 获取上下文
     */
    private static Context getContext(Object object) {
        Context context;
        if (object instanceof android.app.Fragment) {
            context = ((android.app.Fragment) object).getActivity();
        } else if (object instanceof Fragment) {
            context = ((Fragment) object).getActivity();
        } else {
            context = (Activity) object;
        }
        return context;
    }

    /**
     * 请求权限结果，对应onRequestPermissionsResult()方法。
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mRequestCode != -1 && requestCode == mRequestCode) {
            if (grantResults.length > 0) {
                List<String> deniedPermissions = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    int grantResult = grantResults[i];
                    String permission = permissions[i];
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permission);
                    }
                }

                if (deniedPermissions.isEmpty()) {
                    if (mOnPermissionListener != null)
                        mOnPermissionListener.onPermissionGranted();
                } else {
                    if (mOnPermissionListener != null)
                        mOnPermissionListener.onPermissionDenied(deniedPermissions);
                }
            }
        }
    }


    /**
     * 获取权限列表中所有需要授权的权限
     *
     * @param context     上下文
     * @param permissions 权限列表
     * @return
     */
    private static List<String> getDeniedPermissions(Context context, String... permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        if (!isOverMarshmallow()) {
            return new ArrayList<>();
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    /**
     * 检查所传递对象的正确性
     *
     * @param object 必须为 activity or fragment
     */
    private static void checkCallingObjectSuitability(Object object) {
        if (object == null) {
            throw new NullPointerException("Activity or Fragment should not be null");
        }

        boolean isActivity = object instanceof Activity;
        boolean isSupportFragment = object instanceof Fragment;
        boolean isAppFragment = object instanceof android.app.Fragment;

        if (!(isActivity || isSupportFragment || isAppFragment)) {
            throw new IllegalArgumentException("Caller must be an Activity or a Fragment");
        }
    }

    /**
     * 检查所有的权限是否已经被授权
     *
     * @param permissions 权限列表
     * @return
     */
    public static boolean checkPermissions(Context context, String... permissions) {
        if (isOverMarshmallow()) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAllNeverAskAgain(Activity activity,List<String> deniedPermissions){
        boolean allNeverAskAgain = true;
        for (String deniedPermission : deniedPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)) {
                allNeverAskAgain = false;
                break;
            }
        }

        return allNeverAskAgain;
    }


    /**
     * 显示提示对话框
     */
    public static void showRefusePermissionDialog(final Object object, String message) {
        MaterialDialog.Builder builderRefuse = new MaterialDialog.Builder(getContext(object))
                .title("权限申请")
                .content(message)
                .negativeText("取消")
                .positiveText("去设置")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();

                        if (object instanceof Fragment) {
                            startAppSettings((Fragment) object);
                        } else {
                            startAppSettings((Activity) object);
                        }
                    }
                });

        builderRefuse.show();
    }

    /**
     * 启动当前应用设置页面
     */
    public static void startAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, REQUEST_CODE_OPEN_APPLICATION_SETTING);
    }


    /**
     * 启动当前应用设置页面
     */
    public static void startAppSettings(Fragment fragment) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + fragment.getContext().getPackageName()));
        fragment.startActivityForResult(intent, REQUEST_CODE_OPEN_APPLICATION_SETTING);
    }

    /**
     * 判断当前手机API版本是否 >= 6.0
     */
    private static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public interface OnPermissionListener {
        void onPermissionGranted();

        void onPermissionDenied(List<String> deniedPermissions);
    }

    private static OnPermissionListener mOnPermissionListener;
}
