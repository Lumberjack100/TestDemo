package com.shmedo.mcloudapp.common.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gyf.immersionbar.ImmersionBar;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.MineFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BluetoothDeviceListFragment;
import com.shmedo.mcloudapp.maps.ui.activity.MapActivity;
import com.shmedo.mcloudapp.projects.ui.fragment.ProjectListFragment;
import com.shmedo.mcloudapp.util.permission.UpdataManagerUtil;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class NewMainActivity extends BaseActivity {
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    private BluetoothDeviceListFragment bluetoothDeviceListFragment;
    private ProjectListFragment projectListFragment;
    private MineFragment mineFragment;
    private Fragment currentFragment;


    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, NewMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_main;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("CurrentFragment", currentFragment.getClass().getName());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);
        if (XPermissionUtils.checkPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            UpdataManagerUtil.requestPermissionForInstallPackage(this, false);//版本更新
        }
    }

    private void initView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {  // “内存重启”时调用
            String curTag = savedInstanceState.getString("CurrentFragment");
            currentFragment = getSupportFragmentManager().findFragmentByTag(curTag);
            bluetoothDeviceListFragment = (BluetoothDeviceListFragment) getSupportFragmentManager().findFragmentByTag(BluetoothDeviceListFragment.class.getName());
            projectListFragment = (ProjectListFragment) getSupportFragmentManager().findFragmentByTag(ProjectListFragment.class.getName());
            mineFragment = (MineFragment) getSupportFragmentManager().findFragmentByTag(MineFragment.class.getName());

            if (bluetoothDeviceListFragment == null)
                bluetoothDeviceListFragment = new BluetoothDeviceListFragment();

            if (projectListFragment == null)
                projectListFragment = new ProjectListFragment();

            if (mineFragment == null)
                mineFragment = new MineFragment();

            // 解决重叠问题
            getSupportFragmentManager().beginTransaction()
                    .hide(bluetoothDeviceListFragment)
                    .hide(projectListFragment)
                    .hide(mineFragment)
                    .show(currentFragment)
                    .commit();
        } else {
            bluetoothDeviceListFragment = new BluetoothDeviceListFragment();
            projectListFragment = new ProjectListFragment();
            mineFragment = new MineFragment();
            switchFrgment(0);
        }

        initBottomNavigationItemSelectedListener();
    }

    private void initBottomNavigationItemSelectedListener() {
        //为底部导航设置条目选中监听
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_config_module:
                        switchFrgment(0);
                        break;

                    case R.id.item_project_module:
                        switchFrgment(1);
                        break;

                    case R.id.item_me_module:
                        switchFrgment(2);
                        break;
                }

                return true;
                //这里返回true，表示事件已经被处理。如果返回false，为了达到条目选中效果，还需要下面的代码
                // item.setChecked(true);  不论点击了哪一个，都手动设置为选中状态true（该控件并没有默认实现)
                // 。如果不设置，只有第一个menu展示的时候是选中状态，其他的即便被点击选中了，图标和文字也不会做任何更改
            }
        });
    }

    @OnClick({R.id.map_module_view})
    public void onClick(View v) {
        if (v.getId() == R.id.map_module_view) {
            MapActivity.startActivity(NewMainActivity.this);
        }
    }

    /**
     * switch the fragment accordting to id
     */
    private void switchFrgment(int i) {
        switch (i) {
            case 0:
                showFragment(bluetoothDeviceListFragment);
                ImmersionBar.with(this)
                        .statusBarColor(R.color.colorPrimary)
                        .statusBarDarkFont(false)
                        .navigationBarDarkIcon(true)
                        .navigationBarColor(R.color.white)
                        .init();
                break;
            case 1:
                showFragment(projectListFragment);
//                ImmersionBar.with(this)
//                        .statusBarDarkFont(false)
//                        .navigationBarDarkIcon(true)
//                        .navigationBarColor(R.color.white)
//                        .init();
                break;
            case 2:
                showFragment(mineFragment);
                ImmersionBar.with(this)
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

}
