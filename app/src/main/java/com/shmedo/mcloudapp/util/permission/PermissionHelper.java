package com.shmedo.mcloudapp.util.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.ScanActivity;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/10 <br/>
 * 描述：     权限申请帮助类
 */
public class PermissionHelper {
    public static final int REQUEST_CODE_OPEN_GPS = 0x1000;
    public static final int REQUEST_CODE_LOCATION = 0x1001;
    public static final int REQUEST_CODE_GPS_LOCATION = 0x1002;
    public static final int REQUEST_CODE_NAVI = 0x1002;
    public static final int REQUEST_CODE_ROUTE = 0x1003;


    public static void requestScanPermissions(Fragment fragment) {
        XPermissionUtils.requestPermissionsResult(fragment, 200, new String[]{
                        Manifest.permission.CAMERA},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        ScanActivity.startActivityForResultByFragment(fragment, XPermissionUtils.REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(fragment.getActivity(), deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(fragment.getActivity(), GlobalUtil.getString(R.string.message_permission_camera_rationale));
                        } else {
                            ToastUtils.show(GlobalUtil.getString(R.string.message_permission_camera_denied));
                        }
                    }
                });
    }

    public static void showGPSSettingDialog(Activity activity) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(activity)
                .title("权限申请").content(GlobalUtil.getString(R.string.permission_request_location_hardware))
                .negativeText("暂不开启")
                .positiveText("去设置")
                .negativeColor(GlobalUtil.getColor(R.color.gray_797979))
                .positiveColor(GlobalUtil.getColor(R.color.colorPrimary))
                .canceledOnTouchOutside(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

}
