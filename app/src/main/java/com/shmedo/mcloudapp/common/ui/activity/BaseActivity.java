package com.shmedo.mcloudapp.common.ui.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.NetworkUtils;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.ForceToLoginEvent;
import com.shmedo.core.event.NetworkChangeEvent;
import com.shmedo.mcloudapp.MCloudApplication;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.viewmodels.ShareViewModel;
import com.shmedo.mcloudapp.util.HandleBackUtil;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.umeng.analytics.MobclickAgent;

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
    //防止按钮重复点击设置的时间间隔
    private static final int DOUBLE_CLICK_TIME_INTERVAL = 1500;

    private ViewModelProvider mActivityProvider;
    private ViewModelProvider mApplicationProvider;

    private View mTipView;

    private WindowManager mWindowManager;

    private WindowManager.LayoutParams mLayoutParams;

    protected MaterialDialog loadingDialog = null;

    /**
     * 判断当前Activity是否在前台。
     */
    protected boolean isActive = false;

    private boolean mCheckNetwork = false;/*默认检查网络状态*/

    protected boolean mNetConnected;/*网络连接的状态，true表示有网络，flase表示无网络连接*/

    protected ShareViewModel shareViewModel;

    protected abstract int getLayoutId();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 解决 Android在应用设置里关闭权限，导致APP重启进程造成的无用户数据异常
        if (null != savedInstanceState) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        //初始化沉浸式
        initImmersionBar();
        initTipView();//初始化提示View

        shareViewModel = getApplicationScopeViewModel(ShareViewModel.class);
        shareViewModel.getNetworkChangeEvent().observe(this, new Observer<NetworkChangeEvent>() {
            @Override
            public void onChanged(NetworkChangeEvent networkChangeEvent) {
                Timber.i("网络发生变化:%s", networkChangeEvent.toString());
                mNetConnected = networkChangeEvent.isConnected;
                netStateChangedUI(networkChangeEvent.isConnected);
            }
        });
        shareViewModel.getForceToLoginEvent().observe(this, new Observer<ForceToLoginEvent>() {
            @Override
            public void onChanged(ForceToLoginEvent forceToLoginEvent) {
                if (isActive) { // 判断Activity是否在前台，防止非前台的Activity也处理这个事件，造成打开多个LoginActivity的问题。
                    // force to login
                    MCloudApp.logout();
                    LoginActivity.startActivity(BaseActivity.this);
                }
            }
        });
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && UiUtils.isTranslucentOrFloating(this)) {
//            Timber.i("===api 26 全屏横竖屏切换 crash");
//            return;
//        }
        super.setRequestedOrientation(requestedOrientation);
    }

    /**
     * 初始化沉浸式
     * Init immersion bar.
     */
    protected void initImmersionBar() {
        //设置共同沉浸式样式
//        ImmersionBar.with(this).navigationBarColor(R.color.colorPrimary).init();
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
    protected void onResume() {
        super.onResume();
        isActive = true;
        String name = getClass().getName();
        Timber.i("onResume,activity=%s", name);
        //统计时长
        MobclickAgent.onResume(this);

        //在无网络情况下打开APP时，系统不会发送网络状况变更的Intent，需要自己手动检查
        netStateChangedUI(NetworkUtils.isConnected());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        String name = getClass().getName();
        Timber.i("onPause,activity=%s", name);
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        String name = getClass().getName();
        Timber.i("onStop,activity=%s", name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar Home按钮返回事件
        if (item.getItemId() == android.R.id.home) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(this.getWindow().getDecorView());
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

    protected void showWaitDialog(String message) {
        WaitDialog.show(message)
                .setOnBackPressedListener(new OnBackPressedListener() {//返回按键监听
                    @Override
                    public boolean onBackPressed() {
//                        cancelRequest();
                        WaitDialog.dismiss();
                        return false;
                    }
                });
    }

    protected void dismissWaitDialog() {
        WaitDialog.dismiss();
    }

    protected void showTipDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .positiveColorRes(R.color.blue_52B4F8);
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
                com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(v);
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
    protected boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if ((v instanceof EditText)) {  //判断得到的焦点控件是否包含EditText
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

    private void initTipView() {
        LayoutInflater inflater = getLayoutInflater();
        mTipView = inflater.inflate(R.layout.layout_network_tip, null); //提示View布局
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
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

    protected void setCheckNetWork(boolean checkNetWork) {
        mCheckNetwork = checkNetWork;
    }

    protected boolean isDoubleClick(View v) {
        Object tag = v.getTag(v.getId());
        long beforeTimeMillis = tag != null ? (long) tag : 0;
        long timeInMillis = System.currentTimeMillis();
        v.setTag(v.getId(), timeInMillis);

        long interval = timeInMillis - beforeTimeMillis;
        return interval < DOUBLE_CLICK_TIME_INTERVAL;
    }

    protected <T extends ViewModel> T getActivityScopeViewModel(@NonNull Class<T> modelClass) {
        if (mActivityProvider == null) {
            mActivityProvider = new ViewModelProvider(this);
        }
        return mActivityProvider.get(modelClass);
    }

    protected <T extends ViewModel> T getApplicationScopeViewModel(@NonNull Class<T> modelClass) {
        if (mApplicationProvider == null) {
            mApplicationProvider = new ViewModelProvider((MCloudApplication) this.getApplicationContext(),
                    getAppFactory(this));
        }
        return mApplicationProvider.get(modelClass);
    }

    private ViewModelProvider.Factory getAppFactory(Activity activity) {
        Application application = checkApplication(activity);
        return ViewModelProvider.AndroidViewModelFactory.getInstance(application);
    }

    private Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
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
