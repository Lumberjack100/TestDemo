package com.shmedo.mcloudapp.ui.activity.device.sensor;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.das.common.SensorWireShiftInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import ch.ielse.view.SwitchView;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   DisplacementConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/20 09:45
 * 描述：    拉线位移  参数配置 02
 */
public class SenSorMPSConfigActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.sw_stay1)
    SwitchView mSwStay1;
    @BindView(R.id.tv_stay1)
    TextView mTvStay1;
    @BindView(R.id.sw_stay2)
    SwitchView mSwStay2;
    @BindView(R.id.tv_stay2)
    TextView mTvStay2;
    @BindView(R.id.sw_stay3)
    SwitchView mSwStay3;
    @BindView(R.id.tv_stay3)
    TextView mTvStay3;
    @BindView(R.id.sw_stay4)
    SwitchView mSwStay4;
    @BindView(R.id.tv_stay4)
    TextView mTvStay4;
    @BindView(R.id.sw_stay5)
    SwitchView mSwStay5;
    @BindView(R.id.tv_stay5)
    TextView mTvStay5;
    @BindView(R.id.sw_stay6)
    SwitchView mSwStay6;
    @BindView(R.id.tv_stay6)
    TextView mTvStay6;
    @BindView(R.id.sw_stay7)
    SwitchView mSwStay7;
    @BindView(R.id.tv_stay7)
    TextView mTvStay7;
    @BindView(R.id.sw_stay8)
    SwitchView mSwStay8;
    @BindView(R.id.tv_stay8)
    TextView mTvStay8;
    @BindView(R.id.btn_confirm)
    Button mBtnConfirm;
    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
    private Dialog mDialog;
    //private UserConfig uc;
    private MMKV mmkv;
    private SensorWireShiftInfo sensorWireShiftInfo;

    private List<CollectorSensorParamsInfoSub> mCollectorParamsInfoSubList = new ArrayList<>();

    @Override
    protected int initContentView() {
        return R.layout.activity_sensor_mps_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initData();
    }


    private void initView() {
        mToolbarTitle.setText("拉线位移计配置");
        mmkv = MMKV.defaultMMKV();
    }


    private void initData() {
        mCollectorParamsInfoSubList = DeviceFragment.mCollectorParamsInfoSubList;
    }


    @OnClick({R.id.sw_stay1, R.id.sw_stay2, R.id.sw_stay3, R.id.sw_stay4, R.id.sw_stay5,
            R.id.sw_stay6, R.id.sw_stay7, R.id.sw_stay8, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sw_stay1:
                //checkSwitchColor(mTvStay1, mSwStay1,"stay1");
                break;
            case R.id.sw_stay2:
                //checkSwitchColor(mTvStay2, mSwStay2,"stay2");
                break;
            case R.id.sw_stay3:
                //checkSwitchColor(mTvStay3, mSwStay3,"stay3");
                break;
            case R.id.sw_stay4:
                //checkSwitchColor(mTvStay4, mSwStay4,"stay4");
                break;
            case R.id.sw_stay5:
                //checkSwitchColor(mTvStay5, mSwStay5,"stay5");
                break;
            case R.id.sw_stay6:
                //checkSwitchColor(mTvStay6, mSwStay6,"stay6");
                break;
            case R.id.sw_stay7:
                //checkSwitchColor(mTvStay7, mSwStay7,"stay7");
                break;
            case R.id.sw_stay8:
                //checkSwitchColor(mTvStay8, mSwStay8,"stay8");
                break;
            case R.id.btn_confirm:

                break;
        }
    }


}
