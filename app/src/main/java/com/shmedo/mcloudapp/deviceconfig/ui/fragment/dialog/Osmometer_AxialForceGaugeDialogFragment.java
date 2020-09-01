package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.enums.SensorType;
import com.shmedo.core.model.SensorGudanPercolateInfo;
import com.shmedo.core.model.SensorKangPercolateInfo;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor.BaseSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorBGK4500View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorVWP03View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorZLJ300tView;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 振弦式传感器配置参数对话框
 * 基康渗压计(BGK-4500)、葛南渗压计(VWP-03)、轴力计(ZLJ-300T)
 */
public class Osmometer_AxialForceGaugeDialogFragment extends BaseDialogFragment {

    @BindView(R.id.spinnerType)
    Spinner spinnerType;

    @BindView(R.id.spinnerWay)
    Spinner spinnerWay;

    @BindView(R.id.sensorBGK4500View)
    SensorBGK4500View sensorBGK4500View;

    @BindView(R.id.sensorVWP03View)
    SensorVWP03View sensorVWP03View;

    @BindView(R.id.sensorZLJ300tView)
    SensorZLJ300tView sensorZLJ300tView;


    private ArrayAdapter<String> adapterWay;
    private List<String> sensorWays = Arrays.asList("00", "01", "02", "03");
    private List<String> wayList = new ArrayList<>();

