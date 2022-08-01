package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.CustomDialog;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.AttachPopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.hac.HacMeasuringDataInfoEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.AdmeModuleErrorType;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.hac.HacHoleAreaDepthInfo;
import com.shmedo.configlibrary.iot.model.hac.HacMeasuringDataInfo;
import com.shmedo.configlibrary.iot.model.hac.HacMotionState;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.hac.AdmeHacMeasuringDataProcedureActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class BleAdmeHacMeasuringDataFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddress;

    @BindView(R.id.tv_hole_num)
    TextView mTvHoleNum; //孔号

    @BindView(R.id.tv_area_num)
    TextView mTvAreaNum;//区号

    @BindView(R.id.tv_hole_depth)
    TextView mTvHoleDepth;//测斜管孔深

    @BindView(R.id.et_decentralization_waiting_time)
    ClearEditText mEtDecentralizationWaitingTime;//下放等待时间(min)

    @BindView(R.id.tv_data_settlement_method)
    TextView mTvDataSettlementMethod;//数据解算方式

    @BindView(R.id.singleWayTestEnableSBtn)
    SwitchButton mSbSingleWayTestEnable;//单向测量

    @BindView(R.id.btn_run)
    Button mBtnRun;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private String equipmodel = "0";//电机工作标识  0：停止  1：正常 2: 异常
    private String address;//MAC 地址
    private String holeno;//孔号
    private String areano;//区号
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String dataSettlementMethod;// 数据解算方式

    private final String[] settlementMethods = new String[]{"顶部固定法", "底部固定法"};

    private HacMotionState motionState;
    private HacMeasuringDataInfo measuringDataInfo;
    private final List<HacHoleAreaDepthInfo> holeAreaDepthInfoArrayList = new ArrayList<>();
    private List<String> holeNumList = new ArrayList<>();

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static BleAdmeHacMeasuringDataFragment newInstance() {
        return new BleAdmeHacMeasuringDataFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_adme_hac_measuring_data;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
//        initQueryCommands();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            initQueryCommands();
        }
    }

    private void setView() {
        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mEtMacAddress.setHint("XXXXXXXXXXXX");

        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationWaitingTime.setHint("1-32");

        mTvDataSettlementMethod.setText(settlementMethods[0]);
        dataSettlementMethod = "0";
    }

    private void initQueryCommands() {
        commandItems.clear();

        //获取数据测量配置参数
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM);
        commandItems.add(command);

        //查询电机当前运动状态
        command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE);
        commandItems.add(command);

        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_15000_MILLIS);
        sendCommandFromCmdList();
    }

    @OnClick({R.id.iv_show_hole_dropdown, R.id.ll_data_settlement_method, R.id.btn_run})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (id == R.id.iv_show_hole_dropdown) {
            showHoleDropDownList(view);

        } else if (id == R.id.ll_data_settlement_method) {
            showDataSettlementMethodDialog();

        } else if (id == R.id.btn_run) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("配置参数错误!");
                return;
            }
            initRunCommands();
        }
    }

    /**
     * 选择孔号
     *
     * @param v
     */
    private void showHoleDropDownList(View v) {
        if (holeNumList == null || holeNumList.size() == 0) {
            ToastUtils.show("没有可选孔号");
            return;
        }
        String[] holeNums = holeNumList.toArray(new String[0]);
        AttachPopupView attachPopupView = new XPopup.Builder(getContext())
                .hasShadowBg(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .isDarkTheme(false)
                .popupAnimation(PopupAnimation.NoAnimation) //NoAnimation表示禁用动画
                .atView(v)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(holeNums, null, new OnSelectListener() {
                    @Override
                    public void onSelect(int position, String text) {
                        mTvHoleNum.setText(text);
                        HacHoleAreaDepthInfo info = holeAreaDepthInfoArrayList.get(position);
                        mTvAreaNum.setText(info != null ? info.getAreano() : "");
                        mTvHoleDepth.setText(info != null ? decimalFormat.format(Double.parseDouble(info.getHoledepth())) : "");
                    }
                }, 0, 0);
        attachPopupView.show();
    }

    /**
     * 选择数据解算方式
     */
    public void showDataSettlementMethodDialog() {
        int pos = Arrays.asList(settlementMethods).indexOf(String.valueOf(mTvDataSettlementMethod.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", settlementMethods,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvDataSettlementMethod.setText(text);
                                if (position == 0) {
                                    dataSettlementMethod = "0";
                                } else {
                                    dataSettlementMethod = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private boolean checkValueIsValid() {
        address = mEtMacAddress.getText().toString();
        holeno = mTvHoleNum.getText().toString();
        areano = mTvAreaNum.getText().toString();
        decentralizationWaitingTime = mEtDecentralizationWaitingTime.getText().toString();

        if (TextUtils.isEmpty(address)) {
            ToastUtils.show("请输入Mac地址!");
            mEtMacAddress.requestFocus();
            return false;
        }
        if (!ValidateUtil.isValidMacAddressNoColon(address)) {
            ToastUtils.show("请输入正确的Mac地址!");
            mEtMacAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(holeno)) {
            ToastUtils.show("请选择孔号!");
            return false;
        }

        decentralizationWaitingTime = mEtDecentralizationWaitingTime.getText().toString().trim();
        if (TextUtils.isEmpty(decentralizationWaitingTime)) {
            ToastUtils.show("请输入下放等待时间!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(decentralizationWaitingTime);
            if (port < 1 || port > 32) {
                ToastUtils.show("请输入正确的下放等待时间!");
                mEtDecentralizationWaitingTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放等待时间!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * 数据测量配置参数
     */
    private String setMeasuringDataParamCommand() {
        try {
            HacMeasuringDataInfoEntity entity = new HacMeasuringDataInfoEntity();
            entity.setEquipmodel("1");
            entity.setAddress(address);
            entity.setHoleno(holeno);
            entity.setAreano(areano);
            entity.setDownwaitetime(decentralizationWaitingTime);
            entity.setDatatype(dataSettlementMethod);
            entity.setOnewaytest(mSbSingleWayTestEnable.isChecked() ? "1" : "0");

            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_SET_DATA_MEASURE_PARAM, entity);

            return command;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void initRunCommands() {
        commandItems.clear();

        //数据测量配置参数
        String command = setMeasuringDataParamCommand();
        if (!TextUtils.isEmpty(command))
            commandItems.add(command);

        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_15000_MILLIS);
        sendCommandFromCmdList();
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_HAC_MD_GET_DATA_MEASURE_PARAM: {//获取数据测量配置参数
                IOTCommandResult<HacMeasuringDataInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "获取数据测量配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                measuringDataInfo = commandResult.getResult();
                initMeasuringDataInfoParam();
                sendCommandFromCmdList();
            }
            break;

            case ADME_HAC_MD_GET_MOTION_STATE: {//查询电机当前运动状态
                IOTCommandResult<HacMotionState> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "获取电机当前运动状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                motionState = commandResult.getResult();
                initMotionState();
                sendCommandFromCmdList();
            }
            break;

            case ADME_HAC_MD_SET_DATA_MEASURE_PARAM: {//设置HAC数据测量参数,开始测量
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置数据测量参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeHacMeasuringDataProcedureActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, motionState);
            }
            break;
        }
    }

    /**
     * 初始化测量配置参数
     */
    private void initMeasuringDataInfoParam() {
        if (measuringDataInfo == null) {
            Timber.e("HacMeasuringDataInfo is Null!");
            measuringDataInfo = new HacMeasuringDataInfo();
            return;
        }
        try {
            equipmodel = measuringDataInfo.getEquipmodel();

            mEtMacAddress.setText(measuringDataInfo.getAddress());
            mEtDecentralizationWaitingTime.setText(measuringDataInfo.getDownwaitetime());

            dataSettlementMethod = measuringDataInfo.getDatatype();
            if (dataSettlementMethod.equals("0")) {
                mTvDataSettlementMethod.setText(settlementMethods[0]);
            } else {
                mTvDataSettlementMethod.setText(settlementMethods[1]);
            }

            mSbSingleWayTestEnable.setCheckedImmediatelyNoEvent(measuringDataInfo.getOnewaytest().equals("1"));

            holeAreaDepthInfoArrayList.clear();
            holeAreaDepthInfoArrayList.addAll(measuringDataInfo.getHolelist());
            if (holeAreaDepthInfoArrayList.size() == 0) {
                ToastUtils.show("还没有测孔信息,请先进行孔深测量");
                mBtnRun.setEnabled(false);
                return;
            }
            holeNumList.clear();
            for (HacHoleAreaDepthInfo info : holeAreaDepthInfoArrayList) {
                holeNumList.add(info.getHoleno());
            }
            HacHoleAreaDepthInfo info = holeAreaDepthInfoArrayList.get(0);
            mTvHoleNum.setText(info.getHoleno());
            mTvAreaNum.setText(info.getAreano());
            mTvHoleDepth.setText(decimalFormat.format(Double.parseDouble(info.getHoledepth())));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化电机运动状态
     */
    private void initMotionState() {
        if (motionState == null) {
            Timber.e("HacMotionState is Null!");
            motionState = new HacMotionState();
            return;
        }
        /**
         * 逻辑处理
         * 进入数据测量页面时，查询 md_hac_getdatameasparame 和 md_hac_getmotionstate 指令，先判断 equipmodel
         1.1 equipmodel =1(正常测量状态)：
         根据 motorinfo 控制跳转页面，motorinfo=2|3|4 进入数据测量页面状态；motorinfo=5|6 进入数据读取页面状态；motorinfo=7 进入数据上传页面状态。
         1.2 equipmodel =0 (停止状态)：
         单测时按钮显示正向测量；正反测时，motorinfo=8，按钮显示正向测量；motorinfo=9，按钮显示反向测量。
         1.3 equipmodel =2(异常状态)，弹框提示异常信息，点击按钮开始测量时，设备自动清除异常状态标志。
         */
        if (equipmodel.equals("1")) {//表示在测量 然后根据 motorinfo 控制跳转页面
            if (!motionState.getMotorinfo().equals("8") && !motionState.getMotorinfo().equals("9")) {
                AdmeHacMeasuringDataProcedureActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, motionState);
            } else {
                mBtnRun.setText(motionState.getMotorinfo().equals("9") ? "反向测量" : "正向测量");
            }
            return;
        }
        //停止或异常状态下,判断是否单测模式，单测模式下显示正向测量；正反测模式下，根据 motorinfo 处理操作按钮
        if (mSbSingleWayTestEnable.isChecked()) {
            mBtnRun.setText("正向测量");
        } else {
            mBtnRun.setText(motionState.getMotorinfo().equals("9") ? "反向测量" : "正向测量");
        }
        if (equipmodel.equals("2") && !motionState.getAbndiasis().equals("0")) {//表示异常，展示异常原因
            showErrorProtectionTip(motionState.getAbndiasis());
        }
    }

    private void showErrorProtectionTip(String abndiasis) {
        //列出异常原因
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("异常原因: \n");
        String[] codes = abndiasis.split("\\|");
        for (String code : codes) {
            AdmeModuleErrorType errorType = AdmeModuleErrorType.valueByCode(code);
            if (errorType != null) {
                stringBuilder.append(errorType.getDescription());
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append("\n已启动异常保护，请排除故障后重新测量");
        CustomDialog.build()
                .setCustomView(new OnBindView<CustomDialog>(R.layout.error_protection_tip) {
                    @Override
                    public void onBind(final CustomDialog dialog, View v) {
                        TextView tvContent = v.findViewById(R.id.tv_content);
                        Button btnOk = v.findViewById(R.id.btn_sure);
                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        tvContent.setText(stringBuilder.toString());
                    }
                })
                .setCancelable(false)
                .setMaskColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.dialog_mask))
                .show();
    }
}