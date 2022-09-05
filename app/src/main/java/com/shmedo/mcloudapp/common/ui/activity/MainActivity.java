package com.shmedo.mcloudapp.common.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gyf.immersionbar.ImmersionBar;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.MineFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.DeviceModuleMainFragment;
import com.shmedo.mcloudapp.util.UpdataManagerUtil;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    private DeviceModuleMainFragment deviceModuleMainFragment;
    private MineFragment mineFragment;
    private Fragment currentFragment;


    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("CurrentFragment", currentFragment.getClass().getName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCheckNetWork(true);
        initView(savedInstanceState);
        UpdataManagerUtil.checkNewVersion2(this, false);
        if (MCloudApp.getCurrentUserInfo() != null && MCloudApp.getCurrentUserInfo().getUser() != null)
            CrashReport.setUserId(MCloudApp.getCurrentUserInfo().getUser().getAccount());  //该用户本次启动后的异常日志用户account
    }

    private void initView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {  // “内存重启”时调用
            String curTag = savedInstanceState.getString("CurrentFragment");
            currentFragment = getSupportFragmentManager().findFragmentByTag(curTag);
            deviceModuleMainFragment = (DeviceModuleMainFragment) getSupportFragmentManager().findFragmentByTag(DeviceModuleMainFragment.class.getName());
            mineFragment = (MineFragment) getSupportFragmentManager().findFragmentByTag(MineFragment.class.getName());

            if (deviceModuleMainFragment == null)
                deviceModuleMainFragment = new DeviceModuleMainFragment();

            if (mineFragment == null)
                mineFragment = new MineFragment();

            // 解决重叠问题
            getSupportFragmentManager().beginTransaction()
                    .hide(deviceModuleMainFragment)
                    .hide(mineFragment)
                    .show(currentFragment)
                    .commit();
        } else {
            deviceModuleMainFragment = new DeviceModuleMainFragment();
            mineFragment = new MineFragment();
            switchFrgment(0);
        }
        initBottomNavigationItemSelectedListener();
    }

    private void initBottomNavigationItemSelectedListener() {
        //为底部导航设置条目选中监听
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_config_module:
                        switchFrgment(0);
                        break;

                    case R.id.item_me_module:
                        switchFrgment(1);
                        break;
                }
                return true;
                //这里返回true，表示事件已经被处理。如果返回false，为了达到条目选中效果，还需要下面的代码
                // item.setChecked(true);  不论点击了哪一个，都手动设置为选中状态true（该控件并没有默认实现)
                // 。如果不设置，只有第一个menu展示的时候是选中状态，其他的即便被点击选中了，图标和文字也不会做任何更改
            }
        });
    }

    /**
     * switch the fragment accordting to id
     */
    private void switchFrgment(int i) {
        switch (i) {
            case 0:
                showFragment(deviceModuleMainFragment);
                //设置系统栏(状态栏、导航栏)的背景色、字体等
                ImmersionBar.with(this)
                        .fitsSystemWindows(true)  //使用该属性,必须指定状态栏颜色
                        .statusBarColor(R.color.white)
                        .statusBarDarkFont(true)
                        .navigationBarDarkIcon(true)
                        .navigationBarColor(R.color.white)
                        .init();
                break;

            case 1:
                showFragment(mineFragment);
                //设置系统栏(状态栏、导航栏)的背景色、字体等
                ImmersionBar.with(this)
                        .fitsSystemWindows(true)  //使用该属性,必须指定状态栏颜色
                        .statusBarColor(R.color.white)
                        .statusBarDarkFont(true)
                        .navigationBarDarkIcon(true)
                        .navigationBarColor(R.color.white)
                        .init();
                break;
        }
    }

    private void showFragment(Fragment fragment) {
        if (currentFragment != fragment) {//  判断传入的fragment是不是当前的currentFragmentgit
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (!fragment.isAdded()) { //  判断传入的fragment是否已经被add()过
                transaction.add(R.id.content_frame, fragment, fragment.getClass().getName());
                if (currentFragment != null) {
                    transaction.hide(currentFragment);
                }
            } else {
                transaction.hide(currentFragment).show(fragment);
            }
            currentFragment = fragment;  //  然后将传入的fragment赋值给currentFragment
            transaction.commit();
        }
    }

    /**
     * 解决Fragment中的onActivityResult()方法无响应问题。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 1.使用getSupportFragmentManager().getFragments()获取到当前Activity中添加的Fragment集合
         * 2.遍历Fragment集合，手动调用在当前Activity中的Fragment中的onActivityResult()方法。
         */
        if (getSupportFragmentManager().getFragments() != null && getSupportFragmentManager().getFragments().size() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment mFragment : fragments) {
                mFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast toast = Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
