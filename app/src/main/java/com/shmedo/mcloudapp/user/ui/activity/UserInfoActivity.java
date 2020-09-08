package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.ActivityCollector;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.ui.activity.LoginActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.CompanyInfo;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.ResponseHandler;
import com.shmedo.mcloudapp.util.permission.UpdataManagerUtil;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 用户信息页面
 */
public class UserInfoActivity extends BaseActivity {
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

    private CompanyInfo mCompanyInfo;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_user_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("我的");
        getCompanyInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateView();
    }

    private void updateView() {
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            user = userInfo.getUser();
            if (user.getHeadPhotoPath() != null) {
                GlideUtils.loadImage(this, user.getHeadPhotoPath(), mIvUserAvatar, R.drawable.ic_avatar_default);
            }
            mTvUserName.setText(user.getName() != null ? user.getName() : "");
            mTvUserTitle.setText(user.getPosition() != null ? user.getPosition() : "");
            mTvVersionName.setText(GlobalUtil.getAppVersionName());
        }
    }

    @OnClick({R.id.userLayout, R.id.companyLayout, R.id.updatePwdLayout, R.id.checkVersionLayout, R.id.aboutLayout, R.id.btn_exit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userLayout:
                UserHomePageActivity.startActivity(this);
                break;

            case R.id.companyLayout:
                CompanyHomePageActivity.startActivity(this, mCompanyInfo);
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
                .canceledOnTouchOutside(false)
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
                        redirectToLoginActivity();   //注销账号
                    }
                });

        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 跳转到登录页面
     */
    private void redirectToLoginActivity() {
//        Intent in = new Intent(this, LoginActivity.class);
//        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(in);
//        finish();

        LoginActivity.startActivity(this);
    }

    /**
     * 查询单个公司信息
     */
    private void getCompanyInfo() {
        showLoadingDialog("加载中...");

        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, "");
        MDRetrofit.getInstance()
                .createService()
                .GetCompanyInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<CompanyInfo>() {
                    @Override
                    protected void onResponse(CompanyInfo companyInfo, ErrCode errCode) {
                        dismissLoadingDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                mCompanyInfo = companyInfo;
                                if (mCompanyInfo != null) {
                                    mTvCompanyName.setText(mCompanyInfo.getFullName() != null ? mCompanyInfo.getFullName() : "");
                                }
                            }else{
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }


}

