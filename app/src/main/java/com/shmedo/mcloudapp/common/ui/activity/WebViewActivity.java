package com.shmedo.mcloudapp.common.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;

public class WebViewActivity extends BaseActivity {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.webView)
    WebView webView;

    private String url;

    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(ARG_PARAM1, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_webview;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initWebView();
        parseIntent();
    }

    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(ARG_PARAM1)) {
            url = intent.getStringExtra(ARG_PARAM1);
            loadData();
        }
    }

    public void loadData() {
        if (TextUtils.isEmpty(url)) {
            ToastUtils.show("地址错误，地址不能为空");
        } else {
            webView.loadUrl(url);
        }
    }
}
