package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleDataCenterFragment;

import butterknife.BindView;

public class DataCenterActivity extends BaseActivity {
    private static final String CONNECT_WAY = "connect_way";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    public static final int NET_CONNECT = 0x001;//网络连接
    public static final int BLE_CONNECT = 0x002;//蓝牙连接
    private int connectWay = NET_CONNECT;

    private Fragment fragment;


    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, DataCenterActivity.class);
        intent.putExtra(CONNECT_WAY, connectWay);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_data_center;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("数据中心");
        parseIntent();
        initFragment();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(CONNECT_WAY)) {
            connectWay = intent.getIntExtra(CONNECT_WAY, NET_CONNECT);
        }
    }

    private void initFragment() {
        if (connectWay == NET_CONNECT) {

        } else {
            fragment = new BleDataCenterFragment();
        }

        replaceFragment(fragment);
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}
