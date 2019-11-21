package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.views.PlayPauseView;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.RecycleViewDivider;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ADMEMotorControlActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.back)
    ImageView mIvBack;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.img_bluetooth)
    ImageView mIvBluetooth;

    @BindView(R.id.spinner)
    Spinner mSpinner;

    @BindView(R.id.et_distance)
    EditText mEtDistance;

    @BindView(R.id.btn_pull_up)
    Button mBtnPullUp;

    @BindView(R.id.btn_pull_down)
    Button mBtnPullDown;

    @BindView(R.id.btn_clear)
    Button mBtnClear;

    @BindView(R.id.play_pause_view)
    PlayPauseView playPauseView;

    @BindView(R.id.btn_count)
    Button mBtnCount;

    @BindView(R.id.tv_distance)
    TextView mTvDistance;

    @BindView(R.id.tv_pulses_number)
    TextView mTvPulseNumber;

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    private CommonAdapter adapter;

    private List<String> distanceList = new ArrayList<>();

    private int countNum = 0;


    public static void startActivity(Context context, String configInfo) {
        Intent intent = new Intent(context, ADMEMotorControlActivity.class);
        intent.putExtra(Extras.ADME_MOTOR_CONTROL_CONFIG_INFO, configInfo);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_admemotor_control;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAdapter();
        initPlayPauseListener();


        testData();
    }

    private void initView() {
        mToolbarTitle.setText("参数设置");
        mIvBluetooth.setVisibility(View.VISIBLE);

        mEtDistance.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtDistance.setHint("默认100");
        mEtDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth);
        }
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL));
        adapter = new CommonAdapter<String>(this, R.layout.listitem_motor_distance, distanceList) {
            @Override
            protected void convert(ViewHolder holder, final String distance, final int position) {
                holder.setText(R.id.tv_number, "计次 " + countNum);
                holder.setText(R.id.tv_interval, "间隔：" + 100 + " mm");
                holder.setText(R.id.tv_distance, distance + " mm");
            }
        };
        mRecyclerView.setAdapter(adapter);
    }


    private void initPlayPauseListener() {
        playPauseView.setPlayPauseListener(new PlayPauseView.PlayPauseListener() {
            @Override
            public void play() {
                // do something
                Toast.makeText(ADMEMotorControlActivity.this, "Play", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void pause() {
                // do something
                Toast.makeText(ADMEMotorControlActivity.this, "Pause", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @OnClick({R.id.back, R.id.img_bluetooth, R.id.btn_confirm, R.id.btn_pull_up, R.id.btn_pull_down, R.id.btn_clear, R.id.btn_count})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.img_bluetooth:
                if (MCloudApp.isIsBluetoothDeviceConnected()) {
                    if (isConfigChange) {
                        isExitMode = false;
                        showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
                    } else {
                        disconnectDevice();
                    }
                } else {
                    findAndConnectBleDevice();
                }
                break;

            case R.id.btn_confirm:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                doConfirm();
                break;

            case R.id.btn_pull_up:
                break;

            case R.id.btn_pull_down:
                break;

            case R.id.btn_clear:
                break;

            case R.id.btn_count:
                break;
        }
    }

    private void doConfirm() {

    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //计米轮参数配置后应答
        if (cmdStr.startsWith("$$7023") && cmdStr.endsWith("\r\n")) {
            dismissLoadingDialog();
            hander.removeCallbacks(dismssDialogRunnable);
            isExitMode = true;
            showSaveDialog("是否保存设备配置参数？");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothStateEvent bluetoothStateEvent) {
        mIvBluetooth.setImageResource(bluetoothStateEvent.isConnected ? R.drawable.ic_bluetooth_connected : R.drawable.ic_bluetooth);
    }


    @Override
    public void onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            if (isConfigChange) {
                isExitMode = true;
                showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
            } else {
                finish();
            }
        } else {
            finish();
        }
    }


    private void testData(){

        distanceList.add("150");
        distanceList.add("200");
        distanceList.add("350");
        distanceList.add("400");
        distanceList.add("550");
        adapter.notifyDataSetChanged();
    }

}
