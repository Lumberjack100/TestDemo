package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 创建者:   gonghe
 * 创建时间:  2020-02-28
 * 描述：    TODO
 */
public class TestActivity extends AppCompatActivity  {
    public static final int REQUEST_ENABLE_BT = 0x002;

    @BindView(R.id.recycleview_bluetooth_device)
    RecyclerView mRecyclerView;

    @BindView(R.id.pb_wifi_loading)
    ProgressBar progressBar;

    @BindView(R.id.tv_scan_state)
    TextView mTvScanState;

    @BindView(R.id.btn_scan)
    Button btnScan;

    private CommonAdapter adapter;

    private DeviceAdapter deviceAdapter;

    private List<MDevice> deviceList = new ArrayList<>();


    private int index = 0;

    private Handler hander;

    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (index > 20)
                return;

            index++;
            MDevice mDevice = new MDevice();

//        adapter.addItem(mDevice);
            deviceAdapter.addData(mDevice);

            hander.postDelayed(dismssDialogRunnable, 5);
        }
    };

    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, TestActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Timber.d("执行了 onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("执行了 onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("执行了 onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("执行了 onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("执行了 onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("执行了 onDestroy()");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("执行了 onSaveInstanceState()");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Timber.d("执行了 onRestoreInstanceState()");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("执行了 onCreate()");
        setContentView(R.layout.device_list);
        ButterKnife.bind(this);

        hander = new Handler();
        initAdapter2();
        startDiscoveryDevice();
    }


    @OnClick({R.id.btn_scan, R.id.LL_close})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btnScan.getText().toString().contains("停止扫描")) {
                    updateViewState(false);
                } else {
                    startDiscoveryDevice();
                }
                break;

            case R.id.LL_close:
                finish();
                break;
        }
    }

    private void updateViewState(boolean isScanning) {
        progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
        mTvScanState.setVisibility(isScanning ? View.VISIBLE : View.GONE);
        btnScan.setText(isScanning ? "停止扫描" : "开始扫描");
    }


    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    public void startDiscoveryDevice() {
        deviceList.clear();
        index++;
        for(int i = 0;i<20;i++) {
            MDevice mDevice = new MDevice();
            deviceAdapter.addData(mDevice);
        }

//        hander.postDelayed(dismssDialogRunnable, 10);
//        updateViewState(true);
    }


    private void initAdapter2() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration());
        deviceAdapter = new DeviceAdapter(R.layout.item_bluetoothdevice, deviceList);
        mRecyclerView.setAdapter(deviceAdapter);

        deviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
//                showTipDialog("点击了 "+position+" 设备");

                WifiConnectionActivity.startActivity(TestActivity.this);
            }
        });
    }


    class DeviceAdapter extends BaseQuickAdapter<MDevice, BaseViewHolder> {
        public DeviceAdapter(int layoutResId, @org.jetbrains.annotations.Nullable List<MDevice> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, MDevice mDevice) {
            holder.setText(R.id.tv_dev_name, "New测试设备：" + holder.getAdapterPosition());
            holder.setText(R.id.tv_dev_mac, "00:00:00:00:00:00");
            holder.setText(R.id.tv_dev_signal, "00dBm");
        }
    }



}