    private String selectedSensorType = "";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_osmometer__axial_force_gauge_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectorSensorParamsInfo = ((BaseSensorConfigActivity) getActivity()).getCurrentCollectorSensorParamsInfo();
        wayList.clear();
        wayList.addAll(sensorWays);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        initSensorTypeAdapter();
        initSpecifiedSensorView();
        initSensorWayAdapter();
        return rootView;
    }

    private void initSensorTypeAdapter() {
        //传感器类型
        String[] stringArray = getActivity().getResources().getStringArray(R.array.sensor_osmometer);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.sensor_spinner_item, stringArray);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerType.setAdapter(adapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.contains("基康渗压计")) {
                    selectedSensorType = SensorType.KANG_PERCOLATE.toString();
                    sensorBGK4500View.setVisibility(View.VISIBLE);
                    sensorVWP03View.setVisibility(View.GONE);
                    sensorZLJ300tView.setVisibility(View.GONE);
                } else if (item.contains("葛南渗压计")) {
                    selectedSensorType = SensorType.GUDAN_PERCOLATE.toString();
                    sensorBGK4500View.setVisibility(View.GONE);
                    sensorVWP03View.setVisibility(View.VISIBLE);
                    sensorZLJ300tView.setVisibility(View.GONE);
                } else if (item.contains("轴力计")) {
                    selectedSensorType = SensorType.JUNXING_ZLJ_300T.toString();
                    sensorBGK4500View.setVisibility(View.GONE);
                    sensorVWP03View.setVisibility(View.GONE);
                    sensorZLJ300tView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 初始化指定的传感器视图
     */
    private void initSpecifiedSensorView() {
        switch (collectorSensorParamsInfo.getSensorType()) {
            case KANG_PERCOLATE://基康渗压计(BGK-4500)
                spinnerType.setSelection(0);
                sensorBGK4500View.setVisibility(View.VISIBLE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorBGK4500View.bindSensorData(collectorSensorParamsInfo);
                break;

            case GUDAN_PERCOLATE://葛南渗压计(VWP-03)
                spinnerType.setSelection(1);
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.VISIBLE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorVWP03View.bindSensorData(collectorSensorParamsInfo);
                break;

            case JUNXING_ZLJ_300T://轴力计(ZLJ-300T)
                spinnerType.setSelection(2);
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.VISIBLE);
                sensorZLJ300tView.bindSensorData(collectorSensorParamsInfo);
                break;
        }
    }


    private void initSensorWayAdapter() {
        adapterWay = new ArrayAdapter<>(getActivity(), R.layout.sensor_spinner_item, wayList);
        adapterWay.setDropDownViewResource(R.layout.spinner_item);
        spinnerWay.setAdapter(adapterWay);
        spinnerWay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChannelNumber = wayList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String channelNumber = StringUtil.formatStringTwo(collectorSensorParamsInfo.getChannelNumber());
        switch (channelNumber) {
            case "00":
                spinnerWay.setSelection(0);
                break;

            case "01":
                spinnerWay.setSelection(1);
                break;

            case "02":
                spinnerWay.setSelection(2);
                break;

            case "03":
                spinnerWay.setSelection(3);
                break;
        }
    }


    @OnClick({R.id.rl_scan_config, R.id.tv_cancel, R.id.tv_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_scan_config:
                PermissionHelper.requestScanPermissions(Osmometer_AxialForceGaugeDialogFragment.this);
                break;

            case R.id.tv_cancel:
                doNegativeClick(view);
                break;

            case R.id.tv_save:
                doPositiveClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
        boolean updateDataSuccess = false;
        switch (selectedSensorType) {
            case "50":
                updateDataSuccess = sensorBGK4500View.updateSensorData(collectorSensorParamsInfo);
                break;

            case "51":
                updateDataSuccess = sensorVWP03View.updateSensorData(collectorSensorParamsInfo);
                break;

            case "58":
                updateDataSuccess = sensorZLJ300tView.updateSensorData(collectorSensorParamsInfo);
                break;
        }

        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }

        collectorSensorParamsInfo.setChannelNumber(selectedChannelNumber);
        collectorSensorParamsInfo.setSensorType(SensorType.value(selectedSensorType));


        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        if (listener.onPositiveClick(view, collectorSensorParamsInfo)) {
            dismiss();
        }
    }

    private void doNegativeClick(View view) {
        // Do stuff here.
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }


    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            ToastUtils.show("二维码不正确!");
            return;
        }

        if (!result.startsWith("medo") && !result.startsWith("MEDO")) {
            ToastUtils.show("二维码不正确!");
            return;
        }

        String[] localData = result.split(",");
        if (localData.length < 3) {
            ToastUtils.show("二维码不正确!");
            return;
        }

        switch (localData[2]) {
            case "BGK": {
                SensorKangPercolateInfo sensorInfo = new SensorKangPercolateInfo();
                sensorInfo.setPolynomialRatioA(localData[3]);
                sensorInfo.setPolynomialRatioB(localData[4]);
                sensorInfo.setPolynomialRatioC(localData[5]);
                sensorInfo.setTemperatureCoefficientK(localData[6]);
                if (spinnerType.getSelectedItemPosition() != 0) {
                    showSwitchSensorTypeDialog(localData[2], sensorInfo);
                    return;
                }
                spinnerType.setSelection(0);
                sensorBGK4500View.initDataByScan(sensorInfo);
            }
            break;

            case "NGN": {
                SensorGudanPercolateInfo sensorInfo = new SensorGudanPercolateInfo();
                sensorInfo.setSensitivityK(localData[3]);
                sensorInfo.setTemperatureCoefficientB(localData[4]);
                if (spinnerType.getSelectedItemPosition() != 1) {
                    showSwitchSensorTypeDialog(localData[2], sensorInfo);
                    return;
                }
                spinnerType.setSelection(1);
                sensorVWP03View.initDataByScan(sensorInfo);
            }
            break;

            default:
                ToastUtils.show("此设备类型暂时不支持!");
                break;
        }
    }


    private void showSwitchSensorTypeDialog(String type, Object object) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity())
                .title("温馨提示：")
                .content("扫描条码获取的传感器类型与当前不一致，是否切换？")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (type) {
                            case "BGK":
                                spinnerType.setSelection(0);
                                sensorBGK4500View.initDataByScan((SensorKangPercolateInfo) object);
                                break;

                            case "NGN":
                                spinnerType.setSelection(1);
                                sensorVWP03View.initDataByScan((SensorGudanPercolateInfo) object);
                                break;

                            default:
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == XPermissionUtils.REQUEST_CODE_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                    Timber.d("扫描结果为：" + content);
                    scanResult(content);
                }
            }
        }
    }
}
