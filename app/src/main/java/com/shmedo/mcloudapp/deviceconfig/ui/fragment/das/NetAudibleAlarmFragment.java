package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.AlarmTypeEntity;
import com.shmedo.configlibrary.iot.cmd.entity.das.AudibleAlarmEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.AudibleAlarm;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：     声光报警器
 */
public class NetAudibleAlarmFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.alarmEnableSBtn)
    SwitchButton mSbAlarmEnable;

    @BindView(R.id.tv_alarm_type)
    TextView mTvAlarmType;//监测要素 0  降雨量  1 水位

    @BindView(R.id.et_level1)
    EditText mEtLevel1;//一级预警值

    @BindView(R.id.et_level2)
    EditText mEtLevel2;//二级预警值

    @BindView(R.id.et_level3)
    EditText mEtLevel3;//三级预警值

    @BindView(R.id.et_alarm_addr)
    EditText mEtAlarmAddr;//地址

    @BindView(R.id.et_play_time)
    EditText mEtPlayTime;//播放时长

    @BindView(R.id.et_play_gap)
    EditText mEtPlayGap;//切换间隙

    @BindView(R.id.screenEnableSBtn)
    SwitchButton mSbScreenEnable;

    @BindView(R.id.et_screen_addr)
    EditText mEtScreenAddr;//地址

    @BindView(R.id.et_show_time)
    EditText mEtShowTime;//显示时长

    @BindView(R.id.et_show_gap)
    EditText mEtShowGap;//显示间隙

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.alarmChildMaskLayer)
    ViewGroup alarmChildMaskLayer;

    @BindView(R.id.screenChildMaskLayer)
    ViewGroup screenChildMaskLayer;

    private String alarmType;
    private String level1;
    private String level2;
    private String level3;
    private String alarmAddr;
    private String playTime;
    private String playGap;
    private String screenAddr;
    private String showTime;
    private String showGap;

    private AudibleAlarm audibleAlarm = new AudibleAlarm();

    private String[] alarmTypes;

    public static NetAudibleAlarmFragment newInstance(DeviceInfo deviceInfo) {
        NetAudibleAlarmFragment fragment = new NetAudibleAlarmFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.audible_alarm_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        alarmTypes = StringUtils.getStringArray(R.array.alarm_type);
        setFilter();
        resetHint();
        initViewListener();
        showWaitDialog("加载中...");
        queryAlarmControl();
    }

    private void setFilter() {
        mEtAlarmAddr.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtPlayTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtPlayGap.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtShowTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtShowGap.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtScreenAddr.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtPlayTime.setHint(StringUtils.getString(R.string.alarm_play_time_hint));
        mEtPlayGap.setHint(StringUtils.getString(R.string.alarm_play_gap_hint));

        mTvAlarmType.setText(alarmTypes[0]);
        alarmType = "0";
    }

    private void resetHint() {
        if (alarmType.equals("0")) {
            mEtLevel1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            mEtLevel2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            mEtLevel3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            mEtLevel1.setHint(StringUtils.getString(R.string.rain_level1_hint));
            mEtLevel2.setHint(StringUtils.getString(R.string.rain_level1_hint));
            mEtLevel3.setHint(StringUtils.getString(R.string.rain_level1_hint));
        } else {
            mEtLevel1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            mEtLevel2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            mEtLevel3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            mEtLevel1.setHint(StringUtils.getString(R.string.water_level1_hint));
            mEtLevel2.setHint(StringUtils.getString(R.string.water_level2_hint));
            mEtLevel3.setHint(StringUtils.getString(R.string.water_level3_hint));
        }
    }

    private void initViewListener() {
        mSbAlarmEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    alarmChildMaskLayer.setVisibility(View.VISIBLE);
                } else {
                    alarmChildMaskLayer.setVisibility(View.GONE);
                }
            }
        });
        mSbScreenEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    screenChildMaskLayer.setVisibility(View.VISIBLE);
                } else {
                    screenChildMaskLayer.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 查询声光报警器控制参数
     */
    private void queryAlarmControl() {
        AlarmTypeEntity alarmTypeEntity = new AlarmTypeEntity(alarmType);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM, alarmTypeEntity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    @OnClick({R.id.ll_alarm_type, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_alarm_type) {
            showAlarmTypeDialog();

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
     * 选择报警监测要素
     */
    private void showAlarmTypeDialog() {
        int pos = Arrays.asList(alarmTypes).indexOf(String.valueOf(mTvAlarmType.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", alarmTypes,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvAlarmType.setText(text);
                                alarmType = String.valueOf(position);
                                resetHint();
                                showWaitDialog("加载中...");
                                queryAlarmControl();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private boolean checkValueIsValid() {
        level1 = mEtLevel1.getText().toString().trim();
        level2 = mEtLevel2.getText().toString().trim();
        level3 = mEtLevel3.getText().toString().trim();
        alarmAddr = mEtAlarmAddr.getText().toString().trim();
        playTime = mEtPlayTime.getText().toString().trim();
        playGap = mEtPlayGap.getText().toString().trim();
        screenAddr = mEtScreenAddr.getText().toString().trim();
        showTime = mEtShowTime.getText().toString().trim();
        showGap = mEtShowGap.getText().toString().trim();


        if (TextUtils.isEmpty(mEtLevel1.getText().toString())) {
            PopTip.show("请输入一级报警值!").autoDismiss(3500).iconError();
            mEtLevel1.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtLevel1.getText().toString());
        } catch (Exception ex) {
            PopTip.show("一级报警值必须为整数值!").autoDismiss(3500).iconError();
            mEtLevel1.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mEtLevel2.getText().toString())) {
            PopTip.show("请输入二级报警值!").autoDismiss(3500).iconError();
            mEtLevel2.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtLevel2.getText().toString());
            if (value < Integer.parseInt(mEtLevel1.getText().toString())) {
                PopTip.show("二级报警值不能小于一级!").autoDismiss(3500).iconError();
                return false;
            }
        } catch (Exception ex) {
            PopTip.show("二级报警值必须为整数值!").autoDismiss(3500).iconError();
            mEtLevel2.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mEtLevel3.getText().toString())) {
            PopTip.show("请输入三级报警值!").autoDismiss(3500).iconError();
            mEtLevel3.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtLevel3.getText().toString());
            if (value < Integer.parseInt(mEtLevel2.getText().toString())) {
                PopTip.show("三级报警值不能小于二级!").autoDismiss(3500).iconError();
                return false;
            }
        } catch (Exception ex) {
            PopTip.show("三级报警值必须为整数值!").autoDismiss(3500).iconError();
            mEtLevel3.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mEtAlarmAddr.getText().toString())) {
            PopTip.show("请输入声光报警器地址!").autoDismiss(3500).iconError();
            mEtAlarmAddr.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtAlarmAddr.getText().toString());

        } catch (Exception ex) {
            PopTip.show("声光报警器地址必须为整数值!").autoDismiss(3500).iconError();
            mEtAlarmAddr.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mEtPlayTime.getText().toString())) {
            PopTip.show("请输入播放时长!").autoDismiss(3500).iconError();
            mEtPlayTime.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtPlayTime.getText().toString());
            if (value < 10 || value > 300) {
                PopTip.show("播放时长不能小于10秒或大于300秒!").autoDismiss(3500).iconError();
                return false;
            }
        } catch (Exception ex) {
            PopTip.show("播放时长必须为整数值!").autoDismiss(3500).iconError();
            mEtPlayTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mEtPlayGap.getText().toString())) {
            PopTip.show("请输入切换间隙!").autoDismiss(3500).iconError();
            mEtPlayGap.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtPlayGap.getText().toString());
            if (value < 5 || value > 300 || value > Integer.parseInt(mEtPlayTime.getText().toString())) {
                PopTip.show("播放时长不能小于5秒或大于300秒，并且不能超过播放时长!").autoDismiss(4000).iconError();
                return false;
            }
        } catch (Exception ex) {
            PopTip.show("切换间隙必须为整数值!").autoDismiss(3500).iconError();
            mEtPlayGap.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mEtScreenAddr.getText().toString())) {
            PopTip.show("请输入电子点阵屏地址!").autoDismiss(3500).iconError();
            mEtScreenAddr.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtScreenAddr.getText().toString());

        } catch (Exception ex) {
            PopTip.show("电子点阵屏必须为整数值!").autoDismiss(3500).iconError();
            mEtScreenAddr.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mEtShowTime.getText().toString())) {
            PopTip.show("请输入显示时长!").autoDismiss(3500).iconError();
            mEtShowTime.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtShowTime.getText().toString());

        } catch (Exception ex) {
            PopTip.show("显示时长必须为整数值!").autoDismiss(3500).iconError();
            mEtShowTime.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(mEtShowGap.getText().toString())) {
            PopTip.show("请输入显示间隙!").autoDismiss(3500).iconError();
            mEtShowGap.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(mEtShowGap.getText().toString());

        } catch (Exception ex) {
            PopTip.show("显示间隙必须为整数值!").autoDismiss(3500).iconError();
            mEtShowGap.requestFocus();
            return false;
        }

