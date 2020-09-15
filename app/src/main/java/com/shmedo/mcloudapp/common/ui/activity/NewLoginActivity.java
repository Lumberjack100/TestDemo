package com.shmedo.mcloudapp.common.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gyf.immersionbar.ImmersionBar;
import com.shmedo.mcloudapp.R;

import butterknife.BindView;

public class NewLoginActivity extends BaseActivity {
    @BindView(R.id.status_bar_view)
    View statusBarView;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, NewLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_login;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);
    }

    /**
     * 初始化系统栏
     */
    @Override
    protected void initImmersionBar() {
        ImmersionBar.with(this)
                .titleBar(statusBarView)
                .statusBarColor(R.color.transparent, 0)
                .statusBarDarkFont(false)
                .navigationBarDarkIcon(true)
                .navigationBarColor(R.color.white)
                .init();
    }
}
