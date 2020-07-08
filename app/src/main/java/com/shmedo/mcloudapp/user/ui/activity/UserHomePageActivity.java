package com.shmedo.mcloudapp.user.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.views.ClearEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 用户个人详细信息页
 */
public class UserHomePageActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.userNameET)
    ClearEditText mTvCompanyName;

    @BindView(R.id.titleET)
    ClearEditText mTvCompanyType;

    @BindView(R.id.emailET)
    ClearEditText mTvIndustryName;

    @BindView(R.id.tv_phone)
    TextView mTvPhone;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, UserHomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_user_home_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("我的信息");
    }


    @OnClick({R.id.mobileLayout, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mobileLayout:
                UpdatePhoneActivity.startActivity(this);
                break;

            case R.id.btn_confirm:

                break;
        }
    }
}
