package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorBGK4500View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorVWP03View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorZLJ300tView;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

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

    private SensorType selectedSensorType;//传感器类型
    private int sensorAislePos = 0;//传感器通道选择项索引
    private int sensorTypePos = 0;//传感器类型选择项索引
    private String sensorAisle;//传感器通道
    private ProjectDeviceInfo projectDeviceInfo;
    private DasExternalSensorInfo externalSensorInfo;

    public static NetDasExternalVibratingWireSensorFragment newInstance(ProjectDeviceInfo projectDeviceInfo, ArrayList<String> aisleList, DasExternalSensorInfo externalSensorInfo) {
        NetDasExternalVibratingWireSensorFragment fragment = new NetDasExternalVibratingWireSensorFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        args.putStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST, aisleList);
        args.putSerializable(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
            usedAisleList = getArguments().getStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST);
            externalSensorInfo = (DasExternalSensorInfo) getArguments().getSerializable(AppContants.Extras.SENSOR_PARAM);

            selectedSensorType = SensorType.value(externalSensorInfo.getType());
            sensorAisle = externalSensorInfo.getAddr();
            if (!TextUtils.isEmpty(sensorAisle)) {
                usedAisleList.remove(sensorAisle);
            }

            unUsedAisleList.clear();
            unUsedAisleList.addAll(allAisleList);
            for (String aisle : usedAisleList) {
                for (String ss : allAisleList) {
                    if (aisle.equals(String.valueOf(Integer.parseInt(ss) - 1))) {
                        unUsedAisleList.remove(ss);
                    }
                }
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.das_external_vibrating_wire_sensor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initValue();
    }

    private void initValue() {
        if (TextUtils.isEmpty(sensorAisle)) {
            sensorAisle = String.valueOf(Integer.parseInt(unUsedAisleList.get(0)) - 1);
            sensorAislePos = 0;
            mTvSensorAisle.setText(unUsedAisleList.get(0));
        } else {
            for (int i = 0; i < unUsedAisleList.size(); i++) {
                String aisle = String.valueOf(Integer.parseInt(unUsedAisleList.get(i)) - 1);
                if (aisle.equals(sensorAisle)) {
                    sensorAislePos = i;
                    mTvSensorAisle.setText(unUsedAisleList.get(i));
                    break;
                }
            }
        }
        switch (selectedSensorType) {
            case KANG_PERCOLATE://基康渗压计(BGK-4500)
                sensorTypePos = 0;
                mTvSensorType.setText(sensorTypeList.get(0));
                sensorBGK4500View.setVisibility(View.VISIBLE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorBGK4500View.initData(externalSensorInfo);
                break;

            case GUDAN_PERCOLATE://葛南渗压计(VWP-03)
                sensorTypePos = 1;
                mTvSensorType.setText(sensorTypeList.get(1));
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.VISIBLE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorVWP03View.initData(externalSensorInfo);
                break;
//
//            case JUNXING_ZLJ_300T://轴力计(ZLJ-300T)
//                sensorTypePos = 2;
//                mTvSensorType.setText(sensorTypeList.get(2));
//                sensorBGK4500View.setVisibility(View.GONE);
//                sensorVWP03View.setVisibility(View.GONE);
//                sensorZLJ300tView.setVisibility(View.VISIBLE);
//                sensorZLJ300tView.initData(parcelableData == null ? null : (SensorJunXingZljInfo) parcelableData);
//                break;
        }
    }

    @OnClick({R.id.sensorAisleLayout, R.id.sensorTypeLayout,  R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.sensorAisleLayout) {
            showSensorAisleChooseDialog();
        } else if (id == R.id.sensorTypeLayout) {
            showSensorTypeChooseDialog();
        }  else if (id == R.id.btn_confirm) {
            processConfirm();
        }
    }

    private void showSensorAisleChooseDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", unUsedAisleList.toArray(new String[unUsedAisleList.size()]),
                        null, sensorAislePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                sensorAislePos = position;
                                mTvSensorAisle.setText(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private void showSensorTypeChooseDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", (String[]) sensorTypeList.toArray(),
                        null, sensorTypePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                sensorTypePos = position;
                                mTvSensorType.setText(text);

                                if (text.contains("基康渗压计")) {
                                    selectedSensorType = SensorType.KANG_PERCOLATE;
                                    sensorBGK4500View.setVisibility(View.VISIBLE);
                                    sensorVWP03View.setVisibility(View.GONE);
                                    sensorZLJ300tView.setVisibility(View.GONE);
                                } else if (text.contains("葛南渗压计")) {
                                    selectedSensorType = SensorType.GUDAN_PERCOLATE;
                                    sensorBGK4500View.setVisibility(View.GONE);
                                    sensorVWP03View.setVisibility(View.VISIBLE);
                                    sensorZLJ300tView.setVisibility(View.GONE);
                                } else if (text.contains("轴力计")) {
                                    selectedSensorType = SensorType.JUNXING_ZLJ_300T;
                                    sensorBGK4500View.setVisibility(View.GONE);
                                    sensorVWP03View.setVisibility(View.GONE);
                                    sensorZLJ300tView.setVisibility(View.VISIBLE);
                                }

//                                String sensorName = BlueResultParserUtil.getSensorName(selectedSensorType);
//                                mToolbarTitle.setText(sensorName);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private void processConfirm() {
        boolean updateDataSuccess = false;
        switch (selectedSensorType) {
            case KANG_PERCOLATE:
                updateDataSuccess = sensorBGK4500View.updateSensorData(externalSensorInfo);
                break;

            case GUDAN_PERCOLATE:
                updateDataSuccess = sensorVWP03View.updateSensorData(externalSensorInfo);
                break;
//
//            case JUNXING_ZLJ_300T:
//                parcelableData = new SensorJunXingZljInfo();
//                updateDataSuccess = sensorZLJ300tView.updateSensorData((SensorJunXingZljInfo) parcelableData);
//                break;
        }

        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        externalSensorInfo.setAddr(mTvSensorAisle.getText().toString());
        externalSensorInfo.setType(selectedSensorType.toString());
        Intent intent = new Intent();
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    private void setResult() {
        Intent intent = new Intent();
        mActivity.setResult(Activity.RESULT_CANCELED, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
        return false;
    }
}