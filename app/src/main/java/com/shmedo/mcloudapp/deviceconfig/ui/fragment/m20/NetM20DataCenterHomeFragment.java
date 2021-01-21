package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseNetIotCommunicateFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/21/21 <br/>
 * 描述：    M20网络模式 数据中心主页面
 */
public class NetM20DataCenterHomeFragment extends BaseNetIotCommunicateFragment {
    public static final String LEVEL_INITIAL = "com.shmedo.mcloudapp.LEVEL_INITIAL";

    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    @BindView(R.id.tv_data_center_three)
    TextView mTvDataCenterThree;

    @BindView(R.id.tv_data_center_four)
    TextView mTvDataCenterFour;

    @BindView(R.id.btn_confirm)
    Button mBtnComplete;

    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;
    private boolean isLevelInit = false;


    public static NetM20DataCenterHomeFragment newInstance(int configMethod, boolean isLevelInit) {
        NetM20DataCenterHomeFragment fragment = new NetM20DataCenterHomeFragment();
        Bundle args = new Bundle();
        args.putInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        args.putBoolean(LEVEL_INITIAL, isLevelInit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            configMethod = getArguments().getInt(AppContants.Extras.DATA_CENTER_CONFIG_METHOD);
            isLevelInit = getArguments().getBoolean(LEVEL_INITIAL, false);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_m20_data_center_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isLevelInit) {
            mBtnComplete.setVisibility(View.VISIBLE);
        } else {
            mBtnComplete.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.dataCenterFourLayout, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.dataCenterOneLayout) {
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.NET_PLATFORM_CONNECT, configMethod, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.NET_PLATFORM_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());

        } else if (id == R.id.dataCenterThreeLayout) {
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.NET_PLATFORM_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterThree.getText().toString());

        } else if (id == R.id.dataCenterFourLayout) {
            DataCenterConfigActivity.startActivity(mActivity, AppContants.DeviceType.M20, AppContants.CommunicationWay.NET_PLATFORM_CONNECT, configMethod, ServerNumber.NUMBER_TWO, mTvDataCenterFour.getText().toString());

        } else if (id == R.id.btn_confirm) {
            mActivity.finish();
        }
    }

}