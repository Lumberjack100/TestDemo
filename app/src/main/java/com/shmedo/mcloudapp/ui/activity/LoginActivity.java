package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.LoginManager;
import com.shmedo.mcloudapp.util.MyCountDownTimer;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.views.ClearEditText;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   LoginActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/8 10:09
 */
public class LoginActivity extends BaseActivity implements LoginManager.LoginCallback {

    @BindView(R.id.user_image)
    CircleImageView mUserImage;

    @BindView(R.id.login_accountLogin)
    TextView mLoginAccountLogin;

    @BindView(R.id.login_quickLogin)
    TextView mLoginQuickLogin;

    @BindView(R.id.login_select_left_line)
    TextView mLoginSelectLeftLine;

    @BindView(R.id.login_select_right_line)
    TextView mLoginSelectRightLine;

    @BindView(R.id.ll_account_login)
    LinearLayout mLlAccountLogin;

    @BindView(R.id.ll_phone_login)
    LinearLayout mLlPhoneLogin;

    @BindView(R.id.login_editText_account)
    ClearEditText mLoginEditTextAccount;

    @BindView(R.id.login_account_password)
    ClearEditText mLoginAccountPassword;

    @BindView(R.id.login_editText_iphone)
    ClearEditText mLoginEditTextIphone;

    @BindView(R.id.login_phone_password)
    ClearEditText mLoginPhonePassword;

    @BindView(R.id.btn_get_code)
    Button mBtnGetCode;

    @BindView(R.id.server_config)
    ImageView mServerConfig;

    @BindView(R.id.btn_login_account)
    Button mBtnLoginAccount;

    @BindView(R.id.btn_login_phone)
    Button mBtnLoginPhone;

    @BindView(R.id.tv_registered)
    TextView mTvRegistered;

    @BindView(R.id.tv_forgot_password)
    TextView mTvForgotPassword;

    @BindView(R.id.tourists_login)
    TextView mTouristsLogin;

    private DaoManager manager = DaoManager.getInstance();

    private UserConfig userConfig;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_login;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCheckNetWork(true);
//        UpdataManagerUtil.requestPermissionForInstallPackage(this);//版本更新
        initView();
        initServiceAddressAndUser();
    }


    private void initView() {
        //设置手机号码最大长度
        mLoginEditTextIphone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        //设置手机验证码最大长度
        mLoginPhonePassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }


    private void initServiceAddressAndUser() {
        userConfig = UserConfig.getConfig(this, CommonVariable.USER_CONFIG_NAME);
        String addr = userConfig.readString(CommonVariable.SERVICE_ADDRESS);
        if (!TextUtils.isEmpty(addr)) {
            MCloudApp.setHttpsServiceAddress(addr);
        }

        String uid = userConfig.readString(CommonVariable.UID);
        String pwd = userConfig.readString(CommonVariable.PWD);
        if (!TextUtils.isEmpty(uid)) {
            mLoginEditTextAccount.setText(uid);
            mLoginEditTextAccount.setSelection(uid.length());
        }

        if (!TextUtils.isEmpty(pwd)) {
            mLoginAccountPassword.setText(pwd);
        }
    }


    @OnClick({R.id.login_accountLogin, R.id.login_quickLogin, R.id.btn_get_code, R.id.server_config, R.id.btn_login_account, R.id.btn_login_phone, R.id.tv_registered, R.id.tv_forgot_password, R.id.tourists_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.login_accountLogin://切换账号登录视图
                mLoginAccountLogin.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mLoginQuickLogin.setTextColor(ContextCompat.getColor(this, R.color.gray_757575));
                mLoginSelectLeftLine.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mLoginSelectRightLine.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_757575));
                mLlAccountLogin.setVisibility(View.VISIBLE);
                mLlPhoneLogin.setVisibility(View.GONE);
                mTvForgotPassword.setVisibility(View.VISIBLE);
                break;

            case R.id.login_quickLogin: //切换手机快速登录视图
                mLoginAccountLogin.setTextColor(ContextCompat.getColor(this, R.color.gray_757575));
                mLoginQuickLogin.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mLoginSelectLeftLine.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_757575));
                mLoginSelectRightLine.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mLlAccountLogin.setVisibility(View.GONE);
                mLlPhoneLogin.setVisibility(View.VISIBLE);
                mTvForgotPassword.setVisibility(View.GONE);
                break;

            case R.id.btn_get_code://获取验证码
                String mPhoneNumber = mLoginEditTextIphone.getText().toString().trim();
                if (ValidateUtil.checkMobileNumber(mPhoneNumber)) {
                    sendSmsCode(mPhoneNumber);
                } else {
                    ToastUtils.show("手机号输入格式错误！");
                }
                break;

            case R.id.server_config: //配置服务器
