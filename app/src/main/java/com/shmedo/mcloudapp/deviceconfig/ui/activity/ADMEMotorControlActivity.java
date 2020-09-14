package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.event.BluetoothConnectStateEvent;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.view.PlayPauseView;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

;

public class ADMEMotorControlActivity extends BaseDeviceConnectActivity {
    private static final int PULL_AUTO = 0x0001;

    private static final int HAND_PULL_UP = 0x0002;

    private static final int HAND_PULL_DOWN = 0x0003;

    private static final int STATE_PLAY = 0x0004;

    private static final int STATE_PAUSE = 0x0005;

    private static final int STATE_CLEAR = 0x0006;


    @BindView(R.id.back)
    ImageView mIvBack;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.img_bluetooth)
    ImageView mIvBluetooth;

    @BindView(R.id.spinner)
    Spinner mSpinner;

    @BindView(R.id.et_speed)
    EditText mEtSpeed;

    @BindView(R.id.et_distance)
    EditText mEtDistance;

    @BindView(R.id.btn_pull_up)
    Button mBtnPullUp;

    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;

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

    private int runMode = PULL_AUTO;//默认值：上拉

    private String configInfo;//查询测试控制电机指令

    private String pulseNumber;//脉冲数

    private String distance;//距离

    private String speedPullUp;//上拉速度

    private String speedPullDown;//下降速度

    private Handler UIHandler;

    private MyRunnable mRunnable;

    private static int repeatNum = 0;//当查询电机脉冲数重复超过一定次数(3次)时，判定电机停止


    public static void startActivity(Context context, String configInfo) {
        Intent intent = new Intent(context, ADMEMotorControlActivity.class);
        intent.putExtra(AppContants.Extras.ADME_MOTOR_CONTROL_CONFIG_INFO, configInfo);
        context.startActivity(intent);
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            sendCommonCommand("##7024\r\n");
            UIHandler.postDelayed(this, 1000);
        }
    }

    private void startRunnable() {
        if (mRunnable == null) {
            mRunnable = new MyRunnable();
            UIHandler.postDelayed(mRunnable, 0);
        }
    }

    private void stopRunnable() {
//        UIHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                UIHandler.removeCallbacks(mRunnable);
//                mRunnable = null;
//            }
//        }, 3000);

        UIHandler.removeCallbacks(mRunnable);
        mRunnable = null;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_admemotor_control;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        parseIntent();
        initAdapter();
        initPlayPauseListener();
    }

    private void initView() {
        mToolbarTitle.setText("参数设置");
        mIvBluetooth.setVisibility(View.GONE);

        mEtSpeed.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtSpeed.setHint("请输入0--99");
        mEtSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        mEtDistance.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtDistance.setHint("默认100");
        mEtDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        updateDistanceAndPulseNumber("0", "0");

        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth);
        }
    }

    private void parseIntent() {
        UIHandler = new Handler();

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(AppContants.Extras.ADME_MOTOR_CONTROL_CONFIG_INFO)) {
            configInfo = intent.getStringExtra(AppContants.Extras.ADME_MOTOR_CONTROL_CONFIG_INFO);
        }

        //##7020,电机测试状态（1：停止，2：上拉，3：下降）,电机测试距离（0：一直持续，其他数值为设定的运动距离单位：mm）,上拉速度,下降速度
        //$$7020,2,500,5,15
        if (TextUtils.isEmpty(configInfo)) {
            Timber.e("configInfo 为空或者null");
            return;
        }

        if (!configInfo.startsWith(CommandResult.COMMAND_RESULT_HEADER)) {
            Timber.e("configInfo 格式错误:" + configInfo);
            return;
        }

        String[] cmdArray = configInfo.replace("\r\n", "").split(",");
        if (cmdArray.length < 5) {
            Timber.e("configInfo 格式错误:" + configInfo);
            return;
        }

        speedPullUp = cmdArray[3];
        speedPullDown = cmdArray[4];
        if (cmdArray[1].equals("2")) {//上拉
            mSpinner.setSelection(0);
            mEtSpeed.setText(speedPullUp);
        } else if (cmdArray[1].equals("3")) {//下降
            mSpinner.setSelection(1);
            mEtSpeed.setText(speedPullDown);
        } else {//停止
            mSpinner.setSelection(0);
            mEtSpeed.setText(speedPullUp);
        }

        mEtDistance.setText(cmdArray[2]);
    }

    private void initAdapter() {
        String[] datas = getResources().getStringArray(R.array.pull_mode);
        ArrayAdapter<String> pullModeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, datas);//android.R.layout.simple_spinner_item
        pullModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(pullModeAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAdapter<String>(this, R.layout.listitem_motor_distance, distanceList) {
            @Override
            protected void convert(CommonViewHolder holder, final String distance, final int position) {
                holder.setText(R.id.tv_number, "计次 " + (distanceList.size() - position));
                holder.setText(R.id.tv_distance, distance + " mm");

                try {
                    double interval;
                    double curDistance = Double.parseDouble(distance);
                    if (position + 1 < distanceList.size()) {
                        double lastDistance = Double.parseDouble(distanceList.get(position + 1));
                        interval = curDistance - lastDistance;
                    } else {
                        interval = curDistance;
                    }

                    holder.setText(R.id.tv_interval, "间隔：" + String.format("%.3f", interval) + " mm");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        mRecyclerView.setAdapter(adapter);
    }


    private void initPlayPauseListener() {
        playPauseView.setPlayPauseListener(new PlayPauseView.PlayPauseListener() {
            @Override
            public void play() {
                setPlayPauseState(STATE_PLAY);
            }

            @Override
            public void pause() {
                setPlayPauseState(STATE_PAUSE);
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
                    findAndConnectSpecificDevice();
                }
                break;

            case R.id.btn_confirm:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

                if (mBtnConfirm.getText().toString().equals("确定")) {
                    doConfirm();
                } else {
                    //发送停止指令
                    sendCommonCommand("##7021,1,0\r\n");
//                    stopRunnable();
//                    mBtnConfirm.setText("确定");
                }
                break;

            case R.id.btn_pull_up://上拉
                runMode = HAND_PULL_UP;
                setPlayPauseState(STATE_PLAY);
                break;

            case R.id.btn_pull_down://下降
                runMode = HAND_PULL_DOWN;
                setPlayPauseState(STATE_PLAY);
                break;

            case R.id.btn_clear://清空
                setPlayPauseState(STATE_CLEAR);
                break;

            case R.id.btn_count://计数
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                if (!TextUtils.isEmpty(distance)) {
                    adapter.addItem(distance, 0);
                    mRecyclerView.scrollToPosition(0);
                }
                break;
        }
    }


    private void setPlayPauseState(int state) {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }

        switch (state) {
            case STATE_PLAY:
                chooseRunModeView.setVisibility(View.GONE);
                controlPullView.setVisibility(View.VISIBLE);
                playPauseView.play();
                mBtnClear.setEnabled(false);
                mBtnCount.setEnabled(true);
                mBtnConfirm.setEnabled(false);
                if (runMode == HAND_PULL_UP) {
                    //发送上拉指令
                    sendCommonCommand("##7021,2,0\r\n");
                } else if (runMode == HAND_PULL_DOWN) {
                    //发送下降指令
                    sendCommonCommand("##7021,3,0\r\n");
                }
                startRunnable();
                break;

            case STATE_PAUSE:
                chooseRunModeView.setVisibility(View.GONE);
                controlPullView.setVisibility(View.VISIBLE);
                mBtnClear.setEnabled(true);
                mBtnCount.setEnabled(false);
                mBtnConfirm.setEnabled(true);
                //发送停止指令
                sendCommonCommand("##7021,1,0\r\n");
//                stopRunnable();
                break;

            case STATE_CLEAR:
                chooseRunModeView.setVisibility(View.VISIBLE);
                controlPullView.setVisibility(View.GONE);
                mBtnClear.setEnabled(false);
                mBtnCount.setEnabled(true);
                mBtnConfirm.setEnabled(true);
                pulseNumber = "0";
                distance = "0";
                updateDistanceAndPulseNumber(distance, pulseNumber);
                distanceList.clear();
                adapter.notifyDataSetChanged();
                break;
        }
    }


    private void doConfirm() {
        String speed = mEtSpeed.getText().toString().trim();
        String distance = mEtDistance.getText().toString().trim();
        if (TextUtils.isEmpty(speed)) {
            ToastUtils.show("速度不能为空");
            return;
        }

        if (TextUtils.isEmpty(distance)) {
            ToastUtils.show("距离不能为空");
            return;
        }

        if (Integer.parseInt(distance) <= 0) {
            ToastUtils.show("距离请输入正整数");
            return;
        }

        //拼接控制电机指令
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("##7021,");
        if (mSpinner.getSelectedItemPosition() == 0) {
            stringBuilder.append("2,");
            speedPullUp = speed;
        } else {
            stringBuilder.append("3,");
            speedPullDown = speed;
        }
        runMode = PULL_AUTO;
        speedPullUp = TextUtils.isEmpty(speedPullUp) ? "0" : speedPullUp;
        speedPullDown = TextUtils.isEmpty(speedPullDown) ? "0" : speedPullDown;

        stringBuilder.append(distance + ",");
        stringBuilder.append(speedPullUp + ",");
        stringBuilder.append(speedPullDown + "\r\n");
        String cmdStr = String.valueOf(stringBuilder);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
        sendCommonCommand(cmdStr);
        Timber.d("控制电机" + (mSpinner.getSelectedItemPosition() == 0 ? "上拉" : "下降") + "指令==" + cmdStr);
        //轮询查询电机状态
        startRunnable();
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //控制电机上拉、下降指令应答
        if (!cmdStr.startsWith("$$7021,1")
                && (cmdStr.startsWith("$$7021,2") || cmdStr.startsWith("$$7021,3"))) {
            stopProgressRunnable();
            if (runMode == PULL_AUTO) {
                ToastUtils.show("已设置控制电机指令");
                mBtnConfirm.setText("停止");
            }
            return;
        }

        //停止电机指令应答
        if (cmdStr.startsWith("$$7021,1")) {
//            ToastUtils.show("电机停止");
//            mBtnConfirm.setText("确定");
            repeatNum = 0;
//            stopRunnable();
            return;
        }

        //查询脉冲数，距离应答
        if (cmdStr.startsWith("$$7024") && cmdStr.endsWith("\r\n")) {
            String[] cmdArray = cmdStr.replace("\r\n", "").split(",");
            if (cmdArray.length < 3 || TextUtils.isEmpty(cmdArray[1].trim()) || TextUtils.isEmpty(cmdArray[2].trim())) {
                Timber.d("查询脉冲数，距离应答指令错误");
                return;
            }

            //电机已经停止，不用再轮询电子状态
//            if (!TextUtils.isEmpty(pulseNumber) && pulseNumber.equals(cmdArray[1])) {
//                repeatNum++;
//                if (repeatNum >= 3) {
//                    ToastUtils.show("电机停止");
//                    stopRunnable();
//                    mBtnConfirm.setText("确定");
//                    mBtnConfirm.setEnabled(true);
//                    return;
//                }
//            }

            distance = cmdArray[2];
            pulseNumber = cmdArray[1];

            try {
                distance = String.format(Locale.getDefault(),"%.3f", Double.parseDouble(cmdArray[2]));
            } catch (Exception ex) {
                distance = cmdArray[2];
                ex.printStackTrace();
            }
            updateDistanceAndPulseNumber(distance, pulseNumber);
            return;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothConnectStateEvent bluetoothConnectStateEvent) {
        mIvBluetooth.setImageResource(bluetoothConnectStateEvent.isConnected ? R.drawable.ic_bluetooth_connected : R.drawable.ic_bluetooth);
        playPauseView.setEnabled(bluetoothConnectStateEvent.isConnected);
    }


    private void updateDistanceAndPulseNumber(String distance, String number) {
        SpannableStringBuilder builder = new SpannableStringBuilder(distance);
        AbsoluteSizeSpan span = new AbsoluteSizeSpan(22, true);
        builder.setSpan(span, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("   mm");
        mTvDistance.setText(builder);

        builder = new SpannableStringBuilder(number);
        builder.setSpan(span, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("   个");
        mTvPulseNumber.setText(builder);
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
}
