package com.shmedo.mcloudapp.common.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.core.event.NetworkChangeEvent;
import com.shmedo.core.receiver.NetworkConnectChangedReceiver;
import com.shmedo.core.util.ActivityCollector;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.core.util.NetworkUtils;
import com.shmedo.mcloudapp.util.UiUtils;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Objects;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.base
 * 文件名:   BaseActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:46
 * 描述：
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected MaterialDialog loadingDialog = null;

    protected boolean mCheckNetwork = false;/*默认检查网络状态*/

    protected boolean mNetConnected;/*网络连接的状态，true表示有网络，flase表示无网络连接*/

    private NetworkConnectChangedReceiver mNetWorkChangReceiver;/*网络状态变化的广播接收器*/

    private View mTipView;

    private WindowManager mWindowManager;

    private WindowManager.LayoutParams mLayoutParams;

    private WeakReference<Activity> weakRefActivity = null;


    protected abstract int initContentView();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && UiUtils.isTranslucentOrFloating(this)) {
            UiUtils.fixOrientation(this);
            Timber.i("===api 26 全屏横竖屏切换 crash=");
        }
        super.onCreate(savedInstanceState);

        weakRefActivity = new WeakReference<Activity>(this);
        ActivityCollector.add(weakRefActivity);
        setContentView(initContentView());
        ButterKnife.bind(this);

        initTipView();//初始化提示View
        registerNetWorkChangReceiver();
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && UiUtils.isTranslucentOrFloating(this)) {
            Timber.i("===api 26 全屏横竖屏切换 crash");
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }


    /**
     * Use a Toolbar as an Action Bar
     */
    protected void setToolBar(int toolbarId) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String name = getClass().getName();
        Timber.i("startPage,activity=%s", name);

        //在无网络情况下打开APP时，系统不会发送网络状况变更的Intent，需要自己手动检查
        netStateChangedUI(NetworkUtils.isConnected());
    }


    @Override
    protected void onPause() {
        super.onPause();
        String name = getClass().getName();
        Timber.i("endPage,activity=%s", name);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar Home按钮返回事件
        if (item.getItemId() == android.R.id.home) {
            KeyBordUtils.hideSoftKeyboard(this.getWindow().getDecorView());
            onBackPressed();
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


    protected void showLoadingDialog(String tip) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        if (loadingDialog == null) {
            loadingDialog = new MaterialDialog.Builder(this)
                    .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
//            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    protected void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }


    protected void showTipDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定");
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    /**
     * 点击空白区域隐藏键盘.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {  //把操作放在用户点击的时候
            View v = getCurrentFocus();      //得到当前页面的焦点,ps:有输入框的页面焦点一般会被输入框占据
            if (isShouldHideKeyboard(v, motionEvent)) { //判断用户点击的是否是输入框以外的区域
//                hideKeyboard(v.getWindowToken());   //收起键盘

                KeyBordUtils.hideSoftKeyboard(v);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {  //判断得到的焦点控件是否包含EditText
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],    //得到输入框在屏幕中上下左右的位置
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击位置如果是EditText的区域，忽略它，不收起键盘。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略
        return false;
    }


    private void registerNetWorkChangReceiver() {
        //注册网络状态监听广播
        mNetWorkChangReceiver = new NetworkConnectChangedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkChangReceiver, filter);
    }


    private void initTipView() {
        LayoutInflater inflater = getLayoutInflater();
        mTipView = inflater.inflate(R.layout.layout_network_tip, null); //提示View布局
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        //使用非CENTER时，可以通过设置XY的值来改变View的位置
        mLayoutParams.gravity = Gravity.TOP;
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;
    }


    /**
     * 根据网络状态显示或者隐藏提示对话框
     *
     * @param isConnected
     */
    private void netStateChangedUI(boolean isConnected) {
        if (mCheckNetwork) {
            if (isConnected) {
                if (mTipView != null && mTipView.getParent() != null) {
                    mWindowManager.removeView(mTipView);
                }
            } else {
                if (mTipView.getParent() == null) {
                    mWindowManager.addView(mTipView, mLayoutParams);
                }
            }
        }
    }


    /**
     * 网络状态发生变化时的处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChangeEvent(NetworkChangeEvent event) {
        Timber.i("网络发生变化:%s", event.toString());
        mNetConnected = event.isConnected;
        MCloudApp.setIsNetworkConnected(mNetConnected);
        netStateChangedUI(event.isConnected);
    }

    protected void setCheckNetWork(boolean checkNetWork) {
        mCheckNetwork = checkNetWork;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.remove(weakRefActivity);
        unregisterReceiver(mNetWorkChangReceiver);
    }


    @Override
    public void finish() {
        super.finish();
        //当提示View被动态添加后直接关闭页面会导致该View内存溢出，所以需要在finish时移除
        if (mTipView != null && mTipView.getParent() != null) {
            mWindowManager.removeView(mTipView);
        }
    }


    @Override
    public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            super.onBackPressed();
        }
    }
}
