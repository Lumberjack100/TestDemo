package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.internal.LinkedTreeMap;
import com.hjq.toast.ToastUtils;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.model.SensorScanResult;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorBGK4500View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorVWP03View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorYLJView;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorZLJ300tView;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/16 <br/>
 * 描述：     TODO
 */
public class NetDasExternalVibratingWireSensorFragment extends BaseFragment {
    @BindView(R.id.tv_sensor_type)
    TextView mTvSensorType;

    @BindView(R.id.tv_sensor_aisle)
    TextView mTvSensorAisle;

    @BindView(R.id.sensorBGK4500View)
    SensorBGK4500View sensorBGK4500View;

    @BindView(R.id.sensorVWP03View)
    SensorVWP03View sensorVWP03View;

    @BindView(R.id.sensorZLJ300tView)
    SensorZLJ300tView sensorZLJ300tView;

    @BindView(R.id.sensorYLJView)
    SensorYLJView sensorYLJView;

    private List<String> sensorTypeList = Arrays.asList("基康渗压计(BGK-4500)", "葛南渗压计(VWP-03)", "轴力计(ZLJ-300T)", "应力计");
    private List<String> allAisleList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");//所有通道
    private ArrayList<String> usedAisleList = new ArrayList<>();//已占用的通道
    private List<String> unUsedAisleList = new ArrayList<>();//未使用的通道

    private IOTSensorType iotSensorType;//传感器类型
    private String sensorAisle;//传感器通道
    private DasExternalSensorInfo externalSensorInfo;

