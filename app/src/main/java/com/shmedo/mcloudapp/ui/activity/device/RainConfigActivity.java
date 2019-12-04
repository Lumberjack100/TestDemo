package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.model.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

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

                DecimalFormat df = new DecimalFormat("0");
                rainResult = df.format(Double.valueOf(result) * 100);
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
            String result = Double.valueOf(rainAccury) / 100 + "mm";
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
        String cmdStr = "##121" + rainResult + "\r\n";
        sendCommonCommand(cmdStr);
        showLoadingDialog("正在发送配置指令...");
        hander.postDelayed(dismssDialogRunnable, 10000);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //参数配置后应答
        if (cmdStr.startsWith("$$121") && cmdStr.endsWith("\r\n")) {
            dismissLoadingDialog();
            hander.removeCallbacks(dismssDialogRunnable);
            ToastUtils.show("设置完成");
            hander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RainConfigActivity.this.finish();
                }
            }, 3000);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
