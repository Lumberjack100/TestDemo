package com.shmedo.mcloudapp.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.LoadingDialog;
import com.shmedo.mcloudapp.views.MyWebView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   QueryDataFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:39
 * 描述：    查询数据
 */
public class QueryDataFragment extends BaseFragment {

    @BindView(R.id.webView) MyWebView mWebView;
    Unbinder unbinder;

    private LoadingDialog mLoadingDialog;
    private Timer mTimer;
    private String SENSORDATA_URL="http://chaxun.shmedo.cn";
    //private String SENSORDATA_URL="http://172.168.5.37:8030/demopage/wode_cexieyi.html";
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
        return R.layout.fragment_query_data;
    }


    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this,view);
        initData();
        return view;
    }

    private void initView() {

    }

    private void initData() {
        if(CommonVariable.isNetworkConnected()){
            initWebView();
        }else{
            ToastUtil.showSToast("当前网络不可用");
        }
    }

    private void initWebView() {
        mLoadingDialog = new LoadingDialog(getActivity());
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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //return super.shouldOverrideUrlLoading(view, url);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                } else {
                    view.loadUrl(request.toString());
                }
                return true;
                //try {
                //    if (url.startsWith("http:") || url.startsWith("https:") || url.startsWith("tbopen:")) {
                //        view.loadUrl(url);
                //        Log.i("adu","--------------url--------------");
                //    } else {
                //        Log.i("adu","--------------intent--------------");
                //        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //        startActivity(intent);
                //    }
                //    return true;
                //} catch (Exception e){
                //    return false;
                //}
            }


            @Override public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                if (url!=null&& url.equals(SENSORDATA_URL)){
                    //startTime();
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
    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
