package com.shmedo.mcloudapp.util.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;
import com.shmedo.mcloudapp.R;

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

    public static void requestScanPermissions(FragmentActivity activity) {
        PermissionX.init(activity)
                .permissions(Manifest.permission.CAMERA)
//                .explainReasonBeforeRequest()
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "米易通需要以下权限继续", "允许", "拒绝");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "请前往设置页面授予权限", "去设置");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            int result = ScanUtil.startScan(activity, XPermissionUtils.REQUEST_CODE_SCAN, new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE).create());

                        } else {
                            ToastUtils.show("下列权限被拒绝：" + deniedList);
                        }
                    }
                });
    }

    public static void requestScanPermissions(Fragment fragment) {
        PermissionX.init(fragment)
                .permissions(Manifest.permission.CAMERA)
//                .explainReasonBeforeRequest()
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "米易通需要以下权限继续", "允许", "拒绝");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "请前往设置页面授予权限", "去设置");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            int result = ScanUtil.startScan(fragment.getActivity(), XPermissionUtils.REQUEST_CODE_SCAN, new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE).create());

                        } else {
                            ToastUtils.show("下列权限被拒绝：" + deniedList);
                        }
                    }
                });
    }

    public static void showGPSSettingDialog(Activity activity) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(activity)
                .title("权限申请").content(StringUtils.getString(R.string.permission_request_location_hardware))
                .negativeText("暂不开启")
                .positiveText("去设置")
                .negativeColor(ColorUtils.getColor(R.color.gray_797979))
                .positiveColor(ColorUtils.getColor(R.color.colorPrimary))
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
