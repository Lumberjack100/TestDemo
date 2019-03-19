package com.shmedo.mcloudapp.ui.activity.device;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.LoadingDialog;
import com.shmedo.mcloudapp.views.MyWebView;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device
 * 文件名:   QueryDataRecordActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/28 17:01
 * 描述：    查询数据记录
 */
public class QueryDataRecordActivity extends BaseActivity {

    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.webView) MyWebView mWebView;
    @BindView(R.id.container) LinearLayout mContainer;
    private LoadingDialog mLoadingDialog;
    private Timer mTimer;
    private String SENSORDATA_URL="http://chaxun.shmedo.cn";
    @SuppressLint("HandlerLeak")
    private Handler mHandler=new Handler(){
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0){
                ToastUtil.showSToast("当前网络不可用");
                if (mLoadingDialog.isShowing()){
                    mLoadingDialog.dismiss();
                }
            }
        }
    };



    @Override protected int initContentView() {
        return R.layout.activity_query_data_record;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("数据查询");
 
    }


    private void initData() {
        if(CommonVariable.isNetworkConnected()){
            initWebView();
        }else{
           ToastUtil.showSToast("当前网络不可用");
        }
    }


    private void initWebView() {
        mLoadingDialog = new LoadingDialog(this);
        mLoadingDialog.showNoCancelDialog("正在加载...");
        mWebView.loadUrl(SENSORDATA_URL);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        mWebView.setVerticalScrollBarEnabled(true);
        webSettings.setAllowFileAccess(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                mLoadingDialog.showNoCancelDialog("正在加载...");
                return true;
            }


            @Override public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                if (url!=null&& url.equals(SENSORDATA_URL)){
                    startTime();
                }
            }


            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadingDialog.dismiss();
            }
        });

    }
    /**
     * 开启计时
     */
    private void startTime() {
        TimerTask timerTask=new TimerTask() {
            @Override public void run() {
                mHandler.sendEmptyMessage(0);
            }
        };
        mTimer.schedule(timerTask,20000);
    }
}
