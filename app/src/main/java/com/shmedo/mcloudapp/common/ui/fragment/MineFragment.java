package com.shmedo.mcloudapp.common.ui.fragment;

import static autodispose2.AutoDispose.autoDisposable;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserWrapperInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.LoginActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.user.model.CompanyInfo;
import com.shmedo.mcloudapp.user.ui.activity.AboutAppActivity;
import com.shmedo.mcloudapp.user.ui.activity.CompanyHomePageActivity;
import com.shmedo.mcloudapp.user.ui.activity.UpdatePasswordActivity;
import com.shmedo.mcloudapp.user.ui.activity.UserHomePageActivity;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.ResponseHandler;
import com.shmedo.mcloudapp.util.UpdataManagerUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
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

    private UserWrapperInfo userWrapperInfo;
    private UserWrapperInfo.UserInfo user;
    private int companyID;
    private CompanyInfo mCompanyInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mine;
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
        userWrapperInfo = MCloudApp.getCurrentUserInfo();
        if (userWrapperInfo != null && userWrapperInfo.getUser() != null) {
            user = userWrapperInfo.getUser();
            if (user.getHeadPhotoPath() != null) {
                GlideUtils.loadImage(getActivity(), user.getHeadPhotoPath(), mIvUserAvatar, R.drawable.ic_avatar_default);
            }
            mTvUserName.setText(user.getName());
            mTvUserTitle.setText(user.getPosition());
            mTvVersionName.setText(AppUtils.getAppVersionName());
        }
    }

    @OnClick({R.id.userLayout, R.id.companyLayout, R.id.updatePwdLayout, R.id.checkVersionLayout, R.id.aboutLayout, R.id.loginOutLayout})
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
                UpdataManagerUtil.checkNewVersion2(getActivity(), true);
                break;

            case R.id.aboutLayout:
                AboutAppActivity.startActivity(getActivity());
                break;

            case R.id.loginOutLayout:
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
                .content(StringUtils.getString(R.string.exit_login_tip))
                .canceledOnTouchOutside(false)
                .negativeText("取消")
                .positiveText("确定")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
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
        MCloudApp.logout();
        //登出
        MobclickAgent.onProfileSignOff();
        LoginActivity.startActivity(getActivity());
    }

    /**
     * 查询单个公司信息
     */
    private void getCompanyInfo() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", companyID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService()
                .getCompanyInfo(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<CompanyInfo>() {
                    @Override
                    protected void onResponse(CompanyInfo companyInfo, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                mCompanyInfo = companyInfo;
                                if (mCompanyInfo != null) {
                                    mTvCompanyName.setText(mCompanyInfo.getFullName() != null ? mCompanyInfo.getFullName() : "");
                                }
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
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