//        if (!showGap.equals(mEtShowGap.getText().toString())) {
//            if (TextUtils.isEmpty(mEtShowGap.getText().toString())) {
//                PopTip.show("请输入显示间隙!").autoDismiss(3500).iconError();
//                mEtShowGap.requestFocus();
//                return false;
//            }
//            try {
//                int value = Integer.parseInt(mEtShowGap.getText().toString());
//
//            } catch (Exception ex) {
//                PopTip.show("显示间隙必须为整数值!").autoDismiss(3500).iconError();
//                mEtShowGap.requestFocus();
//                return false;
//            }
//            showGap = mEtShowGap.getText().toString();
//        } else {
//            showGap = null;
//        }

        return true;
    }

    private void processSave() {
        try {
            AudibleAlarmEntity entity = new AudibleAlarmEntity();
            entity.setAlarmstatus(mSbAlarmEnable.isChecked() ? "0" : "1");
            entity.setScreenstatus(mSbScreenEnable.isChecked() ? "0" : "1");
            entity.setAlarmtype(alarmType);
            entity.setLevel1(level1);
            entity.setLevel2(level2);
            entity.setLevel3(level3);
            entity.setAlarmaddr(alarmAddr);
            entity.setPlaytime(playTime);
            entity.setPlaygap(playGap);
            entity.setScreenaddr(screenAddr);
            entity.setShowtime(showTime);
            entity.setShowgap(showGap);

            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_AUDIBLE_ALARM, entity);
            showWaitDialog("处理中...");
            doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case DAS_MD_GET_AUDIBLE_ALARM: {
                IOTCommandResult<AudibleAlarm> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询报警控制参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                audibleAlarm = commandResult.getResult();
                initAlarmControl();
            }
            break;

            case DAS_MD_SET_AUDIBLE_ALARM: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置报警控制参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("保存成功");
            }
            break;

            default:
                break;
        }
    }

    private void initAlarmControl() {
        if (audibleAlarm == null) {
            Timber.e("AudibleAlarm 为空!");
            audibleAlarm = new AudibleAlarm();
            return;
        }
        alarmType = audibleAlarm.getAlarmtype();
        level1 = audibleAlarm.getLevel1();
        level2 = audibleAlarm.getLevel2();
        level3 = audibleAlarm.getLevel3();
        alarmAddr = audibleAlarm.getAlarmaddr();
        playTime = audibleAlarm.getPlaytime();
        playGap = audibleAlarm.getPlaygap();
        screenAddr = audibleAlarm.getScreenaddr();
        showTime = audibleAlarm.getShowtime();
        showGap = audibleAlarm.getShowgap();

        alarmChildMaskLayer.setVisibility(audibleAlarm.getAlarmstatus().equals("0") ? View.GONE : View.VISIBLE);
        mSbAlarmEnable.setCheckedImmediatelyNoEvent(audibleAlarm.getAlarmstatus().equals("0"));
        screenChildMaskLayer.setVisibility(audibleAlarm.getScreenstatus().equals("0") ? View.GONE : View.VISIBLE);
        mSbScreenEnable.setCheckedImmediatelyNoEvent(audibleAlarm.getScreenstatus().equals("0"));
        switch (alarmType) {
            case "0":
                mTvAlarmType.setText(alarmTypes[0]);
                break;

            case "1":
                mTvAlarmType.setText(alarmTypes[1]);
                break;
        }
        mEtLevel1.setText(level1);
        mEtLevel2.setText(level2);
        mEtLevel3.setText(level3);
        mEtAlarmAddr.setText(alarmAddr);
        mEtPlayTime.setText(playTime);
        mEtPlayGap.setText(playGap);
        mEtScreenAddr.setText(screenAddr);
        mEtShowTime.setText(showTime);
        mEtShowGap.setText(showGap);
    }
}