//                CrashReport.testJavaCrash();
                ServiceConfigActivity.startActivity(this);
                break;

            case R.id.btn_login_account://账号登录
                if (TextUtils.isEmpty(MCloudApp.getHttpsServiceAddress())) {
                    ToastUtils.show("请先配置服务地址！");
                    ServiceConfigActivity.startActivity(this);
                    return;
                }

                if (prepareForLogin(false)) {
                    return;
                }

                showLoadingDialog("正在登录...");
                LoginManager.getInstance().login(mLoginEditTextAccount.getText().toString(), mLoginAccountPassword.getText().toString(), this);
                break;

            case R.id.btn_login_phone://快速登录
                if (TextUtils.isEmpty(MCloudApp.getHttpsServiceAddress())) {
                    ToastUtils.show("请先配置服务地址！");
                    ServiceConfigActivity.startActivity(this);
                    return;
                }

                if (prepareForLogin(true)) {
                    return;
                }

                String mobile = mLoginEditTextIphone.getText().toString();
                String code = mLoginPhonePassword.getText().toString();
                if (!ValidateUtil.checkMobileNumber(mobile) || !ValidateUtil.isNumeric(code)) {
                    ToastUtils.show("手机号或验证码输入格式错误！");
                    return;
                }

                showLoadingDialog("正在登录...");
                LoginManager.getInstance().quickLogin(mobile, code, this);
                break;

            case R.id.tv_registered:  //用户注册
                break;

            case R.id.tv_forgot_password:
                //忘记密码
                ToastUtils.show("忘记密码");
                break;

            case R.id.tourists_login: //游客登录
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }


    @Override
    public void callback(int code, Object data) {
        dismissLoadingDialog();

        if (LoginManager.LOGIN_CODE_SUCCESS == code) {
            MainActivity.start(LoginActivity.this);
            finish();

        } else {
            ToastUtils.show("登录失败\n" + data);
        }
    }


    /**
     * 发送验证码
     */
    private void sendSmsCode(String mPhoneNumber) {
        String json = GsonFactory.getGson().toJson(mPhoneNumber);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);

        showLoadingDialog("正在获取验证码...");
        MDRetrofit.getInstance().createService().sendSmsCode(CommonVariable.APP_KEY, CommonVariable.APP_SECRET, body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new BaseObserver<String>() {
            @Override
            public void Success(String s, String message) {
                dismissLoadingDialog();

                if (s.contains("已发送")) {
                    MyCountDownTimer timer = new MyCountDownTimer(mBtnGetCode, 60000, 1000);
                    timer.start();
                }
            }

            @Override
            public void Failure(String message) {
                dismissLoadingDialog();
                ToastUtils.show(message);
            }
        });
    }


    private boolean prepareForLogin(boolean isQuicklyLogin) {
        if (!isQuicklyLogin) {
            if (mLoginEditTextAccount.length() == 0) {
                mLoginEditTextAccount.setError("请输入用户名");
                mLoginEditTextAccount.requestFocus();
                return true;
            }
            if (mLoginAccountPassword.length() == 0) {
                mLoginAccountPassword.setError("请输入密码");
                mLoginAccountPassword.requestFocus();
                return true;
            }

        } else {

            if (mLoginEditTextIphone.length() == 0) {
                mLoginEditTextIphone.setError("请输入手机号");
                mLoginEditTextIphone.requestFocus();
                return true;
            }
            if (mLoginPhonePassword.length() == 0) {
                mLoginPhonePassword.setError("请输入验证码");
                mLoginPhonePassword.requestFocus();
                return true;
            }
        }

        return false;
    }
}
