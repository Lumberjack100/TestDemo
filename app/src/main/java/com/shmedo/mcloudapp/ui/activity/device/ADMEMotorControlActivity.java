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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.views.PlayPauseView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ADMEMotorControlActivity extends BaseDeviceConnectActivity {
    private static final int PULL_UP = 0x0002;

    private static final int PULL_DOWN = 0x0003;

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

    @BindView(R.id.rl_choose_run_mode)
    View chooseRunModeView;

    @BindView(R.id.rl_control_pull)
    View controlPullView;

    @BindView(R.id.tv_distance)
    TextView mTvDistance;

    @BindView(R.id.tv_pulses_number)
    TextView mTvPulseNumber;

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    private CommonAdapter adapter;

    private List<String> distanceList = new ArrayList<>();

    private int countNum = 0;

    private int runMode = PULL_UP;//默认值：上拉

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
        String[] datas = getResources().getStringArray(R.array.pull_mode);
        ArrayAdapter<String> pullModeAdapter = new ArrayAdapter<>(this,R.layout.spinner_item , datas);//android.R.layout.simple_spinner_item
        pullModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(pullModeAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL));
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
                mBtnClear.setEnabled(false);
                mBtnCount.setEnabled(true);

                if (runMode == PULL_UP) {
                    //发送上拉指令
                    sendCommonCommand("##7021,2,0\r\n");
                } else {
                    //发送下降指令
                    sendCommonCommand("##7021,3,0\r\n");
                }
            }

            @Override
            public void pause() {
                mBtnClear.setEnabled(true);
                mBtnCount.setEnabled(false);
                //发送停止指令
                sendCommonCommand("##7021,1,0\r\n");
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
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                chooseRunModeView.setVisibility(View.VISIBLE);
                controlPullView.setVisibility(View.GONE);
                runMode = PULL_UP;
                //发送上拉指令
                sendCommonCommand("##7021,2,0\r\n");
                break;

            case R.id.btn_pull_down:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                chooseRunModeView.setVisibility(View.VISIBLE);
                controlPullView.setVisibility(View.GONE);
                runMode = PULL_DOWN;
                //发送下降指令
                sendCommonCommand("##7021,3,0\r\n");
                break;

            case R.id.btn_clear:
                chooseRunModeView.setVisibility(View.GONE);
                controlPullView.setVisibility(View.VISIBLE);
                distanceList.clear();
                adapter.notifyDataSetChanged();
                break;

            case R.id.btn_count:

                adapter.addItem("166", 0);
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


    private void testData() {

        distanceList.add("150");
//        distanceList.add("200");
//        distanceList.add("350");
//        distanceList.add("400");
//        distanceList.add("550");
        adapter.notifyDataSetChanged();
    }

}
