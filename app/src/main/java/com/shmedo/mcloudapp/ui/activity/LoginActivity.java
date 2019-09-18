package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.entity.UserInfoWrapper;
import com.shmedo.mcloudapp.entity.parameter.SignInParameter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.MD5Util;
import com.shmedo.mcloudapp.util.MyCountDownTimer;
import com.shmedo.mcloudapp.util.StartActivityUtil;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.LoadingDialog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   LoginActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/8 10:09
 * 描述：    TODO
 */
public class LoginActivity extends BaseActivity {

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

    @BindView(R.id.btn_getCode)
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
    private LoadingDialog dialog;
    private UserInfoWrapper userInfoWrapper = new UserInfoWrapper();
    private UserConfig userConfig;


    @Override
    protected int initContentView() {
        return R.layout.activity_login;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialog();
        initView();
        initServiceAddressAndUser();
    }


    private void initDialog() {
        manager.init(this);
        dialog = new LoadingDialog(this);
    }

    private void initView() {
        //设置手机号码最大长度
        mLoginEditTextIphone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        //设置手机验证码最大长度
        mLoginPhonePassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }


    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            Log.i("adu", "------" + result);
            return result.substring(0, result.length() - 1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void initServiceAddressAndUser() {
        userConfig = UserConfig.getConfig(this, CommonVariable.USER_CONFIG_NAME);
        String addr = userConfig.readString(CommonVariable.SERVICE_ADDRESS);
        if (!StringUtil.isNullOrEmpty(addr)) {
            CommonVariable.setServiceAddress(addr);
        }
        String uid = userConfig.readString(CommonVariable.UID);
        String pwd = userConfig.readString(CommonVariable.PWD);
        if ((!StringUtil.isNullOrEmpty(uid)) && (!StringUtil.isNullOrEmpty(pwd))) {
            mLoginEditTextAccount.setText(uid);
            mLoginEditTextAccount.setSelection(uid.length());
            mLoginAccountPassword.setText(pwd);
            //isAutoLogin = true;
        }
    }


    @OnClick({R.id.login_accountLogin, R.id.login_quickLogin, R.id.btn_getCode, R.id.server_config,
            R.id.btn_login_account, R.id.btn_login_phone, R.id.tv_registered,
            R.id.tv_forgot_password, R.id.tourists_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.login_accountLogin:
                //账号登录
                mLoginAccountLogin.setTextColor(ContextCompat.getColor(this, R.color.app_color_blue_2));
                mLoginQuickLogin.setTextColor(ContextCompat.getColor(this, R.color.font_main_79));
                mLoginSelectLeftLine.setBackgroundColor(ContextCompat.getColor(this, R.color.app_color_blue_2));
                mLoginSelectRightLine.setBackgroundColor(ContextCompat.getColor(this, R.color.font_main_79));
                mLlAccountLogin.setVisibility(View.VISIBLE);
                mLlPhoneLogin.setVisibility(View.GONE);
                mTvForgotPassword.setVisibility(View.VISIBLE);
                break;

            case R.id.login_quickLogin:
                //手机快速登录
                mLoginAccountLogin.setTextColor(ContextCompat.getColor(this, R.color.font_main_79));
                mLoginQuickLogin.setTextColor(ContextCompat.getColor(this, R.color.app_color_blue_2));
                mLoginSelectLeftLine.setBackgroundColor(ContextCompat.getColor(this, R.color.font_main_79));
                mLoginSelectRightLine.setBackgroundColor(ContextCompat.getColor(this, R.color.app_color_blue_2));
                mLlAccountLogin.setVisibility(View.GONE);
                mLlPhoneLogin.setVisibility(View.VISIBLE);
                mTvForgotPassword.setVisibility(View.GONE);
                break;

            case R.id.btn_getCode:
                //获取验证码
                String mPhoneNumber = mLoginEditTextIphone.getText().toString().trim();
                if (StringUtil.isPhoneNumber(mPhoneNumber)) {
                    sendSmsCode(mPhoneNumber);
                } else {
                    ToastUtil.showSToast("手机号输入格式错误！");
                }
                break;

            case R.id.server_config:
                //配置服务器
                StartActivityUtil.comeOnBaby(this, ServiceConfigActivity.class);
                break;

            case R.id.btn_login_account:
                //点击账号登录方式
                if (StringUtil.isNullOrEmpty(CommonVariable.getServiceAddress())) {
                    ToastUtil.showLToast("请先配置服务地址！");
                    StartActivityUtil.comeOnBaby(this, ServiceConfigActivity.class);
                    return;
                }

                if (prepareForLogin(false)) {
                    return;
                }

                final String uid = mLoginEditTextAccount.getText() != null
                        ? mLoginEditTextAccount.getText().toString().trim()
                        : null;
                final String pwd = mLoginAccountPassword.getText() != null
                        ? mLoginAccountPassword.getText().toString().trim()
                        : null;
                if (StringUtil.isNullOrEmpty(uid) || StringUtil.isNullOrEmpty(pwd)) {
                    ToastUtil.showLToast("用户名或密码不能为空！");
                    return;
                }
                accountSingIn(uid, pwd);
                break;

            case R.id.btn_login_phone:
                //点击短信登录方式
                if (StringUtil.isNullOrEmpty(CommonVariable.getServiceAddress())) {
                    ToastUtil.showLToast("请先配置服务地址！");
                    StartActivityUtil.comeOnBaby(this, ServiceConfigActivity.class);
                    return;
                }

                if (prepareForLogin(true)) {
                    return;
                }

                String code = mLoginPhonePassword.getText() != null
                        ? mLoginPhonePassword.getText().toString().trim()
                        : null;
                String phoneNumber = mLoginEditTextIphone.getText() != null
                        ? mLoginEditTextIphone.getText().toString().trim()
                        : null;
                if (StringUtil.isNullOrEmpty(code) && StringUtil.isNullOrEmpty(phoneNumber)) {
                    ToastUtil.showSToast("手机号或验证码不能为空！");
                    return;
                }

                if (StringUtil.isCodeCorrect(code) && StringUtil.isPhoneNumber(phoneNumber)) {
                    quickLogin(code, phoneNumber);
                } else {
                    ToastUtil.showLToast("手机号或验证码输入格式错误！");
                }
                break;

            case R.id.tv_registered:
                //用户注册
                ToastUtil.showLToast("用户注册");
                break;

            case R.id.tv_forgot_password:
                //忘记密码
                ToastUtil.showLToast("忘记密码");
                break;

            case R.id.tourists_login:
                //游客登录
                Intent in = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(in);
                break;

            default:
                break;
        }
    }


    /**
     * 快速登录
     */
    private void quickLogin(String code, String mPhoneNumber) {
        SignInParameter parameter = new SignInParameter(mPhoneNumber, code);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        dialog.showNoCancelDialog("正在登录...");
        MDRetrofit.getInstance().createService().SmsLogin(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String s, String message) {
                        dialog.dismiss();
                        if (s.contains("手机号对应的用户不存在")) {
                            ToastUtil.showSToast("手机号对应的用户不存在");
                            //TODO 手机号不存在设置为游客登录
                        } else {
                            getMyInfo(s, null, null);
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        dialog.dismiss();
                        ToastUtil.showSToast(message);
                    }
                });
    }


