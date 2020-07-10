package com.shmedo.mcloudapp.util.permission;

import android.Manifest;
import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.dragon.core.util.GlobalUtil;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.ui.activity.MainActivity;
import com.shmedo.mcloudapp.ui.activity.ScanActivity;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/10 <br/>
 * 描述：     权限申请帮助类
 */
public class PermissionHelper {

    public static void requestScanPermissions(Activity activity) {
        XPermissionUtils.requestPermissionsResult(activity, 200, new String[]{
                        Manifest.permission.CAMERA},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        ScanActivity.startActivityForResult(activity, MainActivity.REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(activity, deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(activity, GlobalUtil.getString(R.string.message_permission_camera_rationale));
                        } else {
                            ToastUtils.show(GlobalUtil.getString(R.string.message_permission_camera_denied));
                        }
                    }
                });
    }

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

}
