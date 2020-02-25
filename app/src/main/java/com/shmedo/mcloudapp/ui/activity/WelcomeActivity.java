package com.shmedo.mcloudapp.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.LoginManager;
import com.shmedo.mcloudapp.util.StartActivityUtil;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   WelcomeActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:17
 */
public class WelcomeActivity extends BaseActivity implements LoginManager.LoginCallback {

    private MaterialDialog mMaterialDialog;

    private UserConfig userConfig;

    private String mAccount = null;

    private String mPassword = null;


    @Override
    protected int initContentView() {
        return R.layout.activity_welcome;
    }


    /**
     * 沉浸式状态栏
     */
    private void initStates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStates();
        setCheckNetWork(false);
        initViewAndData();
        checkPermission();
    }

    private void initViewAndData() {
        userConfig = UserConfig.getConfig(this, CommonVariable.USER_CONFIG_NAME);
        mAccount = userConfig.readString(CommonVariable.UID);
        mPassword = userConfig.readString(CommonVariable.PWD);
    }


    private void makeAutoLogin(String account, String password) {
//        showLoadingDialog("正在登录...");
        LoginManager.getInstance().login(account, password, this);
    }


    @Override
    public void callback(int code, Object data) {
//        dismissLoadingDialog();
        if (LoginManager.LOGIN_CODE_SUCCESS == code) {
            redirectToMainActivity();

        } else {
            redirectToLoginActivity();
        }
    }


    /**
     * 跳转到登录界面
     */
    private void redirectToLoginActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                StartActivityUtil.comeOnBaby(WelcomeActivity.this, LoginActivity.class);
                finish();
            }
        }, 2000);
    }

    /**
     * 跳转到主界面
     */
    private void redirectToMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.start(WelcomeActivity.this);
                finish();
            }
        }, 2000);
    }


    private void checkPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        //自动登录
                        if (!TextUtils.isEmpty(mAccount) && !TextUtils.isEmpty(mPassword)) {
                            makeAutoLogin(mAccount, mPassword);
                        } else {
                            redirectToLoginActivity();
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if (mMaterialDialog != null && !mMaterialDialog.isShowing()) {
                            mMaterialDialog.show();

                        } else {
                            showRefusePermissionDialog();
                        }
                    }
                })
                .start();
    }


    private void showRefusePermissionDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(WelcomeActivity.this)
                .title("权限申请").content(getResources().getString(R.string.permission_external_storage))
                .negativeText("取消")
                .positiveText("去设置")
                .negativeColor(getResources().getColor(R.color.font_main))
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        XPermissionUtils.startAppSettings(WelcomeActivity.this);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == XPermissionUtils.CODE_REQUEST_PERMISSIONS) {
            checkPermission();
        }
    }

}