    /**
     * 发送验证码
     */
    private void sendSmsCode(String mPhoneNumber) {
        String json = GsonFactory.getGson().toJson(mPhoneNumber);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        dialog.showNoCancelDialog("正在获取验证码...");
        MDRetrofit.getInstance()
                .createService()
                .sendSmsCode(CommonVariable.APP_KEY, CommonVariable.APP_SECRET, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String s, String message) {
                        dialog.dismiss();
                        if (s.contains("已发送")) {
                            MyCountDownTimer timer = new MyCountDownTimer(mBtnGetCode, 60000, 1000);
                            timer.start();
                        }
                    }


                    @Override
                    public void Failure(String message) {
                        dialog.dismiss();
                        ToastUtil.showLToast(message);
                    }
                });
    }


    /**
     * 账户登录
     */
    private void accountSingIn(final String uid, final String pwd) {
        SignInParameter parameter = new SignInParameter(uid, pwd);
        parameter.setPassword(MD5Util.MD5(uid + pwd));
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        dialog.showNoCancelDialog("正在登录...");
        MDRetrofit.getInstance().createService().getSingIn(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {

                    @Override
                    public void Success(String token, String message) {
                        dialog.dismiss();
                        getMyInfo(token, uid, pwd);
                    }

                    @Override
                    public void Failure(String message) {
                        dialog.dismiss();
                        ToastUtil.showLToast("登录失败" + message);
                    }
                });
    }


    /**
     * 获取用户信息
     *
     * @param token
     * @param uid
     * @param pwd
     */
    private void getMyInfo(final String token, final String uid, final String pwd) {
        MDRetrofit.getInstance().createService().getMyInfo(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<UserInfo>() {
                    @Override
                    public void Success(UserInfo userInfo, String message) {
                        dialog.dismiss();

                        CommonVariable.setAccessToken(token);
                        CommonVariable.setCurrentUserInfo(userInfo);

                        Long id = Long.valueOf(userInfo.getUser().getId());
                        userInfoWrapper.setId(id);
                        userInfoWrapper.setUserInfo(GsonFactory.getGson().toJson(userInfo));
                        manager.getDaoSession().getUserInfoWrapperDao().insertOrReplace(userInfoWrapper);

                        userConfig.writeString(CommonVariable.ACCOUNT, userInfo.getUser().getAccount());
                        userConfig.writeString(CommonVariable.USER_ID, String.valueOf(userInfo.getUser().getId()));
                        if (userInfo.getUser().getHeadPhotoPath() != null) {
                            userConfig.writeString(CommonVariable.HEAD_PHOTO_PATH, userInfo.getUser().getHeadPhotoPath());
                            Timber.d("头像路径------" + userInfo.getUser().getHeadPhotoPath());
                        }

                        if (uid != null && pwd != null) {
                            userConfig.writeString(CommonVariable.UID, uid);
                            userConfig.writeString(CommonVariable.PWD, pwd);
                        }

                        Intent in = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(in);
                        finish();
                    }


                    @Override
                    public void Failure(String message) {
                        dialog.dismiss();
                        ToastUtil.showLToast("登录失败" + message);
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
