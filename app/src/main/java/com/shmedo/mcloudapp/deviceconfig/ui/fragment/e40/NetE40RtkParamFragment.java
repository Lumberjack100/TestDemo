package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40BasePositionEntity;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40RTKModeEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40BasePositionInfo;
import com.shmedo.configlibrary.iot.model.e40.E40RTKModeInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  7/2/21 <br/>
 * 描述：    E50 4G模式RTK参数配置页面
 */
public class NetE40RtkParamFragment extends BaseNetIotCommunicateFragment {

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    @BindView(R.id.tv_rtk_mode)
    TextView mTvRTKMode;

    @BindView(R.id.ll_base_station_params)
    View baseStationParamsView;

    @BindView(R.id.tv_base_mode)
    TextView mTvBaseMode;

    @BindView(R.id.et_longitude)
    ClearEditText mEtLongitude;

    @BindView(R.id.et_latitude)
    ClearEditText mEtLatitude;

    @BindView(R.id.et_elevation)
    ClearEditText mEtElevation;

    private int rtkModePos;
    private int baseModePos;

    private String rtkModeOld;//
    private String rtkMode;//
    private String baseModeOld;//
    private String baseMode;//
    private String longitude;//
    private String latitude;//
    private String elevation;//

    private E40RTKModeInfo rtkModeInfo;
    private E40BasePositionInfo basePositionInfo;

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static NetE40RtkParamFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40RtkParamFragment fragment = new NetE40RtkParamFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.e40_rtk_param_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryRtkMode();
    }

    private void setView() {
        mEtLongitude.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtLatitude.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtElevation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});

        rtkModePos = 1;
        rtkModeOld = rtkMode = "1";
        mTvBaseMode.setText("移动站");

        baseModePos = 2;
        baseModeOld = baseMode = "3";
        mTvBaseMode.setText("手动");
    }

    /**
     * 获取RTK 模式信息
     */
    private void queryRtkMode() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_RTK);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取基站位置信息
     */
    private void queryBasePositionInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_BASE_POSITION);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.ll_rtk_mode, R.id.ll_base_mode, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_rtk_mode) {
            showRTKModeDialog();
        }
        if (id == R.id.ll_base_mode) {
            showBaseModeDialog();

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            setBasePositionInfo();
        }
    }

    /**
     * 选择RTK 模式
     */
    private void showRTKModeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"基站", "移动站"},
                        null, rtkModePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                rtkModePos = position;
                                rtkMode = String.valueOf(position);
                                mTvRTKMode.setText(text);
                                if (text.equals("移动站")) {
                                    baseStationParamsView.setVisibility(View.GONE);
                                } else {
                                    baseStationParamsView.setVisibility(View.VISIBLE);
                                }
                                setRTKMode();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择基准点模式
     */
    private void showBaseModeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"自动", "首次自动", "手动"},
                        null, baseModePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                baseModePos = position;
                                if (position == 0) {
                                    baseMode = "1";
                                    setBasePositionParamState(false);
                                } else if (position == 1) {
                                    baseMode = "2";
                                    setBasePositionParamState(false);
                                } else if (position == 2) {
                                    baseMode = "3";
                                    setBasePositionParamState(true);
                                }
                                mTvBaseMode.setText(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private boolean checkValueIsValid() {
        longitude = mEtLongitude.getText().toString().trim();
        latitude = mEtLatitude.getText().toString().trim();
        elevation = mEtElevation.getText().toString().trim();

        if (rtkMode.equals("0") && baseMode.equals("3")) {
            if (TextUtils.isEmpty(longitude)) {
                ToastUtils.show("请输入经度!");
                mEtLongitude.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(latitude)) {
                ToastUtils.show("请输入纬度!");
                mEtLatitude.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(elevation)) {
                ToastUtils.show("请输入高程!");
                mEtElevation.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void setRTKMode() {
        E40RTKModeEntity entity = new E40RTKModeEntity();
        entity.setMode(rtkMode);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_RTK, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    private void setBasePositionInfo() {
        E40BasePositionEntity entity = new E40BasePositionEntity();
        entity.setMode(baseMode);
        entity.setLon(TextUtils.isEmpty(longitude) ? "" : longitude);
        entity.setLat(TextUtils.isEmpty(latitude) ? "" : latitude);
        entity.setAlt(TextUtils.isEmpty(elevation) ? "" : elevation);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_BASE_POSITION, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
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
        ToastUtils.show("下发指令失败");
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
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case E40_MD_GET_RTK: {
                IOTCommandResult<E40RTKModeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询RTK模式出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                rtkModeInfo = commandResult.getResult();
                initRtkMode();
            }
            break;

            case E40_MD_SET_RTK: {
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置RTK模式出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            break;
            case E40_MD_GET_BASE_POSITION: {
                dismissWaitDialog();
                IOTCommandResult<E40BasePositionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询基站位置信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                basePositionInfo = commandResult.getResult();
                initBasePositionInfo();
            }
            break;

            case E40_MD_SET_BASE_POSITION: {
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置基站位置信息出错!", cmdResult.getReason());
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

    private void initRtkMode() {
        if (rtkModeInfo == null) {
            dismissWaitDialog();
            Timber.e("E40RTKModeInfo 为空!");
            rtkModeInfo = new E40RTKModeInfo();
            return;
        }
        rtkModeOld = rtkModeInfo.getMode().trim();
        rtkMode = rtkModeInfo.getMode().trim();
        if (rtkModeOld.equals("0")) {
            rtkModePos = 0;
            mTvRTKMode.setText("基站");
            baseStationParamsView.setVisibility(View.VISIBLE);

            //查询基站位置信息
            queryBasePositionInfo();
        } else {
            dismissWaitDialog();
            rtkModePos = 1;
            mTvRTKMode.setText("移动站");
            baseStationParamsView.setVisibility(View.GONE);
        }
    }

    private void initBasePositionInfo() {
        if (basePositionInfo == null) {
            Timber.e("E40BasePositionInfo 为空!");
            basePositionInfo = new E40BasePositionInfo();
            return;
        }
        baseModeOld = basePositionInfo.getMode().trim();
        baseMode = basePositionInfo.getMode().trim();
        longitude = basePositionInfo.getLon().trim();
        latitude = basePositionInfo.getLat().trim();
        elevation = basePositionInfo.getAlt().trim();

        if (baseModeOld.equals("1")) {
            baseModePos = 0;
            mTvBaseMode.setText("自动");
            setBasePositionParamState(false);

        } else if (baseModeOld.equals("2")) {
            baseModePos = 1;
            mTvBaseMode.setText("首次自动");
            setBasePositionParamState(false);

        } else if (baseModeOld.equals("3")) {
            baseModePos = 2;
            mTvBaseMode.setText("手动");
            setBasePositionParamState(true);
        }

        try {
            decimalFormat.applyPattern("#.########");
            mEtLongitude.setText(decimalFormat.format(Double.parseDouble(longitude)));
            mEtLatitude.setText(decimalFormat.format(Double.parseDouble(latitude)));
            decimalFormat.applyPattern("#.########");
            mEtElevation.setText(decimalFormat.format(Double.parseDouble(elevation)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setBasePositionParamState(boolean isEditable) {
        if (isEditable) {
            mEtLongitude.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_666666));
            mEtLatitude.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_666666));
            mEtElevation.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_666666));

            mEtLongitude.setHint("请输入");
            mEtLatitude.setHint("请输入");
            mEtElevation.setHint("请输入");

            mEtLongitude.setEnabled(true);
            mEtLatitude.setEnabled(true);
            mEtElevation.setEnabled(true);
        } else {
            mEtLongitude.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_cccccc));
            mEtLatitude.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_cccccc));
            mEtElevation.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_cccccc));

            mEtLongitude.setHint("");
            mEtLatitude.setHint("");
            mEtElevation.setHint("");

            mEtLongitude.setEnabled(false);
            mEtLatitude.setEnabled(false);
            mEtElevation.setEnabled(false);
        }
    }

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
        rtkModeOld = rtkMode;
        baseModeOld = baseMode;
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
        if (rtkModeOld != null && rtkMode != null && !rtkModeOld.equals(rtkMode)) {
            return true;
        }
        //基站模式并且是手动设置基站位置信息
        if (rtkModeOld != null && rtkMode.equals("0") && baseMode != null && baseMode.equals("3")) {
            if (longitude != null && !longitude.equals(mEtLongitude.getText().toString().trim())) {
                return true;
            }
            if (latitude != null && !latitude.equals(mEtLatitude.getText().toString().trim())) {
                return true;
            }
            if (elevation != null && !elevation.equals(mEtElevation.getText().toString().trim())) {
                return true;
            }
        }
        return false;
    }
}