    public static NetDasExternalVibratingWireSensorFragment newInstance(ArrayList<String> aisleList, IOTSensorType sensorType, DasExternalSensorInfo externalSensorInfo) {
        NetDasExternalVibratingWireSensorFragment fragment = new NetDasExternalVibratingWireSensorFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST, aisleList);
        args.putSerializable(AppContants.Extras.SENSOR_TYPE, sensorType);
        args.putSerializable(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usedAisleList = getArguments().getStringArrayList(AppContants.Extras.SENSOR_ADDRESS_LIST);
            externalSensorInfo = (DasExternalSensorInfo) getArguments().getSerializable(AppContants.Extras.SENSOR_PARAM);
            iotSensorType = (IOTSensorType) getArguments().getSerializable(AppContants.Extras.SENSOR_TYPE);
            sensorAisle = externalSensorInfo == null ? "" : externalSensorInfo.getAddr();
            if (!TextUtils.isEmpty(sensorAisle)) {
                usedAisleList.remove(sensorAisle);
            }
            unUsedAisleList.clear();
            unUsedAisleList.addAll(allAisleList);
            for (String aisle : usedAisleList) {
                for (String ss : allAisleList) {
                    if (Integer.parseInt(aisle) == (Integer.parseInt(ss) - 1)) {
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
            mTvSensorAisle.setText(unUsedAisleList.get(0));
        } else {
            for (int i = 0; i < unUsedAisleList.size(); i++) {
                String aisle = String.valueOf(Integer.parseInt(unUsedAisleList.get(i)) - 1);
                if (aisle.equals(sensorAisle)) {
                    mTvSensorAisle.setText(unUsedAisleList.get(i));
                    break;
                }
            }
        }
//        switch (iotSensorType) {
//            case KANG_PERCOLATE://基康渗压计(BGK-4500)
//                mTvSensorType.setText(sensorTypeList.get(0));
//                sensorBGK4500View.setVisibility(View.VISIBLE);
//                sensorVWP03View.setVisibility(View.GONE);
//                sensorZLJ300tView.setVisibility(View.GONE);
//                sensorYLJView.setVisibility(View.GONE);
//                sensorBGK4500View.initData(externalSensorInfo);
//                break;
//
//            case GUDAN_PERCOLATE://葛南渗压计(VWP-03)
//                mTvSensorType.setText(sensorTypeList.get(1));
//                sensorBGK4500View.setVisibility(View.GONE);
//                sensorVWP03View.setVisibility(View.VISIBLE);
//                sensorZLJ300tView.setVisibility(View.GONE);
//                sensorYLJView.setVisibility(View.GONE);
//                sensorVWP03View.initData(externalSensorInfo);
//                break;
//
//            case JUNXING_ZLJ_300T://轴力计(ZLJ-300T)
//                mTvSensorType.setText(sensorTypeList.get(2));
//                sensorBGK4500View.setVisibility(View.GONE);
//                sensorVWP03View.setVisibility(View.GONE);
//                sensorZLJ300tView.setVisibility(View.VISIBLE);
//                sensorYLJView.setVisibility(View.GONE);
//                sensorZLJ300tView.initData(externalSensorInfo);
//                break;
//
//            case GUDAN_STRESS://应力计
//                mTvSensorType.setText(sensorTypeList.get(3));
//                sensorBGK4500View.setVisibility(View.GONE);
//                sensorVWP03View.setVisibility(View.GONE);
//                sensorZLJ300tView.setVisibility(View.GONE);
//                sensorYLJView.setVisibility(View.VISIBLE);
//                sensorYLJView.initData(externalSensorInfo);
//        }

        if (iotSensorType == IOTSensorType.KANG_PERCOLATE) {
            switchSensorType(sensorTypeList.get(0));

        } else if (iotSensorType == IOTSensorType.GUDAN_PERCOLATE) {
            switchSensorType(sensorTypeList.get(1));

        } else if (iotSensorType == IOTSensorType.JUNXING_ZLJ_300T) {
            switchSensorType(sensorTypeList.get(2));

        } else if (iotSensorType == IOTSensorType.GUDAN_STRESS) {
            switchSensorType(sensorTypeList.get(3));
        }
    }

    private void switchSensorType(String sensorName) {
        mTvSensorType.setText(sensorName);
        switch (iotSensorType) {
            case KANG_PERCOLATE://基康渗压计(BGK-4500)
                sensorBGK4500View.setVisibility(View.VISIBLE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorYLJView.setVisibility(View.GONE);
                sensorBGK4500View.initData(externalSensorInfo);
                break;

            case GUDAN_PERCOLATE://葛南渗压计(VWP-03)
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.VISIBLE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorYLJView.setVisibility(View.GONE);
                sensorVWP03View.initData(externalSensorInfo);
                break;

            case JUNXING_ZLJ_300T://轴力计(ZLJ-300T)
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.VISIBLE);
                sensorYLJView.setVisibility(View.GONE);
                sensorZLJ300tView.initData(externalSensorInfo);
                break;

            case GUDAN_STRESS://应力计
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorYLJView.setVisibility(View.VISIBLE);
                sensorYLJView.initData(externalSensorInfo);
        }
    }

    @OnClick({R.id.sensorTypeLayout, R.id.sensorAisleLayout, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.sensorTypeLayout) {
            showSensorTypeChooseDialog();
        } else if (id == R.id.sensorAisleLayout) {
            showSensorAisleChooseDialog();
        } else if (id == R.id.btn_confirm) {
            processConfirm();
        }
    }

    /**
     * 选择传感器类型
     */
    private void showSensorTypeChooseDialog() {
        int pos = sensorTypeList.indexOf(String.valueOf(mTvSensorType.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", (String[]) sensorTypeList.toArray(),
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                if (text.contains("基康渗压计")) {
                                    iotSensorType = IOTSensorType.KANG_PERCOLATE;
                                } else if (text.contains("葛南渗压计")) {
                                    iotSensorType = IOTSensorType.GUDAN_PERCOLATE;
                                } else if (text.contains("轴力计")) {
                                    iotSensorType = IOTSensorType.JUNXING_ZLJ_300T;
                                } else if (text.contains("应力计")) {
                                    iotSensorType = IOTSensorType.GUDAN_STRESS;
                                }
                                switchSensorType(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 选择传感器通道
     */
    private void showSensorAisleChooseDialog() {
        int pos = unUsedAisleList.indexOf(String.valueOf(mTvSensorAisle.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", unUsedAisleList.toArray(new String[0]),
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvSensorAisle.setText(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private void processConfirm() {
        boolean updateDataSuccess = false;
        if (externalSensorInfo == null)
            externalSensorInfo = new DasExternalSensorInfo();

        switch (iotSensorType) {
            case KANG_PERCOLATE:
                updateDataSuccess = sensorBGK4500View.updateSensorData(externalSensorInfo);
                break;

            case GUDAN_PERCOLATE:
                updateDataSuccess = sensorVWP03View.updateSensorData(externalSensorInfo);
                break;

            case JUNXING_ZLJ_300T:
                updateDataSuccess = sensorZLJ300tView.updateSensorData(externalSensorInfo);
                break;

            case GUDAN_STRESS:
                updateDataSuccess = sensorYLJView.updateSensorData(externalSensorInfo);
                break;
        }
        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        String aisle = mTvSensorAisle.getText().toString();
        sensorAisle = String.valueOf(Integer.parseInt(aisle) - 1);
        externalSensorInfo.setAddr(sensorAisle);
        externalSensorInfo.setType(iotSensorType.toString());

        Intent intent = new Intent();
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        if (requestCode == XPermissionUtils.REQUEST_CODE_SCAN) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Timber.d("扫描结果为：%s", obj.originalValue);
                scanResult(obj.originalValue);
            }
        }
    }

    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            ToastUtils.show("二维码不正确!");
            return;
        }
        SensorScanResult sensorScanResult = GsonFactory.getGson().fromJson(result, SensorScanResult.class);
        if (sensorScanResult == null) {
            ToastUtils.show("二维码不正确!");
            return;
        }
        showSerialNumDialog(sensorScanResult);
    }

    private void showSerialNumDialog(SensorScanResult sensorScanResult) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示：")
                .content("仪器编号：" + sensorScanResult.getSerealNum())
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        LinkedTreeMap<String, String> paramMap = sensorScanResult.getParam();
                        if (paramMap == null) {
                            return;
                        }
                        if (sensorScanResult.getSensortype().equals("50")) {
                            if (!TextUtils.isEmpty(paramMap.get("poly_a"))) {
                                externalSensorInfo.setPoly_a(paramMap.get("poly_a"));
                            }
                            if (!TextUtils.isEmpty(paramMap.get("poly_b"))) {
                                externalSensorInfo.setPoly_b(paramMap.get("poly_b"));
                            }
                            if (!TextUtils.isEmpty(paramMap.get("poly_c"))) {
                                externalSensorInfo.setPoly_c(paramMap.get("poly_c"));
                            }
                            if (!TextUtils.isEmpty(paramMap.get("poly_k"))) {
                                externalSensorInfo.setSens_k(paramMap.get("poly_k"));
                            }
                            if (!TextUtils.isEmpty(paramMap.get("temp_t0"))) {
                                externalSensorInfo.setTemp_t0(paramMap.get("temp_t0"));
                            }
                            iotSensorType = IOTSensorType.KANG_PERCOLATE;
                            switchSensorType(sensorTypeList.get(0));

                        } else if (sensorScanResult.getSensortype().equals("51")) {
                            if (!TextUtils.isEmpty(paramMap.get("sens_k"))) {
                                externalSensorInfo.setSens_k(paramMap.get("sens_k"));
                            }
                            if (!TextUtils.isEmpty(paramMap.get("temp_b"))) {
                                externalSensorInfo.setTemp_b(paramMap.get("temp_b"));
                            }
                            if (!TextUtils.isEmpty(paramMap.get("temp_t0"))) {
                                externalSensorInfo.setTemp_t0(paramMap.get("temp_t0"));
                            }
                            if (!TextUtils.isEmpty(paramMap.get("referval_f"))) {
                                externalSensorInfo.setReferval_f(paramMap.get("referval_f"));
                            }
                            iotSensorType = IOTSensorType.GUDAN_PERCOLATE;
                            switchSensorType(sensorTypeList.get(1));
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
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
