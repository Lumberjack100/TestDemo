package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.ui.PrivacyTipDialog;

import butterknife.BindView;

public class ProtocolActivity extends BaseActivity {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_content)
    TextView mTvContent;

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
        mToolbarTitle.setText("");
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(ARG_PARAM1)) {
            contentType = (PrivacyTipDialog.ContentType) intent.getSerializableExtra(ARG_PARAM1);
            Spanned spanned;
            if(contentType== PrivacyTipDialog.ContentType.USER_AGREEMENT){
                spanned= Html.fromHtml(getResources().getString(R.string.user_agreement));
            }else{
                spanned= Html.fromHtml(getResources().getString(R.string.privacy_policy));
            }

            mTvContent.setText(spanned);
        }
    }
}
