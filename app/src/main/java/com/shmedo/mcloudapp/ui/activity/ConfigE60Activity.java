package com.shmedo.mcloudapp.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResultDao;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.WifiSupport;
import com.shmedo.mcloudapp.views.LoadingDialog;
import com.shmedo.mcloudapp.views.MyWebView;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ConfigE60Activity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.mWebView)
    MyWebView mMWebView;

    private LoadingDialog mLoadingDialog;
    private MaterialDialog mMaterialDialog;
    private MaterialDialog.Builder mBuilder;
    private String deviceToken;
    private String deviceTypeName;
    private String ipAddress = "192.168.5.2";
    private boolean againLoading = false;
    private DaoManager manager = DaoManager.getInstance();
    private String securityNo;

    @Override
    protected int initContentView() {
        return R.layout.activity_config_e60;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initData();
    }


    private void initView() {
        mLoadingDialog = new LoadingDialog(this);

        DeviceBasicInfoResult deviceBasicInfoResult = (DeviceBasicInfoResult) (getIntent().getSerializableExtra("device"));
        if (deviceBasicInfoResult != null) {
            deviceToken = deviceBasicInfoResult.getDeviceToken() != null ? deviceBasicInfoResult.getDeviceToken() : "";
            deviceTypeName = deviceBasicInfoResult.getDeviceTypeName() != null ? deviceBasicInfoResult.getDeviceTypeName() : "";

            mToolbarTitle.setText(deviceTypeName + ":" + deviceToken);
        }
    }

    private void initData() {
        if (WifiSupport.isOpenWifi(this)) {
            if (WifiSupport.isWifiConnected(this)) {
                getDeviceJson(ipAddress);

            } else {
                ToastUtil.showSToast("请打开连接正确的WIFI");
                WifiSupport.goWifiSetting(this);
            }

        } else {
            ToastUtil.showSToast("请打开WIFI连接");
            WifiSupport.goWifiSetting(this);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (againLoading) {
            if (mLoadingDialog == null) {
                mLoadingDialog = new LoadingDialog(this);
            }
            initData();
            againLoading = false;
        }
        againLoading = true;
    }

    /**
     * 请求设备json
     */
    private void getDeviceJson(final String ipAddr) {

        String url = "http://" + ipAddr + "/device.json";

        mLoadingDialog.showNoCancelDialog("正在验证设备...");
        MDRetrofit.getInstance().createService().ValidateDeviceE60(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String result, String message) {
                        mLoadingDialog.dismiss();

                        if (TextUtils.isEmpty(result)) {
                            showPopWindow();
                            return;
                        }

                        if (result.startsWith("MEDO")) {
                            String[] data = result.split(",");
                            Timber.d("Call getDeviceJson," + result + "--" + data[1] + "--" + deviceToken + "--" + data[2] + "--" + deviceTypeName);

                            if (data[1].equals(deviceToken) && data[2].equals(deviceTypeName)) {
                                mLoadingDialog.dismiss();
                                setWebView(ipAddr, deviceToken);
                            } else {
                                mLoadingDialog.dismiss();
                                showLoadResultDialog("设备验证失败,请选择正确的设备进行验证");
                            }
                        } else {

                            showLoadResultDialog("设备验证失败,请选择正确的设备进行验证");
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        mLoadingDialog.dismiss();
                        ToastUtil.showLToast("设备验证失败," + message);
                    }
                });
    }


    /**
     * 在没有返回结果的时候，提示用户
     * 1、修改ip地址
     * 2、修改wifi连接
     */
    private void showPopWindow() {
        View view = getLayoutInflater().inflate(R.layout.change_address_pop, null);
        Button changeIP = (Button) view.findViewById(R.id.change_ip);
        Button changeWifi = (Button) view.findViewById(R.id.change_wifi);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        final AlertDialog mDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("设备验证失败，你可以通过以下方式进行辅助验证")
                .setView(view)
                .create();
        mDialog.show();
        changeIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                changeAddress();
            }
        });
        changeWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                WifiSupport.goWifiSetting(ConfigE60Activity.this);
                againLoading = true;
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                finish();
            }
        });
    }

    /**
     * 改变请求的服务器地址
     */
    private void changeAddress() {
        View view = getLayoutInflater().inflate(R.layout.securityno_dialog_view, null);
        final EditText editText = (EditText) view.findViewById(R.id.dialog_edit);
        final AlertDialog mDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("修改设备地址")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", null)
                .create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positionButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String content = editText.getText().toString();
                        if (TextUtils.isEmpty(content)) {
                            ToastUtil.showSToast("ip地址不能为空");
                            return;
                        }
                        //if (!DisposeUtil.isRegex(content)){
                        //    ToastUtils.showShort(ConfigE60Activity.this, "ip地址格式输入有误");
                        //    return;
                        //}
                        mDialog.dismiss();
                        getDeviceJson(content);

                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        finish();
                    }
                });
            }
        });
        mDialog.show();
    }


    private void setWebView(final String ipAddr, String token) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
        }

        if (TextUtils.isEmpty(token)) {
            return;
        }

        securityNo = queryAuthor(token);
        Timber.d("securityNo=" + securityNo);
        mLoadingDialog.showNoCancelDialog("正在登录系统...");
        mMWebView.loadUrl("http://" + ipAddr + "/cors/index.html?uid=E60&pwd=medo123");
        WebSettings webSettings = mMWebView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        mMWebView.setVerticalScrollBarEnabled(true);
        webSettings.setAllowFileAccess(true);
        mMWebView.setDownloadListener(new MyWebViewDownLoadListener());
        mMWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                mLoadingDialog.dismiss();
            }
        });
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Timber.d("======onDownloadStart======"
                    + "\r\n url=" + url
                    + "\r\n userAgent=" + userAgent
                    + "\r\n contentDisposition=" + contentDisposition
                    + "\r\n mimetype=" + mimetype
                    + "\r\n contentLength=" + contentLength);

            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    /**
     * 根据设备名称查询sn号
     *
     * @param token
     * @return
     */
    private String queryAuthor(String token) {
        return manager.getDaoSession().getDeviceBasicInfoResultDao().queryBuilder().where(DeviceBasicInfoResultDao.Properties.DeviceToken.eq(token)).unique().getSecurityNO();
    }

    /**
     * 如果验证错误，提示用户
     *
     * @param content
     */
    private void showLoadResultDialog(String content) {
        mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：").content(content).contentColor(Color.parseColor("#000000")).canceledOnTouchOutside(false).positiveText("确定").onPositive(new MaterialDialog.SingleButtonCallback() {
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
    protected void onDestroy() {
        super.onDestroy();
        mMWebView.removeAllViews();
        mMWebView.destroy();
    }
}
