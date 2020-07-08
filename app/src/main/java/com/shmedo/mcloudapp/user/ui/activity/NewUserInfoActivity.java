package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.ui.activity.LoginActivity;
import com.shmedo.mcloudapp.user.util.GlobalUtil;
import com.shmedo.mcloudapp.util.ActivityCollector;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.permission.UpdataManagerUtil;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 用户信息页面
 */
public class NewUserInfoActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.userAvatar)
    CircleImageView mIvUserAvatar;

    @BindView(R.id.userName)
    TextView mTvUserName;

    @BindView(R.id.userTitle)
    TextView mTvUserTitle;

    @BindView(R.id.tv_companyName)
    TextView mTvCompanyName;

    @BindView(R.id.tv_versionName)
    TextView mTvVersionName;

    private UserInfo userInfo;
    private UserInfo.UserBean user;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, NewUserInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_new_user_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.translate_in_from_left, R.anim.translate_out);
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("我的");
        initView();
    }

    private void initView() {
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            user = userInfo.getUser();
            if (user.getHeadPhotoPath() != null) {
                GlideUtils.loadImage(this, user.getHeadPhotoPath(), mIvUserAvatar, R.drawable.userphoto);
            }
            mTvUserName.setText(user.getName() != null ? user.getName() : "");
            mTvUserTitle.setText(user.getPosition() != null ? user.getPosition() : "");
            mTvCompanyName.setText("");
            mTvVersionName.setText(GlobalUtil.getAppVersionName());
        }
    }

    @OnClick({R.id.userLayout, R.id.companyLayout, R.id.updatePwdLayout, R.id.checkVersionLayout, R.id.aboutLayout, R.id.btn_exit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.userLayout:
                break;

            case R.id.companyLayout:
                CompanyHomePageActivity.startActivity(this);
                break;

            case R.id.updatePwdLayout:
                UpdatePasswordActivity.startActivity(this);
                break;

            case R.id.checkVersionLayout:
                UpdataManagerUtil.requestPermissionForInstallPackage(this, true);
                break;

            case R.id.aboutLayout:
                AboutAppActivity.startActivity(this);
                break;

            case R.id.btn_exit:
                exitApp();
                break;
        }
    }

    /**
     * 退出app
     */
    private void exitApp() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("提示")
                .content(getResources().getString(R.string.exit_login_tip))
                .negativeText("取消")
                .positiveText("确定")
                .negativeColor(getResources().getColor(R.color.font_main))
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();

                        MCloudApp.logout();
                        ActivityCollector.finishAll();
                        exitLogin();   //注销账号
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 跳转到登录页面
     */
    private void exitLogin() {
        Intent in = new Intent(this, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.translate_out_to_left);
    }
}

