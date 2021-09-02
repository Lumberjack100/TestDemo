package com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.enums.SensorType;
import com.shmedo.configlibrary.ble.model.SensorGudanPercolateInfo;
import com.shmedo.configlibrary.ble.model.SensorJunXingZljInfo;
import com.shmedo.configlibrary.ble.model.SensorKangPercolateInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorBGK4500View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorVWP03View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.SensorZLJ300tView;
import com.shmedo.mcloudapp.util.BlueResultParserUtil;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/14 <br/>
 * 描述：     Das振弦式传感器参数配置页面
 * @deprecated 后面将用物联网指令模式取代
 */
public class DasExternalVibratingWireSensorActivity extends BaseActivity {
    private static final String SENSOR_ITEM_LIST = "sensor_item_list";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_action)
    ImageView mIvRightIcon;

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
    private Parcelable parcelableData;
    private String sensorAisle;//传感器通道
    private int sensorAislePos = 0;//传感器通道选择项索引
    private int sensorTypePos = 0;//传感器类型选择项索引


    public static void startActivityForResultByFragment(Fragment context, int requestCode, ArrayList<String> addressList, String sensorAddress, SensorType sensorType, Parcelable parcelable) {
        Intent intent = new Intent(context.getActivity(), DasExternalVibratingWireSensorActivity.class);
        intent.putStringArrayListExtra(SENSOR_ITEM_LIST, addressList);
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, sensorAddress);
        intent.putExtra(AppContants.Extras.SENSOR_TYPE, sensorType);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, parcelable);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_das_external_vibrating_wire_sensor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("振弦式传感器配置");
        mIvRightIcon.setVisibility(View.VISIBLE);
        mIvRightIcon.setImageResource(R.drawable.ic_scan_device_code);
        parseIntent();
        initView();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(SENSOR_ITEM_LIST)) {
            usedAisleList = intent.getStringArrayListExtra(SENSOR_ITEM_LIST);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_ADDRESS)) {
            sensorAisle = intent.getStringExtra(AppContants.Extras.SENSOR_ADDRESS);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_TYPE)) {
            selectedSensorType = (SensorType) intent.getSerializableExtra(AppContants.Extras.SENSOR_TYPE);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_PARAM)) {
            parcelableData = intent.getParcelableExtra(AppContants.Extras.SENSOR_PARAM);
        }
        if (!TextUtils.isEmpty(sensorAisle)) {
            usedAisleList.remove(sensorAisle);
        }
        unUsedAisleList.clear();
        unUsedAisleList.addAll(allAisleList);
        for (String aisle : usedAisleList) {
            for (String ss : allAisleList) {
                if (aisle.equals(StringUtil.formatStringTwo(String.valueOf(Integer.parseInt(ss) - 1)))) {
                    unUsedAisleList.remove(ss);
                }
            }
        }
        String sensorName = BlueResultParserUtil.getSensorName(selectedSensorType);
        mToolbarTitle.setText(sensorName);
    }

    private void initView() {
        if (TextUtils.isEmpty(sensorAisle)) {
            sensorAisle = StringUtil.formatStringTwo(String.valueOf(Integer.parseInt(unUsedAisleList.get(0)) - 1));
            sensorAislePos = 0;
            mTvSensorAisle.setText(unUsedAisleList.get(0));
        } else {
            for (int i = 0; i < unUsedAisleList.size(); i++) {
                String aisle = StringUtil.formatStringTwo(String.valueOf(Integer.parseInt(unUsedAisleList.get(i)) - 1));
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
                sensorBGK4500View.initData(parcelableData == null ? null : (SensorKangPercolateInfo) parcelableData);
                break;

            case GUDAN_PERCOLATE://葛南渗压计(VWP-03)
                sensorTypePos = 1;
                mTvSensorType.setText(sensorTypeList.get(1));
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.VISIBLE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorVWP03View.initData(parcelableData == null ? null : (SensorGudanPercolateInfo) parcelableData);
                break;

            case JUNXING_ZLJ_300T://轴力计(ZLJ-300T)
                sensorTypePos = 2;
                mTvSensorType.setText(sensorTypeList.get(2));
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.VISIBLE);
                sensorZLJ300tView.initData(parcelableData == null ? null : (SensorJunXingZljInfo) parcelableData);
                break;
        }
    }

    @OnClick({R.id.sensorAisleLayout, R.id.sensorTypeLayout, R.id.iv_action, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.sensorAisleLayout) {
            showSensorAisleChooseDialog();
        } else if (id == R.id.sensorTypeLayout) {
            showSensorTypeChooseDialog();
        } else if (id == R.id.iv_action) {
            PermissionHelper.requestScanPermissions(DasExternalVibratingWireSensorActivity.this);
        } else if (id == R.id.btn_confirm) {
            processSave();
        }
    }

    private void showSensorAisleChooseDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(this)
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
        new XPopup.Builder(this)
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

                                String sensorName = BlueResultParserUtil.getSensorName(selectedSensorType);
                                mToolbarTitle.setText(sensorName);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }


    private void processSave() {
        if (!checkValue()) {
            Timber.w("传感器参数存在错误!");
            return;
        }
        boolean updateDataSuccess = false;
        switch (selectedSensorType) {
            case KANG_PERCOLATE:
                parcelableData = new SensorKangPercolateInfo();
                updateDataSuccess = sensorBGK4500View.updateSensorData((SensorKangPercolateInfo) parcelableData);
                break;

            case GUDAN_PERCOLATE:
                parcelableData = new SensorGudanPercolateInfo();
                updateDataSuccess = sensorVWP03View.updateSensorData((SensorGudanPercolateInfo) parcelableData);
                break;

            case JUNXING_ZLJ_300T:
                parcelableData = new SensorJunXingZljInfo();
                updateDataSuccess = sensorZLJ300tView.updateSensorData((SensorJunXingZljInfo) parcelableData);
                break;
        }
        if (!updateDataSuccess) {
            Timber.w("传感器参数存在错误!");
            return;
        }

        Intent intent = getIntent();
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, sensorAisle);
        intent.putExtra(AppContants.Extras.SENSOR_TYPE, selectedSensorType);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, parcelableData);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean checkValue() {
        String aisle = mTvSensorAisle.getText().toString();
        sensorAisle = StringUtil.formatStringTwo(String.valueOf(Integer.parseInt(aisle) - 1));

        int num = 0;
        for (String ss : usedAisleList) {
            if (ss.equals(sensorAisle)) {
                num++;
            }
        }
        if (num >= 1) {
            ToastUtils.show("传感器通道不能重复!");
            return false;
        }
        return true;
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
                if (sensorTypePos != 0) {
                    showSwitchSensorTypeDialog(localData[2], sensorInfo);
                    return;
                }
                sensorBGK4500View.initDataByScan(sensorInfo);
            }
            break;

            case "NGN": {
                SensorGudanPercolateInfo sensorInfo = new SensorGudanPercolateInfo();
                sensorInfo.setSensitivityK(localData[3]);
                sensorInfo.setTemperatureCoefficientB(localData[4]);
                if (sensorTypePos != 1) {
                    showSwitchSensorTypeDialog(localData[2], sensorInfo);
                    return;
                }
                sensorVWP03View.initDataByScan(sensorInfo);
            }
            break;

            default:
                ToastUtils.show("此设备类型暂时不支持!");
                break;
        }
    }

    private void showSwitchSensorTypeDialog(String type, Object object) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("温馨提示：")
                .content("扫描条码获取的传感器类型与当前不一致，是否切换？")
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
                        switch (type) {
                            case "BGK":
                                sensorTypePos = 0;
                                mTvSensorType.setText(sensorTypeList.get(0));
                                selectedSensorType = SensorType.KANG_PERCOLATE;
                                sensorBGK4500View.setVisibility(View.VISIBLE);
                                sensorVWP03View.setVisibility(View.GONE);
                                sensorZLJ300tView.setVisibility(View.GONE);
                                sensorBGK4500View.initDataByScan((SensorKangPercolateInfo) object);
                                break;

                            case "NGN":
                                sensorTypePos = 1;
                                mTvSensorType.setText(sensorTypeList.get(1));
                                selectedSensorType = SensorType.GUDAN_PERCOLATE;
                                sensorBGK4500View.setVisibility(View.GONE);
                                sensorVWP03View.setVisibility(View.VISIBLE);
                                sensorZLJ300tView.setVisibility(View.GONE);
                                sensorVWP03View.initDataByScan((SensorGudanPercolateInfo) object);
                                break;

                            default:
                                break;
                        }
                        String sensorName = BlueResultParserUtil.getSensorName(selectedSensorType);
                        mToolbarTitle.setText(sensorName);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
