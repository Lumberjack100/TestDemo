package com.shmedo.mcloudapp.common.ui.activity;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.StringUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.util.LoginManager;
import com.shmedo.mcloudapp.util.MyCountDownTimer;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

public class LoginActivity extends BaseActivity implements LoginManager.LoginCallback {
    @BindView(R.id.tv_login_way_title_zh)
    TextView mTvLoginWayTitleZh;

    @BindView(R.id.tv_login_way_title_en)
    TextView mTvLoginWayTitleEn;

    @BindView(R.id.ll_account_login)
    View accountLoginLayout;

    @BindView(R.id.accountET)
    ClearEditText mEtAccount;

    @BindView(R.id.passwordET)
    ClearEditText mEtPwd;

    @BindView(R.id.iv_eye_password)
    ImageView mIvEyePwd;

    @BindView(R.id.ll_phone_login)
    View phoneLoginLayout;

    @BindView(R.id.phoneET)
    ClearEditText mEtPhone;

    @BindView(R.id.codeET)
    ClearEditText mEtCode;

    @BindView(R.id.tv_get_code)
    TextView mTvGetCode;

    @BindView(R.id.tv_user_protocol)
    TextView mTvUserProtocol;

    @BindView(R.id.iv_login_way)
    ImageView mIvLoginWay;

    @BindView(R.id.tv_login_way_desc)
    TextView mTvLoginWayDesc;

    public static final int LOGIN_ACCOUNT = 0x001;//账号密码登录
    public static final int LOGIN_PHONE = 0x002;//手机号验证码登录
    private int loginWay = LOGIN_ACCOUNT;

    private boolean isPasswordVisible = false;

    private String account;
    private String pwd;

