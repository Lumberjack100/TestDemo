package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40GpsWorkInfoEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40GpsWorkInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  6/18/21 <br/>
 * 描述：    E40 4G模式GPS 工作参数配置页面
 */
public class NetE40GpsWorkParamFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    @BindView(R.id.et_satellite_elevation_angle)
    ClearEditText mEtSatelliteElevationAngle;

    @BindView(R.id.et_observation_range)
    ClearEditText mEtObservationRange;

    @BindView(R.id.tv_data_frequency)
    TextView mTvDataFrequency;

    private int frequencyPos;

    private String satelliteElevationAngle;//
    private String observationRange;//
    private String dataFrequencyOld;//
    private String dataFrequency;//

    private E40GpsWorkInfo gpsWorkInfo;

    public static NetE40GpsWorkParamFragment newInstance(DeviceInfo deviceInfo) {
        NetE40GpsWorkParamFragment fragment = new NetE40GpsWorkParamFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.e40_gps_work_param_fragment;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        queryParamInfo();
    }

    private void setView() {
        mEtSatelliteElevationAngle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtObservationRange.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mTvDataFrequency.setText("0.05s");
        dataFrequency = dataFrequencyOld = "8";
    }

    /**
     * 获取配置参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_GPS_PARAM);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.ll_data_frequency, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_data_frequency) {
            showDataFrequencyDialog();

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    /**
     * 选择数据频率
     */
    private void showDataFrequencyDialog() {
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"0.05s", "0.1s", "0.2s", "1s", "5s", "10s", "15s", "30s"},
                        null, frequencyPos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                frequencyPos = position;
                                mTvDataFrequency.setText(text);
                                if (position == 0) {
                                    dataFrequency = "8";
                                } else if (position == 1) {
                                    dataFrequency = "7";
                                } else if (position == 2) {
                                    dataFrequency = "6";
                                } else if (position == 3) {
                                    dataFrequency = "5";
                                } else if (position == 4) {
                                    dataFrequency = "4";
                                } else if (position == 5) {
                                    dataFrequency = "3";
                                } else if (position == 6) {
                                    dataFrequency = "2";
                                } else if (position == 7) {
                                    dataFrequency = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private boolean checkValueIsValid() {
        satelliteElevationAngle = mEtSatelliteElevationAngle.getText().toString().trim();
        observationRange = mEtObservationRange.getText().toString().trim();

        if (TextUtils.isEmpty(satelliteElevationAngle)) {
            ToastUtils.show("请输入仰角截止角!");
            mEtSatelliteElevationAngle.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(satelliteElevationAngle);
            if (value < 0 || value > 90) {
                ToastUtils.show("请输入正确的仰角截止角!");
                mEtSatelliteElevationAngle.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的仰角截止角!");
            mEtSatelliteElevationAngle.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(observationRange)) {
            ToastUtils.show("请输入观测范围!");
            mEtObservationRange.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(observationRange);
            if (value != 0 && value != 1) {
                ToastUtils.show("请输入正确的观测范围!");
                mEtObservationRange.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的观测范围!");
            mEtObservationRange.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        E40GpsWorkInfoEntity entity = new E40GpsWorkInfoEntity();
        entity.setCutoffangle(satelliteElevationAngle);
        entity.setRange(observationRange);
        entity.setSavefreq(dataFrequency);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_GPS_PARAM, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case E40_MD_GET_GPS_PARAM:
                ToastUtils.show("下发指令失败");
                break;

            case E40_MD_SET_GPS_PARAM:
                ToastUtils.show("下发指令失败");
                break;

            default:
                break;
        }
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case E40_MD_GET_GPS_PARAM: {
                IOTCommandResult<E40GpsWorkInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询GPS工作参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                gpsWorkInfo = commandResult.getResult();
                initParamInfo();
            }
            break;

            case E40_MD_SET_GPS_PARAM: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置GPS工作参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                break;
        }
    }

    private void initParamInfo() {
        if (gpsWorkInfo == null) {
            Timber.e("E40EthernetInfo 为空!");
            gpsWorkInfo = new E40GpsWorkInfo();
            return;
        }
        satelliteElevationAngle = gpsWorkInfo.getCutoffangle().trim();
        observationRange = gpsWorkInfo.getRange().trim();
        dataFrequencyOld = gpsWorkInfo.getSavefreq().trim();
        dataFrequency = gpsWorkInfo.getSavefreq().trim();

        mEtSatelliteElevationAngle.setText(satelliteElevationAngle);
        mEtObservationRange.setText(observationRange);
        if (dataFrequencyOld.equals("8")) {
            frequencyPos = 0;
            mTvDataFrequency.setText("0.05s");
        } else if (dataFrequencyOld.equals("7")) {
            frequencyPos = 1;
            mTvDataFrequency.setText("0.1s");
        } else if (dataFrequencyOld.equals("6")) {
            frequencyPos = 2;
            mTvDataFrequency.setText("0.2s");
        } else if (dataFrequencyOld.equals("5")) {
            frequencyPos = 3;
            mTvDataFrequency.setText("1s");
        } else if (dataFrequencyOld.equals("4")) {
            frequencyPos = 4;
            mTvDataFrequency.setText("5s");
        } else if (dataFrequencyOld.equals("3")) {
            frequencyPos = 5;
            mTvDataFrequency.setText("10s");
        } else if (dataFrequencyOld.equals("2")) {
            frequencyPos = 6;
            mTvDataFrequency.setText("15s");
        } else if (dataFrequencyOld.equals("1")) {
            frequencyPos = 7;
            mTvDataFrequency.setText("30s");
        }
    }

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
        if (gpsWorkInfo != null) {
            gpsWorkInfo.setCutoffangle(satelliteElevationAngle);
            gpsWorkInfo.setRange(observationRange);
            gpsWorkInfo.setSavefreq(dataFrequency);
        }
        dataFrequencyOld = dataFrequency;
    }

    @Override
    public boolean onBackPressed() {
        if (checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValueIsChange() {
        if (dataFrequencyOld != null && dataFrequency != null && !dataFrequencyOld.equals(dataFrequency)) {
            return true;
        }
        if (satelliteElevationAngle != null && !satelliteElevationAngle.equals(mEtSatelliteElevationAngle.getText().toString().trim())) {
            return true;
        }
        if (observationRange != null && !observationRange.equals(mEtObservationRange.getText().toString().trim())) {
            return true;
        }
        return false;
    }

}