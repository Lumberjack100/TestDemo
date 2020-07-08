package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.user.model.UpdateMyInfoParam;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.views.ClearEditText;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 用户个人详细信息页
 */
public class UserHomePageActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

//    @BindView(R.id.userAvatar)
//    CircleImageView mIvUserAvatar;

    @BindView(R.id.userNameET)
    ClearEditText mEtUserName;

    @BindView(R.id.titleET)
    ClearEditText mEtTitle;

    @BindView(R.id.emailET)
    ClearEditText mEtEmail;

    @BindView(R.id.tv_phone)
    TextView mTvPhone;

    private UserInfo userInfo;
    private UserInfo.UserBean user;

    private String userName, title, email;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UserHomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_user_home_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("我的信息");
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
//                GlideUtils.loadImage(this, user.getHeadPhotoPath(), mIvUserAvatar, R.drawable.userphoto);
            }
            mEtUserName.setText(user.getName() != null ? user.getName() : "");
            mEtTitle.setText(user.getPosition() != null ? user.getPosition() : "");
            mEtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            mTvPhone.setText(user.getCellPhone() != null ? user.getCellPhone() : "");
        }
    }


    @OnClick({R.id.mobileLayout, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mobileLayout:
                UpdatePhoneActivity.startActivity(this);
                break;

            case R.id.btn_confirm:
                preProcessParam();
                break;
        }
    }

    private void preProcessParam() {
        userName = mEtUserName.getText().toString();
        title = mEtTitle.getText().toString();
        email = mEtEmail.getText().toString();

        if (TextUtils.isEmpty(userName)) {
            ToastUtils.show("请输入用户名");
            mEtUserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            ToastUtils.show("请输入职位");
            mEtTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            ToastUtils.show("请输入邮箱");
            mEtEmail.requestFocus();
            return;
        }
        updateMyInfo();
    }

    private void updateMyInfo() {
        showLoadingDialog("处理中...");

        UpdateMyInfoParam parameter = new UpdateMyInfoParam();
        parameter.setName(userName);
        parameter.setPosition(title);
        parameter.setEmail(email);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance()
                .createService()
                .UpdateMyInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String result, String message) {
                        dismissLoadingDialog();
                        ToastUtils.show("修改完成");

                        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
                        if (userInfo != null && userInfo.getUser() != null) {
                            UserInfo.UserBean user = userInfo.getUser();
                            user.setName(userName);
                            user.setPosition(title);
                            user.setEmail(email);
                        }
                        MCloudApp.setCurrentUserInfo(userInfo);
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        ToastUtils.show(message);
                        Timber.w("请求失败--%s", message);
                    }
                });
    }
}
