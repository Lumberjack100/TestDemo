package com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
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
import com.shmedo.mcloudapp.common.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.NewSensorBGK4500View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.NewSensorVWP03View;
import com.shmedo.mcloudapp.deviceconfig.view.sensor.NewSensorZLJ300tView;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/10/14 <br/>
 * 描述：     振弦式传感器参数配置页面
 */
public class ExternalVibratingWireSensorActivity extends BaseActivity {
    private static final String SENSOR_ITEM_LIST = "sensor_item_list";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.right_icon)
    ImageView mIvRightIcon;

    @BindView(R.id.tv_sensor_aisle)
    TextView mTvSensorAisle;

    @BindView(R.id.tv_sensor_type)
    TextView mTvSensorType;

    @BindView(R.id.sensorBGK4500View)
    NewSensorBGK4500View sensorBGK4500View;

    @BindView(R.id.sensorVWP03View)
    NewSensorVWP03View sensorVWP03View;

    @BindView(R.id.sensorZLJ300tView)
    NewSensorZLJ300tView sensorZLJ300tView;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private List<String> sensorAisleList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
    private List<String> sensorTypeList = Arrays.asList("基康渗压计(BGK-4500)", "葛南渗压计(VWP-03)", "轴力计(ZLJ-300T)");

    private SensorType selectedSensorType;//传感器类型
    private Parcelable parcelableData;
    private ArrayList<String> usedAisleList = new ArrayList<>();
    private String sensorAisle;//传感器通道
    private int sensorAislePos = 0;//传感器通道选择项索引
    private int sensorTypePos = 0;//传感器类型选择项索引


    public static void startActivityForResultByFragment(Fragment context, int requestCode, ArrayList<String> addressList, String sensorAddress, SensorType sensorType, Parcelable parcelable) {
        Intent intent = new Intent(context.getActivity(), ExternalVibratingWireSensorActivity.class);
        intent.putStringArrayListExtra(SENSOR_ITEM_LIST, addressList);
        intent.putExtra(AppContants.Extras.SENSOR_ADDRESS, sensorAddress);
        intent.putExtra(AppContants.Extras.SENSOR_TYPE, sensorType);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, parcelable);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_external_vibrating_wire_sensor;
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

        String sensorName = BlueResultParserUtil.getSensorName(selectedSensorType);
        mToolbarTitle.setText(sensorName);
    }

    private void initView() {
        if (TextUtils.isEmpty(sensorAisle)) {
            sensorAisle = "00";
        }
        switch (sensorAisle) {
            case "00":
                sensorAislePos = 0;
                mTvSensorAisle.setText(sensorAisleList.get(0));
                break;

            case "01":
                sensorAislePos = 1;
                mTvSensorAisle.setText(sensorAisleList.get(1));
                break;

            case "02":
                sensorAislePos = 2;
                mTvSensorAisle.setText(sensorAisleList.get(2));
                break;

            case "03":
                sensorAislePos = 3;
                mTvSensorAisle.setText(sensorAisleList.get(3));
                break;

            case "04":
                sensorAislePos = 4;
                mTvSensorAisle.setText(sensorAisleList.get(4));
                break;

            case "05":
                sensorAislePos = 5;
                mTvSensorAisle.setText(sensorAisleList.get(5));
                break;

            case "06":
                sensorAislePos = 6;
                mTvSensorAisle.setText(sensorAisleList.get(6));
                break;

            case "07":
                sensorAislePos = 7;
                mTvSensorAisle.setText(sensorAisleList.get(7));
                break;
        }

        switch (selectedSensorType) {
            case KANG_PERCOLATE://基康渗压计(BGK-4500)
                sensorTypePos = 0;
                mTvSensorType.setText(sensorTypeList.get(0));
                sensorBGK4500View.setVisibility(View.VISIBLE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorBGK4500View.bindSensorData(parcelableData == null ? null : (SensorKangPercolateInfo) parcelableData);
                break;

            case GUDAN_PERCOLATE://葛南渗压计(VWP-03)
                sensorTypePos = 1;
                mTvSensorType.setText(sensorTypeList.get(1));
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.VISIBLE);
                sensorZLJ300tView.setVisibility(View.GONE);
                sensorVWP03View.bindSensorData(parcelableData == null ? null : (SensorGudanPercolateInfo) parcelableData);
                break;

            case JUNXING_ZLJ_300T://轴力计(ZLJ-300T)
                sensorTypePos = 2;
                mTvSensorType.setText(sensorTypeList.get(2));
                sensorBGK4500View.setVisibility(View.GONE);
                sensorVWP03View.setVisibility(View.GONE);
                sensorZLJ300tView.setVisibility(View.VISIBLE);
                sensorZLJ300tView.bindSensorData(parcelableData == null ? null : (SensorJunXingZljInfo) parcelableData);
                break;
        }
    }

    @OnClick({R.id.sensorAisleLayout, R.id.sensorTypeLayout, R.id.right_icon, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sensorAisleLayout:
                showSensorAisleChooseDialog();
                break;

            case R.id.sensorTypeLayout:
                showSensorTypeChooseDialog();
                break;

            case R.id.right_icon:
                PermissionHelper.requestScanPermissions(ExternalVibratingWireSensorActivity.this);
                break;

            case R.id.btn_confirm:
                processSave();
                break;

        }
    }

    private void showSensorAisleChooseDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(this)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", (String[]) sensorAisleList.toArray(),
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
                if (parcelableData == null) {
                    parcelableData = new SensorKangPercolateInfo();
                }
                updateDataSuccess = sensorBGK4500View.updateSensorData((SensorKangPercolateInfo) parcelableData);
                break;

            case GUDAN_PERCOLATE:
                if (parcelableData == null) {
                    parcelableData = new SensorGudanPercolateInfo();
                }
                updateDataSuccess = sensorVWP03View.updateSensorData((SensorGudanPercolateInfo) parcelableData);
                break;

            case JUNXING_ZLJ_300T:
                if (parcelableData == null) {
                    parcelableData = new SensorJunXingZljInfo();
                }
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

        switch (requestCode) {
            case XPermissionUtils.REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                        Timber.d("扫描结果为：%s", content);
                        scanResult(content);
                    }
                }
                break;
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
