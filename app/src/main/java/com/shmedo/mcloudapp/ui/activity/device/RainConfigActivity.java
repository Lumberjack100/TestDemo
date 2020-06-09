package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.entity.SetRainPrecisionEntity;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.interfaces.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   RainConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/22 14:33
 * 描述：   配置雨量计
 */
public class RainConfigActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.sp_rain)
    Spinner mSpRain;

    @BindView(R.id.btn_confirm_complete)
    Button mBtnConfirmComplete;

    private String rainResult;

    private ArrayAdapter<String> dataAdapter;

    private String rainAccury;


    public static void startActivity(Context context, String configInfo) {
        Intent intent = new Intent(context, RainConfigActivity.class);
        intent.putExtra(Extras.PARAM_CONFIG_INFO, configInfo);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_rain_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        parseIntent();
    }


    private void initView() {
        mToolbarTitle.setText("配置雨量计");

        String[] debugData = getResources().getStringArray(R.array.rain);
        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, debugData);
        mSpRain.setAdapter(dataAdapter);
        mSpRain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String result = mSpRain.getSelectedItem().toString().replace("mm", "");
                int precision = (int) (Double.parseDouble(result) * 100);
                rainResult = precision + "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.PARAM_CONFIG_INFO)) {
            rainAccury = intent.getStringExtra(Extras.PARAM_CONFIG_INFO);
            if (TextUtils.isEmpty(rainAccury)) {
                return;
            }
            String result = Double.parseDouble(rainAccury) / 100 + "mm";
            SpinnerAdapter spinnerAdapter = mSpRain.getAdapter();
            int count = spinnerAdapter.getCount();
            for (int i = 0; i < count; i++) {
                if (result.equals(spinnerAdapter.getItem(i).toString())) {
                    mSpRain.setSelection(i);
                    break;
                }
            }
        }
    }


    @OnClick({R.id.back, R.id.btn_confirm_complete})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.btn_confirm_complete:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                doConfirm();
                break;
        }
    }

    private void doConfirm() {
        if (rainResult == null) {
            ToastUtils.show("未获取到选中的值");
            return;
        }

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_DELAY_MILLIS);
        SetRainPrecisionEntity entity = new SetRainPrecisionEntity(Integer.parseInt(rainResult));
        String command = CommandManager.getInstance().getCommand(CommandType.SETTING_RAIN_PRECISION, entity);
        sendCommonCommandImmediately(command);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        if (type == CommandType.SETTING_RAIN_PRECISION) {
            if (tempStr.endsWith(CommandResult.ERROR_END)) {
                ToastUtils.show("雨量计精度配置错误!");
                stopProgressRunnable();
                return;
            }
            stopProgressRunnable();
            ToastUtils.show("设置完成");
            hander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (TextUtils.isEmpty(messageEvent) || !messageEvent.startsWith("$$")) {
            return;
        }
        setResultData(messageEvent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
