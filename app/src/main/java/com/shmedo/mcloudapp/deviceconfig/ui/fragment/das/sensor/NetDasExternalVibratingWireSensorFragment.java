package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorBGK4500View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorVWP03View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorZLJ300tView;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class NetDasExternalVibratingWireSensorFragment extends BaseFragment {
    private static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    @BindView(R.id.tv_sensor_aisle)
    TextView mTvSensorAisle;

    @BindView(R.id.tv_sensor_type)
    TextView mTvSensorType;

    @BindView(R.id.sensorBGK4500View)
    SensorBGK4500View sensorBGK4500View;

    @BindView(R.id.sensorVWP03View)
    SensorVWP03View sensorVWP03View;

    @BindView(R.id.sensorZLJ300tView)
    SensorZLJ300tView sensorZLJ300tView;

    private List<String> sensorTypeList = Arrays.asList("基康渗压计(BGK-4500)", "葛南渗压计(VWP-03)", "轴力计(ZLJ-300T)");
    private List<String> allAisleList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");//所有通道
    private ArrayList<String> usedAisleList = new ArrayList<>();//已占用的通道
    private List<String> unUsedAisleList = new ArrayList<>();//未使用的通道


    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private SensorType sensorType;//传感器类型
    private ArrayList<String> addressList = new ArrayList<>();
    private String sensorAisle;//传感器通道
    private int sensorAislePos = 0;//传感器通道选择项索引
    private int sensorTypePos = 0;//传感器类型选择项索引
    private ProjectDeviceInfo projectDeviceInfo;
    private DasExternalSensorInfo externalSensorInfo;

    public static NetDasExternalVibratingWireSensorFragment newInstance(ProjectDeviceInfo projectDeviceInfo, ArrayList<String> addressList, DasExternalSensorInfo externalSensorInfo) {
        NetDasExternalVibratingWireSensorFragment fragment = new NetDasExternalVibratingWireSensorFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        args.putStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST, addressList);
        args.putSerializable(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
            addressList = getArguments().getStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST);
            externalSensorInfo = (DasExternalSensorInfo) getArguments().getSerializable(AppContants.Extras.SENSOR_PARAM);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.das_external_vibrating_wire_sensor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sensorType = SensorType.value(externalSensorInfo.getType());

    }

}