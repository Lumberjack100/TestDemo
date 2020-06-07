package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.ui.activity.device.sensor.view.SensorBGK4500View;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends BaseDialogFragment {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.spinner)
    Spinner spinnerType;

    @BindView(R.id.sensorBGK4500View)
    SensorBGK4500View sensorBGK4500View;

    private CollectorSensorParamsInfoSub collectorSensorParamsInfoSub;


    public static TestFragment newInstance(CollectorSensorParamsInfoSub infoSub) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, infoSub);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_test;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectorSensorParamsInfoSub = (CollectorSensorParamsInfoSub) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        setValue();
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
//                        ngnOsmometerConfigLayout.setVisibility(View.GONE);
//                        axialforcegaugeConfigLayout.setVisibility(View.GONE);
                        break;

                    case "渗压计-NGN":
                        sensorBGK4500View.setVisibility(View.GONE);
//                        ngnOsmometerConfigLayout.setVisibility(View.VISIBLE);
//                        axialforcegaugeConfigLayout.setVisibility(View.GONE);
                        break;

                    case "轴力计":
                        sensorBGK4500View.setVisibility(View.GONE);
//                        ngnOsmometerConfigLayout.setVisibility(View.GONE);
//                        axialforcegaugeConfigLayout.setVisibility(View.VISIBLE);
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
                sensorBGK4500View.setVisibility(View.VISIBLE);
                spinnerType.setSelection(0);
                break;

            case "51"://葛南渗压计(VWP-03)
                sensorBGK4500View.setVisibility(View.GONE);
                break;

            case "58"://军星轴力计(ZLJ-300T)
                sensorBGK4500View.setVisibility(View.GONE);
                break;
        }
    }

    private void setValue() {
        String sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case "50":
                sensorBGK4500View.bindData(collectorSensorParamsInfoSub);
                break;

            case "51":

                break;

            case "55":
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
        String sensorType = collectorSensorParamsInfoSub.getSensorType();
        switch (sensorType) {
            case "50":
                sensorBGK4500View.updateData(collectorSensorParamsInfoSub);
                break;

            case "51":

                break;

            case "55":
                break;
        }
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        if (listener.onPositiveClick(view)) {
            dismiss();
        }
    }

    private void doNegativeClick(View view) {
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }



    @Override
    protected void scanResult(String content) {

    }
}
