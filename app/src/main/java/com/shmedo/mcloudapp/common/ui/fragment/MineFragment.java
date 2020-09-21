package com.shmedo.mcloudapp.common.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.ActivityCollector;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.NewLoginActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.CompanyInfo;
import com.shmedo.mcloudapp.user.ui.activity.AboutAppActivity;
import com.shmedo.mcloudapp.user.ui.activity.CompanyHomePageActivity;
import com.shmedo.mcloudapp.user.ui.activity.UpdatePasswordActivity;
import com.shmedo.mcloudapp.user.ui.activity.UserHomePageActivity;
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
 * 我的模块主页面
 */
public class MineFragment extends BaseTranslucentFragment {
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
    private int companyID;
    private CompanyInfo mCompanyInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mine;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView();
        if (companyID != MCloudApp.getCompanyID()) {
            companyID = MCloudApp.getCompanyID();
            getCompanyInfo();
        }
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
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
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
//        Intent intent = new Intent(getActivity(), LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        mActivity.finish();

        NewLoginActivity.startActivity(getActivity());
    }

    /**
     * 查询单个公司信息
     */
    private void getCompanyInfo() {
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, String.valueOf(companyID));
        MDRetrofit.getInstance()
                .createService()
                .GetCompanyInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<CompanyInfo>() {
                    @Override
                    protected void onResponse(CompanyInfo companyInfo, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                mCompanyInfo = companyInfo;
                                if (mCompanyInfo != null) {
                                    mTvCompanyName.setText(mCompanyInfo.getFullName() != null ? mCompanyInfo.getFullName() : "");
                                }
                            } else {
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

}
