package com.shmedo.mcloudapp.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResultDao;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.interfaces.Extras;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.util.ApiName;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.WifiSupport;
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

    private String deviceToken;

    private String deviceTypeName;

    private String ipAddress = "172.168.5.249";

    private boolean isAgainLoading = false;

    private DaoManager manager = DaoManager.getInstance();

    private String securityNo;


    public static void startActivity(Context context, DeviceBasicInfoResult deviceBasicInfoResult) {
        Intent intent = new Intent(context, ConfigE60Activity.class);
        intent.putExtra(Extras.DEVICE_E60, deviceBasicInfoResult);
        context.startActivity(intent);
    }


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

    @Override
    protected void onResume() {
        super.onResume();

        if (isAgainLoading) {
            initData();
            isAgainLoading = false;
        }
        isAgainLoading = true;
    }


    private void initView() {
        DeviceBasicInfoResult deviceBasicInfoResult = (DeviceBasicInfoResult) (getIntent().getSerializableExtra(Extras.DEVICE_E60));
        if (deviceBasicInfoResult != null) {
            deviceToken = deviceBasicInfoResult.getDeviceToken() != null ? deviceBasicInfoResult.getDeviceToken() : "";
            deviceTypeName = deviceBasicInfoResult.getDeviceTypeName() != null ? deviceBasicInfoResult.getDeviceTypeName() : "";

            mToolbarTitle.setText(deviceTypeName + ":" + deviceToken);
        }
    }

    private void initData() {
        if (!WifiSupport.isOpenWifi(this)) {
            ToastUtils.show("请打开WIFI连接");
            WifiSupport.goWifiSetting(this);
            return;
        }

        if (!WifiSupport.isWifiConnected(this)) {
            ToastUtils.show("请打开连接正确的WIFI");
            WifiSupport.goWifiSetting(this);
            return;
        }

        getDeviceJson(ipAddress);
    }


    /**
     * 请求设备json
     */
    private void getDeviceJson(final String ipAddr) {
        String url = "http://" + ipAddr + "/device.json";

        showLoadingDialog("正在验证设备...");
        MDRetrofit.getInstance()
                .createService(ApiName.HTTP)
                .ValidateDeviceE60(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String result, String message) {
                        dismissLoadingDialog();

                        if (TextUtils.isEmpty(result)) {
                            showPopWindow();
                            return;
                        }

                        if (!result.startsWith("MEDO")) {
                            showLoadResultDialog("设备验证失败,请选择正确的设备进行验证");
                            return;
                        }

                        String[] data = result.split(",");
                        Timber.d("Call getDeviceJson," + result + "--" + data[1] + "--" + deviceToken + "--" + data[2] + "--" + deviceTypeName);

                        if (data[1].equals(deviceToken) && data[2].equals(deviceTypeName)) {
                            setWebView(ipAddr, deviceToken);
                        } else {
                            showLoadResultDialog("设备验证失败,请选择正确的设备进行验证");
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w(message);
                        dismissLoadingDialog();
                        showPopWindow();
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
                isAgainLoading = true;
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
                            ToastUtils.show("ip地址不能为空");
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
        if (TextUtils.isEmpty(token)) {
            return;
        }

//        securityNo = searchProcess(token);
        Timber.d("securityNo=" + securityNo);

        showLoadingDialog("正在登录系统...");
        mMWebView.loadUrl("http://" + ipAddr + "/cors/index.html?uid=admin&pwd=admin");
        WebSettings webSettings = mMWebView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);

        mMWebView.setVerticalScrollBarEnabled(true);
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

                dismissLoadingDialog();
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
     * 在本地设备基础信息数据库中根据设备名称查询sn号
     *
     * @param token
     * @return
     */
    private String searchProcess(String token) {
        return manager.getDaoSession()
                .getDeviceBasicInfoResultDao()
                .queryBuilder()
                .where(DeviceBasicInfoResultDao.Properties.DeviceToken.eq(token))
                .unique()
                .getSecurityNO();
    }

    /**
     * 如果验证错误，提示用户
     *
     * @param content
     */
    private void showLoadResultDialog(String content) {
        MaterialDialog.Builder  mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
                finish();
            }
        });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMWebView.removeAllViews();
        mMWebView.destroy();
    }
}
