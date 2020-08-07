package com.shmedo.mcloudapp.common.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.ActivityCollector;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.LoginActivity;
import com.shmedo.mcloudapp.common.ui.activity.NewMainActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.CompanyInfo;
import com.shmedo.mcloudapp.user.ui.activity.AboutAppActivity;
import com.shmedo.mcloudapp.user.ui.activity.CompanyHomePageActivity;
import com.shmedo.mcloudapp.user.ui.activity.UpdatePasswordActivity;
import com.shmedo.mcloudapp.user.ui.activity.UserHomePageActivity;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.permission.UpdataManagerUtil;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 我的模块主页面
 */
public class MineFragment extends BaseFragment {
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

    private NewMainActivity activity;
    private UserInfo userInfo;
    private UserInfo.UserBean user;

    private CompanyInfo mCompanyInfo;

    @Override
    protected int initContentView() {
        return R.layout.fragment_mine;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (NewMainActivity) getActivity();
        getCompanyInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView();
    }

    private void updateView() {
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            user = userInfo.getUser();
            if (user.getHeadPhotoPath() != null) {
                GlideUtils.loadImage(getActivity(), user.getHeadPhotoPath(), mIvUserAvatar, R.drawable.ic_avatar_default);
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
                UserHomePageActivity.startActivity(getActivity());
                break;

            case R.id.companyLayout:
                CompanyHomePageActivity.startActivity(getActivity(), mCompanyInfo);
                break;

            case R.id.updatePwdLayout:
                UpdatePasswordActivity.startActivity(getActivity());
                break;

            case R.id.checkVersionLayout:
                UpdataManagerUtil.requestPermissionForInstallPackage(getActivity(), true);
                break;

            case R.id.aboutLayout:
                AboutAppActivity.startActivity(getActivity());
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
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity())
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
        Intent in = new Intent(getActivity(), LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        activity.finish();
    }

    /**
     * 查询单个公司信息
     */
    private void getCompanyInfo() {
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, "");
        MDRetrofit.getInstance()
                .createService()
                .GetCompanyInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<CompanyInfo>() {
                    @Override
                    public void Success(CompanyInfo companyInfo, String message) {
                        dismissLoadingDialog();
                        mCompanyInfo = companyInfo;
                        if(mCompanyInfo!=null) {
                            mTvCompanyName.setText(mCompanyInfo.getFullName() != null ? mCompanyInfo.getFullName() : "");
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("服务器连接失败--%s", message);
                    }
                });
    }

}
