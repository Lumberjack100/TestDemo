package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.ui.PrivacyTipDialog;

import butterknife.BindView;

public class ProtocolActivity extends BaseActivity {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.webView)
    WebView webView;

    private PrivacyTipDialog.ContentType contentType;

    public static void startActivity(Context context, PrivacyTipDialog.ContentType contentType) {
        Intent intent = new Intent(context, ProtocolActivity.class);
        intent.putExtra(ARG_PARAM1, contentType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_protocol;
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
            contentType = (PrivacyTipDialog.ContentType) intent.getSerializableExtra(ARG_PARAM1);

            String url = "";
            if (contentType == PrivacyTipDialog.ContentType.USER_PROTOCOL) {
                mToolbarTitle.setText("用户协议");
                url = "file:///android_asset/private/UserProtocol.html";
            } else {
                mToolbarTitle.setText("隐私政策");
                url = "file:///android_asset/private/PrivacyPolicy.html";
            }
            loadData(url);
        }
    }

    public void loadData(String url) {
        webView.loadUrl(url);
    }
}
