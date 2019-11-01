package com.shmedo.mcloudapp.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.MyWebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   QueryDataFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:39
 * 描述：    查询数据
 */
public class QueryDataFragment extends BaseFragment {

    @BindView(R.id.webView)
    MyWebView mWebView;

    @BindView(R.id.pbar_more)
    ProgressBar progressBar;

    private Unbinder unbinder;

    private String SENSORDATA_URL = "http://chaxun.shmedo.cn";
    //private String SENSORDATA_URL="http://172.168.5.37:8030/demopage/wode_cexieyi.html";

    private Handler mHandler;


    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            ToastUtil.showShortToast("加载失败");
        }
    };

    @Override
    protected int initContentView() {
        return R.layout.fragment_query_data;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        mHandler = new Handler();
        initWebView();
        initData();
        return view;
    }


    private void initData() {
        if (!MCloudApp.isIsNetworkConnected()) {
            ToastUtil.showShortToast("当前网络不可用");
            return;
        }

        mWebView.loadUrl(SENSORDATA_URL);
        mHandler.postDelayed(dismssDialogRunnable, 15000);
    }

    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        // 设置字符编码
        webSettings.setDefaultTextEncodingName("utf-8");
        //支持通过JS打开新窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);//设置可以访问文件
        webSettings.setDomStorageEnabled(true);//允许SessionStorage/LocalStorage存储
        webSettings.setDatabaseEnabled(true); //开启 database storage API 功能

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true);//缩放至屏幕的大小

        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
    }


    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }


        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (url != null && url.equals(SENSORDATA_URL)) {
                //startTime();
            }
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }


    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Timber.d("onJsAlert: " + message);
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            Timber.d("onJsConfirm: " + message);
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Timber.d("onJsPrompt: " + message);
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                if (progressBar.getVisibility() == View.GONE)
                    progressBar.setVisibility(View.VISIBLE);

                progressBar.setProgress(newProgress);
            }

            super.onProgressChanged(view, newProgress);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onBackPressed() {
        return false;

//        if (mWebView.canGoBack()) {
//            mWebView.goBack();
//            return true;
//        } else {
//            return super.onBackPressed();
//        }
    }
}
