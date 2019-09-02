package com.shmedo.mcloudapp.ui.activity.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.ui.fragment.AdvanceSetFragment;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.util.bleutil.LogTag;
import com.shmedo.mcloudapp.views.LoadingDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   OsmometerConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/22 14:44
 * 描述：   渗压计功能配置
 */
public class OsmometerConfigActivity extends BaseActivity {
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.iv_osmometer_address) ImageView mIvOsmometerAddress;
    @BindView(R.id.et_osmometer_address) EditText mEtOsmometerAddress;
    @BindView(R.id.iv_water_alarm_value) ImageView mIvWaterAlarmValue;
    @BindView(R.id.et_water_alarm_value) EditText mEtWaterAlarmValue;
    @BindView(R.id.iv_water_revised) ImageView mIvWaterRevised;
    @BindView(R.id.et_water_revised) EditText mEtWaterRevised;
    @BindView(R.id.iv_osmometer_cord) ImageView mIvOsmometerCord;
    @BindView(R.id.et_osmometer_cord) EditText mEtOsmometerCord;
    @BindView(R.id.iv_nozzel_height) ImageView mIvNozzelHeight;
    @BindView(R.id.et_nozzel_height) EditText mEtNozzelHeight;
    @BindView(R.id.et_note) EditText mEtNote;
    @BindView(R.id.btn_confirm_complete) Button mBtnConfirmComplete;


    private String osmometerAddress;    //渗压计地址
    private String depthTriggerValue;   //深度触发值-水位报警值
    private String depthCorrection;     //深度修正值-
    private String osmometerLength;     //渗压计绳长
    private String nozzelHeight;    //管口高程

    private UserConfig uc;

    @Override protected int initContentView() {
        return R.layout.activity_osmometer_config;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("配置渗压计");
        AdvanceSetFragment.modityHint("随手一记，好记性不如烂笔头",mEtNote);
    }


    private void initData() {
        if (DeviceFragment.mFuncSubInfo != null) {
            Log.e(LogTag.INFO_TAG, "====渗压计页面参数===" + DeviceFragment.mFuncSubInfo.toString());
            mEtOsmometerAddress.setText(DeviceFragment.mFuncSubInfo.getOsmometerAddress());
            mEtWaterAlarmValue.setText(String.valueOf(DeviceFragment.mFuncSubInfo.getDepthTrigger()));
            mEtWaterRevised.setText(String.valueOf(DeviceFragment.mFuncSubInfo.getDepthCorrect()));
            mEtOsmometerCord.setText(DeviceFragment.mFuncSubInfo.getSyCordLength());
            mEtNozzelHeight.setText(String.valueOf(DeviceFragment.mFuncSubInfo.getTemperatureCorrect()));
        }
        uc = UserConfig.getConfig(this,CommonVariable.OSMOMETER_NOTE);
        mEtNote.setText(uc.readString(CommonVariable.OSMOMETER_NOTE));
    }


    @OnClick({ R.id.iv_osmometer_address, R.id.iv_water_alarm_value, R.id.iv_water_revised,
                 R.id.iv_osmometer_cord, R.id.iv_nozzel_height, R.id.btn_confirm_complete })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_osmometer_address:
                LoadingDialog.showScanResultDialog(this,
                    getResources().getString(R.string.osmometer_address));
                break;
            case R.id.iv_water_alarm_value:
                LoadingDialog.showScanResultDialog(this,
                    getResources().getString(R.string.water_alarm_value));
                break;
            case R.id.iv_water_revised:
                LoadingDialog.showScanResultDialog(this,
                    getResources().getString(R.string.water_revised));
                break;
            case R.id.iv_osmometer_cord:
                LoadingDialog.showScanResultDialog(this,
                    getResources().getString(R.string.osmometer_cord));
                break;
            case R.id.iv_nozzel_height:
                LoadingDialog.showScanResultDialog(this,
                    getResources().getString(R.string.nozzel_height));
                break;
            case R.id.btn_confirm_complete:
                sendOsmometerConfig();
                break;
        }
    }


    private void sendOsmometerConfig() {
        int osAddress = Integer.parseInt(mEtOsmometerAddress.getText().toString().trim());
        if (osAddress > 0 && osAddress < 255) {
            osmometerAddress = String.valueOf(osAddress);
        } else {
            ToastUtil.showSToast("渗压计地址输入有误");
            return;
        }
        depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();

        depthCorrection = mEtWaterRevised.getText().toString().trim();

        osmometerLength = mEtOsmometerCord.getText().toString().trim();

        nozzelHeight = mEtNozzelHeight.getText().toString().trim();
        String note = mEtNote.getText().toString().trim();
        uc.writeString(CommonVariable.OSMOMETER_NOTE,note);
        if (StringUtil.isNullOrEmpty(depthTriggerValue)) {
            ToastUtil.showSToast("水位报警值不能为空");
        } else if (StringUtil.isNullOrEmpty(depthCorrection)) {
            ToastUtil.showSToast("水深修正值不能为空");
        } else if (StringUtil.isNullOrEmpty(osmometerLength)) {
            ToastUtil.showSToast("渗压计绳长不能为空");
        } else if (StringUtil.isNullOrEmpty(nozzelHeight)) {
            ToastUtil.showSToast("管口高程值不能为空");
        }   else {
            List<String> list = new ArrayList<>();
            StringBuilder result1=new StringBuilder();
            StringBuilder result2=new StringBuilder();
            StringBuilder result3=new StringBuilder();
            StringBuilder result4=new StringBuilder();
            StringBuilder result5=new StringBuilder();

            result1.append("##4011\r\n");
            result2.append("##402"+String.valueOf(osmometerAddress)+"\r\n");
            result3.append("##403"+depthTriggerValue+",0"+"\r\n");
            result4.append("##404"+depthCorrection+","+nozzelHeight+"\r\n");
            result5.append("##405"+osmometerLength+"\r\n");

            list.add(String.valueOf(result1));
            list.add(String.valueOf(result2));
            list.add(String.valueOf(result3));
            list.add(String.valueOf(result4));
            list.add(String.valueOf(result5));
            for (int i = 0; i < list.size(); i++) {
                if (DeviceFragment.isConneted) {
                    Log.i(LogTag.INFO_TAG, "==渗压计指令==" + list.get(i));
                    //渗压计
                    Message msg = new Message("osmometer", list.get(i).toString(), true);
                    if (DeviceFragment.mdBluetoothManager != null) {
                        DeviceFragment.mdBluetoothManager.writeMessage(msg);
                    }
                    finish();
                } else {
                    ToastUtil.showSToast("蓝牙未连接");
                }
            }
        }

    }

}