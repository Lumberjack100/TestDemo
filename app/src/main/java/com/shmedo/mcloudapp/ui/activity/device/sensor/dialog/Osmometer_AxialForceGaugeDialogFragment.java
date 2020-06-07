package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
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
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.spinner)
    Spinner spinnerType;

    @BindView(R.id.sensorBGK4500View)
    SensorBGK4500View sensorBGK4500View;

    @BindView(R.id.sensorVWP03View)
    SensorVWP03View sensorVWP03View;

    @BindView(R.id.sensorZLJ300tView)
    SensorZLJ300tView sensorZLJ300tView;

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;


    public static Osmometer_AxialForceGaugeDialogFragment newInstance(CollectorSensorParamsInfoSub infoSub) {
        Osmometer_AxialForceGaugeDialogFragment fragment = new Osmometer_AxialForceGaugeDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, infoSub);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_osmometer__axial_force_gauge_dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectorSensorParamsInfoSub = (CollectorSensorParamsInfoSub) getArguments().getSerializable(ARG_PARAM1);
        }
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
                switch (item) {
                    case "渗压计-BGK":
                        sensorBGK4500View.setVisibility(View.VISIBLE);
                        sensorVWP03View.setVisibility(View.GONE);
                        sensorZLJ300tView.setVisibility(View.GONE);
                        break;

                    case "渗压计-NGN":
                        sensorBGK4500View.setVisibility(View.GONE);
                        sensorVWP03View.setVisibility(View.VISIBLE);
                        sensorZLJ300tView.setVisibility(View.GONE);
                        break;

                    case "轴力计":
                        sensorBGK4500View.setVisibility(View.GONE);
                        sensorVWP03View.setVisibility(View.GONE);
                        sensorZLJ300tView.setVisibility(View.VISIBLE);
                        break;
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
                sensorBGK4500View.bindData(collectorSensorParamsInfoSub);
                break;

            case "51"://葛南渗压计(VWP-03)
                spinnerType.setSelection(1);
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.VISIBLE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorVWP03View.bindData(collectorSensorParamsInfoSub);
                break;

            case "58"://军星轴力计(ZLJ-300T)
                spinnerType.setSelection(2);
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.VISIBLE);
                sensorZLJ300tView.bindData(collectorSensorParamsInfoSub);
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
                updateDataSuccess = sensorBGK4500View.updateData(collectorSensorParamsInfoSub);
                break;

            case "51":
                updateDataSuccess = sensorVWP03View.updateData(collectorSensorParamsInfoSub);
                break;

            case "55":
                updateDataSuccess = sensorZLJ300tView.updateData(collectorSensorParamsInfoSub);
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
    protected void scanResult(String content) {

    }
}