    private String mobile;
    private String code;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCheckNetWork(true);
        initLastAccount();
        initLoginUserProtocol();
        getLifecycle().addObserver(LoginManager.getInstance());
    }

    /**
     * 初始化系统栏
     */
    @Override
    protected void initImmersionBar() {
        ImmersionBar.with(this)
                .statusBarColor(R.color.transparent, 0)
                .statusBarDarkFont(false)
                .navigationBarDarkIcon(true)
                .navigationBarColor(R.color.white)
                .init();
    }

    private void initLastAccount() {
        String uid = SPStaticUtils.getString(AppContants.User.UID);
        if (!TextUtils.isEmpty(uid)) {
            mEtAccount.setText(uid);
            mEtAccount.setSelection(uid.length());
        }
    }

    private void initLoginUserProtocol() {
        String text = StringUtils.getString(R.string.login_protocol_desc);
        SpannableString spannableString = new SpannableString(text);
        int start1 = text.indexOf("《用户协议与免责条款》");
        int end1 = start1 + "《用户协议与免责条款》".length();
        spannableString.setSpan(new MyClickText(this, ContentType.USER_PROTOCOL), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //当然这里也可以通过setSpan来设置哪些位置的文本哪些颜色
        mTvUserProtocol.setText(spannableString);
        mTvUserProtocol.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
        mTvUserProtocol.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明
    }

    @OnClick({R.id.iv_eye_password, R.id.tv_get_code, R.id.btn_confirm, R.id.iv_login_way})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_eye_password) {//查看密码
            if (!isPasswordVisible) {
                isPasswordVisible = true;
                mIvEyePwd.setImageResource(R.drawable.icon_eye_open);
                mEtPwd.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                isPasswordVisible = false;
                mIvEyePwd.setImageResource(R.drawable.icon_eye_close);
                mEtPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            mEtPwd.setSelection(mEtPwd.getText().toString().length());
        } else if (id == R.id.tv_get_code) {//获取验证码
            mobile = mEtPhone.getText().toString();
            if (TextUtils.isEmpty(mobile)) {
                ToastUtils.show("请输入手机号");
                mEtPhone.requestFocus();
                return;
            }
            if (!ValidateUtil.checkMobileNumber(mobile)) {
                ToastUtils.show("手机号格式错误！");
                return;
            }
//            doCellPhoneExists();
            sendSmsCode();

        } else if (id == R.id.btn_confirm) {
            if (loginWay == LOGIN_ACCOUNT) {//账号密码登录
                if (!prepareForLogin(false)) {
                    return;
                }
                showWaitDialog("正在登录...");
                String Md5Password = EncryptUtils.encryptMD5ToString(account + pwd);
                LoginManager.getInstance().login(account, pwd, this);
            } else if (loginWay == LOGIN_PHONE) {
                if (!prepareForLogin(true)) {
                    return;
                }
                showWaitDialog("正在登录...");
                LoginManager.getInstance().quickLogin(mobile, code, this);
            }
        } else if (id == R.id.iv_login_way) {//登录方式切换
            if (loginWay == LOGIN_ACCOUNT) {//切换为手机验证码登录
                loginWay = LOGIN_PHONE;
                mTvLoginWayTitleZh.setText(StringUtils.getString(R.string.login_way_phone_zh));
                mTvLoginWayTitleEn.setText(StringUtils.getString(R.string.login_way_phone_en));
                accountLoginLayout.setVisibility(View.GONE);
                phoneLoginLayout.setVisibility(View.VISIBLE);
                mIvLoginWay.setImageResource(R.drawable.icon_account_login);
                mTvLoginWayDesc.setText(StringUtils.getString(R.string.login_way_account_zh));
            } else if (loginWay == LOGIN_PHONE) {//切换为账号密码登录
                loginWay = LOGIN_ACCOUNT;
                mTvLoginWayTitleZh.setText(StringUtils.getString(R.string.login_way_account_zh));
                mTvLoginWayTitleEn.setText(StringUtils.getString(R.string.login_way_account_en));
                accountLoginLayout.setVisibility(View.VISIBLE);
                phoneLoginLayout.setVisibility(View.GONE);
                mIvLoginWay.setImageResource(R.drawable.icon_phone_login);
                mTvLoginWayDesc.setText(StringUtils.getString(R.string.login_way_phone_zh));
            }
        }
    }

    private boolean prepareForLogin(boolean isQuicklyLogin) {
        if (!isQuicklyLogin) {
            account = mEtAccount.getText().toString();
            pwd = mEtPwd.getText().toString();
            if (TextUtils.isEmpty(account)) {
//                mEtAccount.setError("请输入用户名");
                ToastUtils.show("请输入用户名");
                mEtAccount.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(pwd)) {
//                mEtPwd.setError("请输入密码");
                ToastUtils.show("请输入密码");
                mEtPwd.requestFocus();
                return false;
            }

        } else {
            mobile = mEtPhone.getText().toString();
            code = mEtCode.getText().toString();
            if (TextUtils.isEmpty(mobile)) {
//                mEtPhone.setError("请输入手机号");
                ToastUtils.show("请输入手机号");
                mEtPhone.requestFocus();
                return false;
            }
            if (!ValidateUtil.checkMobileNumber(mobile)) {
                ToastUtils.show("手机号格式错误！");
                mEtPhone.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(code)) {
//                mEtCode.setError("请输入验证码");
                ToastUtils.show("请输入验证码");
                mEtCode.requestFocus();
                return false;
            }
            if (code.length() != 6) {
                ToastUtils.show("请输入6位数验证码");
                mEtCode.requestFocus();
                return false;
            }
        }
        return true;
    }

    @Override
    public void callback(int code, String msg) {
        dismissWaitDialog();

        if (LoginManager.LOGIN_CODE_SUCCESS == code) {
            MainActivity.start(LoginActivity.this);
            finish();
        } else if (LoginManager.LOGIN_CODE_FAIL_BUSINESS == code) {
            ToastUtils.show("登录失败: " + msg);
        }
    }

    /**
     * 发送验证码
     */
    private void sendSmsCode() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("phone", mobile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        showWaitDialog("正在获取验证码...");
        MDRetrofit.getInstance()
                .createService()
                .sendSmsCode(body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    protected void onResponse(String data, ErrorInfo errorInfo) {
                        dismissWaitDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (!TextUtils.isEmpty(data) && data.contains("已发送")) {
                                    ToastUtils.show(data);
                                    MyCountDownTimer timer = new MyCountDownTimer(mTvGetCode, 60000, 1000);
                                    timer.setTextColor(R.color.title_text_color, R.color.text_color_b3b3b3);
                                    timer.start();
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
                        dismissWaitDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    static class MyClickText extends ClickableSpan {
        private Context context;
        private ContentType contentType;

        public MyClickText(Context context, ContentType contentType) {
            this.context = context;
            this.contentType = contentType;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            //设置文本的颜色
            ds.setColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.colorPrimary));
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View view) {
            if (contentType == ContentType.USER_PROTOCOL) {
                String url = "file:///android_asset/private/UserProtocol.html";
                WebViewActivity.startActivity(context, url);
            } else {
                String url = "file:///android_asset/private/PrivacyPolicy.html";
                WebViewActivity.startActivity(context, url);
            }
        }
    }

    public enum ContentType implements Serializable {
        USER_PROTOCOL,//用户协议
        PRIVACY_POLICY//隐私政策
    }
}
