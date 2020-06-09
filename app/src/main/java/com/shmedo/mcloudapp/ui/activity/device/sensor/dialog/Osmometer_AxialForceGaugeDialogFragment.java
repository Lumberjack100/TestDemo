package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

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
import com.shmedo.core.model.SensorGudanPercolateInfo;
import com.shmedo.core.model.SensorKangPercolateInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.ui.activity.device.sensor.BaseSensorConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.view.SensorBGK4500View;
import com.shmedo.mcloudapp.ui.activity.device.sensor.view.SensorVWP03View;
import com.shmedo.mcloudapp.ui.activity.device.sensor.view.SensorZLJ300tView;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 基康渗压计(BGK-4500)、葛南渗压计(VWP-03)、军星轴力计(ZLJ-300T)
 */
public class Osmometer_AxialForceGaugeDialogFragment extends BaseDialogFragment {
    @BindView(R.id.spinner)
    Spinner spinnerType;

    @BindView(R.id.sensorBGK4500View)
    SensorBGK4500View sensorBGK4500View;

    @BindView(R.id.sensorVWP03View)
    SensorVWP03View sensorVWP03View;

    @BindView(R.id.sensorZLJ300tView)
    SensorZLJ300tView sensorZLJ300tView;

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;


    @Override
    protected int initContentView() {
        return R.layout.fragment_osmometer__axial_force_gauge_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectorSensorParamsInfoSub = ((BaseSensorConfigActivity) getActivity()).getCurrentCollectorSensorParamsInfoSub();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        return rootView;
    }

    private void initView() {
        //传感器类型
        String[] stringArray = getActivity().getResources().getStringArray(R.array.sensor_osmometer);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.contains("基康渗压计")) {
                    collectorSensorParamsInfoSub.setSensorType("50");
                    sensorBGK4500View.setVisibility(View.VISIBLE);
                    sensorVWP03View.setVisibility(View.GONE);
                    sensorZLJ300tView.setVisibility(View.GONE);
                } else if (item.contains("葛南渗压计")) {
                    collectorSensorParamsInfoSub.setSensorType("51");
                    sensorBGK4500View.setVisibility(View.GONE);
                    sensorVWP03View.setVisibility(View.VISIBLE);
                    sensorZLJ300tView.setVisibility(View.GONE);
                } else if (item.contains("军星轴力计")) {
                    collectorSensorParamsInfoSub.setSensorType("58");
                    sensorBGK4500View.setVisibility(View.GONE);
                    sensorVWP03View.setVisibility(View.GONE);
                    sensorZLJ300tView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case "50"://基康渗压计(BGK-4500)
                spinnerType.setSelection(0);
                sensorBGK4500View.setVisibility(View.VISIBLE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorBGK4500View.bindSensorData(collectorSensorParamsInfoSub);
                break;

            case "51"://葛南渗压计(VWP-03)
                spinnerType.setSelection(1);
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.VISIBLE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorVWP03View.bindSensorData(collectorSensorParamsInfoSub);
                break;

            case "58"://军星轴力计(ZLJ-300T)
                spinnerType.setSelection(2);
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.VISIBLE);
                sensorZLJ300tView.bindSensorData(collectorSensorParamsInfoSub);
                break;
        }
    }


    @OnClick({R.id.rl_scan_config, R.id.tv_cancel, R.id.tv_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_scan_config:
                doScanButtonClick();
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
        String sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case "50":
                updateDataSuccess = sensorBGK4500View.updateSensorData(collectorSensorParamsInfoSub);
                break;

            case "51":
                updateDataSuccess = sensorVWP03View.updateSensorData(collectorSensorParamsInfoSub);
                break;

            case "58":
                updateDataSuccess = sensorZLJ300tView.updateSensorData(collectorSensorParamsInfoSub);
                break;
        }
        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        if (listener.onPositiveClick(view)) {
            dismiss();
        }
    }

    private void doNegativeClick(View view) {
        // Do stuff here.
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }

    @Override
    protected void scanResult(String result) {
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
                sensorBGK4500View.initDataByScan(collectorSensorParamsInfoSub.getSensorAddress(), sensorInfo);
            }
            break;

            case "NGN": {
                SensorGudanPercolateInfo sensorInfo = new SensorGudanPercolateInfo();
                sensorInfo.setSensitivityK(localData[3]);
                sensorInfo.setTemperatureCoefficientB(localData[4]);
                if (spinnerType.getSelectedItemPosition() != 0) {
                    showSwitchSensorTypeDialog(localData[2], sensorInfo);
                    return;
                }
                spinnerType.setSelection(1);
                sensorVWP03View.initDataByScan(collectorSensorParamsInfoSub.getSensorAddress(), sensorInfo);
            }
            break;

            default:
                ToastUtils.show("此设备类型暂时不支持!");
                break;
        }
    }


    public void showSwitchSensorTypeDialog(String type, Object object) {
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
                                sensorBGK4500View.initDataByScan(collectorSensorParamsInfoSub.getSensorAddress(), (SensorKangPercolateInfo) object);
                                break;

                            case "NGN":
                                spinnerType.setSelection(1);
                                sensorVWP03View.initDataByScan(collectorSensorParamsInfoSub.getSensorAddress(), (SensorGudanPercolateInfo) object);
                                break;

                            default:
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
