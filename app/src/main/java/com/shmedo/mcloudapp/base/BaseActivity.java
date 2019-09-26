package com.shmedo.mcloudapp.base;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.base
 * 文件名:   BaseActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:46
 * 描述：    TODO
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int initContentView();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //StatusUtil.StatusBarLightMode(this);
        setContentView(initContentView());
        initState();
        ButterKnife.bind(this);
    }


    /**
     * 沉浸式状态栏
     */
    public void initState() {
        //设置了Theme带ActionBar的Activity，左上角显示返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (Build.VERSION.SDK_INT >= 21) {
                getSupportActionBar().setElevation(0);
            }
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }*/
    }


    @Override
    protected void onResume() {
        super.onResume();

        String name = getClass().getName();
        Timber.d("startPage,activity=" + name);
    }


    @Override
    protected void onPause() {
        super.onPause();

        String name = getClass().getName();
        Timber.d("endPage,activity=" + name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
            int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            super.onBackPressed();
        }
    }
}
