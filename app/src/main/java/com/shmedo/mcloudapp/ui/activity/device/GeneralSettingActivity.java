package com.shmedo.mcloudapp.ui.activity.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.bleutil.LogTag;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   GeneralSettingActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/15 15:23
 * 描述：    通用配置页面——采集器配置
 */
public class GeneralSettingActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_collector_address)
    ImageView mIvCollectorAddress;
    @BindView(R.id.et_collector_address)
    EditText mEtCollectorAddress;
    @BindView(R.id.iv_calculating_time)
    ImageView mIvCalculatingTime;
    @BindView(R.id.et_calculating_time)
    EditText mEtCalculatingTime;
    @BindView(R.id.iv_standby_time)
    ImageView mIvStandbyTime;
    @BindView(R.id.et_standby_time)
    EditText mEtStandbyTime;
    @BindView(R.id.iv_collect_time)
    ImageView mIvCollectTime;
    @BindView(R.id.et_collect_time)
    EditText mEtCollectTime;
    @BindView(R.id.btn_confirm_complete)
    Button mBtnConfirmComplete;

    private String collectorAddress;
    private String calculatTime;
    private String standbyTime;
    private String collectTime;

    @Override
    protected int initContentView() {
        return R.layout.activity_general_setting;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initData();
    }


    private void initData() {
        if (DeviceFragment.collectorInfoSub != null) {
            mEtCollectorAddress.setText(DeviceFragment.collectorInfoSub.getCollectorAddress());
            mEtCalculatingTime.setText(DeviceFragment.collectorInfoSub.getWorkTime());
            mEtStandbyTime.setText(DeviceFragment.collectorInfoSub.getStandbyTime());
            mEtCollectTime.setText(DeviceFragment.collectorInfoSub.getCollectorInterval());
        }
    }


    private void initView() {
        mToolbarTitle.setText("通用设置");
    }


    @OnClick({R.id.iv_collector_address, R.id.iv_calculating_time, R.id.iv_standby_time, R.id.iv_collect_time, R.id.btn_confirm_complete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_collector_address:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.iv_calculating_time:
                showTipDialog(getResources().getString(R.string.calculating_time));
                break;

            case R.id.iv_standby_time:
                showTipDialog(getResources().getString(R.string.standby_time));
                break;

            case R.id.iv_collect_time:
                showTipDialog(getResources().getString(R.string.collect_time));
                break;

            case R.id.btn_confirm_complete:
                sendCollector();
                break;
        }
    }


    private void sendCollector() {
        int address = Integer.parseInt(mEtCollectorAddress.getText().toString().trim());
        if (address > 0 && address < 255) {
            collectorAddress = String.valueOf(address);
        } else {
            ToastUtil.showShortToast("采集器地址输入有误");
            return;
        }

        calculatTime = mEtCalculatingTime.getText().toString().trim();
        standbyTime = mEtStandbyTime.getText().toString().trim();
        collectTime = mEtCollectTime.getText().toString().trim();
        if (StringUtil.isNullOrEmpty(calculatTime)) {
            ToastUtil.showShortToast("解算时间不能为空");
        } else if (StringUtil.isNullOrEmpty(standbyTime)) {
            ToastUtil.showShortToast("待机时间不能为空");
        } else if (StringUtil.isNullOrEmpty(collectTime)) {
            ToastUtil.showShortToast("采集时间不能为空");
        } else {
            //这里需要判断采集器的型号，去确定##100后面的数字是否是01
            List<String> list = new ArrayList<>();
            StringBuilder result1 = new StringBuilder();
            StringBuilder result2 = new StringBuilder();
            StringBuilder result3 = new StringBuilder();
            StringBuilder result4 = new StringBuilder();

            result1.append("##147" + collectorAddress + "\r\n");
            result3.append("##161" + DeviceFragment.collectorType + StringUtil.formatStringFive(calculatTime) + "\r\n");
            result2.append("##160" + DeviceFragment.collectorType + StringUtil.formatStringFour(standbyTime) + "\r\n");
            result4.append("##163" + DeviceFragment.collectorType + StringUtil.formatStringFour(collectTime) + "\r\n");
            list.add(String.valueOf(result1));
            list.add(String.valueOf(result2));
            list.add(String.valueOf(result3));
            list.add(String.valueOf(result4));
            for (int i = 0; i < list.size(); i++) {
                if (DeviceFragment.isConnected) {
                    Log.i(LogTag.INFO_TAG, "==采集器指令=========" + list.get(i).toString());
                    //采集器
                    com.shmedo.mcloudapp.bluetooth.Message msg
                            = new com.shmedo.mcloudapp.bluetooth.Message("collector", list.get(i).toString(), true);
                    if (DeviceFragment.mdBluetoothManager != null) {
                        DeviceFragment.mdBluetoothManager.writeMessage(msg);
                    }
                } else {
                    ToastUtil.showShortToast("蓝牙未连接");
                }
            }
        }
    }

}
