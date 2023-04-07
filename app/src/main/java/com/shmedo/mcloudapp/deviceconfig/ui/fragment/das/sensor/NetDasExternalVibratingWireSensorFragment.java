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
import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.model.SensorScanResult;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorBGK4500View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorVWP03View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorYLJView;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorZLJ300tView;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
    private static final String MCU_PREFIX = "MCU_";
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

    private List<String> sensorTypeList = Arrays.asList(IOTSensorType.KANG_PERCOLATE.getDescription(), IOTSensorType.GUDAN_PERCOLATE.getDescription(),
            IOTSensorType.JUNXING_ZLJ_300T.getDescription(), IOTSensorType.GUDAN_STRESS.getDescription(),
            MCU_PREFIX + IOTSensorType.VW08.getDescription(), MCU_PREFIX + IOTSensorType.WEIR.getDescription(), MCU_PREFIX + IOTSensorType.WATER_LEVEL_GAUGE.getDescription());

    private List<String> allAisleList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16");//所有通道
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        switchSensorType();
    }

    private void switchSensorType() {
        mTvSensorType.setText(iotSensorType.getDescription());
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
                break;

            case VW08://MCU 振弦传感器
            case WEIR://MCU 量水堰计
            case WATER_LEVEL_GAUGE://MCU 水位(液位)计
                mTvSensorType.setText(String.format("%s%s", MCU_PREFIX, iotSensorType.getDescription()));
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorYLJView.setVisibility(View.GONE);
                break;
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
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", sensorTypeList.toArray(new String[0]),
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                if (text.contains(IOTSensorType.KANG_PERCOLATE.getDescription())) {
                                    iotSensorType = IOTSensorType.KANG_PERCOLATE;
                                } else if (text.contains(IOTSensorType.GUDAN_PERCOLATE.getDescription())) {
                                    iotSensorType = IOTSensorType.GUDAN_PERCOLATE;
                                } else if (text.contains(IOTSensorType.JUNXING_ZLJ_300T.getDescription())) {
                                    iotSensorType = IOTSensorType.JUNXING_ZLJ_300T;
                                } else if (text.contains(IOTSensorType.GUDAN_STRESS.getDescription())) {
                                    iotSensorType = IOTSensorType.GUDAN_STRESS;
                                } else if (text.contains(IOTSensorType.VW08.getDescription())) {
                                    iotSensorType = IOTSensorType.VW08;
                                } else if (text.contains(IOTSensorType.WEIR.getDescription())) {
                                    iotSensorType = IOTSensorType.WEIR;
                                }
                                else if (text.contains(IOTSensorType.WATER_LEVEL_GAUGE.getDescription())) {
                                    iotSensorType = IOTSensorType.WATER_LEVEL_GAUGE;
                                }
                                switchSensorType();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择传感器通道
     */
    private void showSensorAisleChooseDialog() {
        int pos = unUsedAisleList.indexOf(String.valueOf(mTvSensorAisle.getText()));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", unUsedAisleList.toArray(new String[0]),
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvSensorAisle.setText(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private void processConfirm() {
        boolean updateDataSuccess = true;
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

            case VW08://MCU 振弦传感器
                externalSensorInfo.setThreshold("NullKey");
                externalSensorInfo.setCorrval("NullKey");
                break;

            case WEIR://MCU 量水堰计
                externalSensorInfo.setThreshold("NullKey");
                externalSensorInfo.setCorrval("NullKey");
                externalSensorInfo.setLsycsds("NullKey");
                externalSensorInfo.setLsyysst("NullKey");
                break;

            case WATER_LEVEL_GAUGE://MCU 振弦传感器
                externalSensorInfo.setThreshold("NullKey");
                externalSensorInfo.setCorrval("NullKey");
                break;
        }
        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        String aisle = mTvSensorAisle.getText().toString();
        sensorAisle = String.valueOf(Integer.parseInt(aisle) - 1);
        externalSensorInfo.setAddr(sensorAisle);
        externalSensorInfo.setType(iotSensorType.getCode());

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
        if (requestCode == PermissionHelper.REQUEST_CODE_SCAN) {
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
        SensorScanResult sensorScanResult = GsonUtils.fromJson(result, SensorScanResult.class);
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
                        LinkedHashMap<String, String> paramMap = sensorScanResult.getParam();
                        if (paramMap == null) {
                            return;
                        }
                        if (externalSensorInfo == null)
                            externalSensorInfo = new DasExternalSensorInfo();

                        if ("50".equals(sensorScanResult.getSensortype())) {
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
                            switchSensorType();

                        } else if ("51".equals(sensorScanResult.getSensortype())) {
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
                            switchSensorType();
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
