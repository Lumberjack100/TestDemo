package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.das.DasDigitalPiezometerEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.das.DasDigitalPiezometerInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class NetDasDigitalPiezometerFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.digitalOsmometerEnableSBtn)
    SwitchButton mSbDigitalOsmometerEnable;

    @BindView(R.id.digitalOsmometerChildsLayout)
    ViewGroup digitalOsmometerChildsLayout;//渗压计配置项

    @BindView(R.id.osmometerAddressEt)
    EditText mEtOsmometerAddress;//渗压计地址

    @BindView(R.id.waterAlarmValueEt)
    EditText mEtWaterAlarmValue;//深度触发值-水位报警值

    @BindView(R.id.waterRevisedEt)
    EditText mEtWaterRevised;//深度修正值

    @BindView(R.id.osmometerCordEt)
    EditText mEtOsmometerCord;//渗压计绳长

    @BindView(R.id.nozzelHeightEt)
    EditText mEtNozzelHeight;//管口高程

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private DasDigitalPiezometerInfo digitalPiezometerInfo;

    private String osmometerAddress;
    private String depthTriggerValue;
    private String depthCorrection;
    private String osmometerLength;
    private String nozzelHeight;

    public static NetDasDigitalPiezometerFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetDasDigitalPiezometerFragment fragment = new NetDasDigitalPiezometerFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.das_digital_piezometer_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        setSwitchViewListener();
        queryDigitalPiezometerInfo();
    }

    private void setFilter() {
        mEtOsmometerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtWaterAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtWaterRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
    }

    /**
     * 开关控件事件
     */
    private void setSwitchViewListener() {
        //数字式渗压计启用开关事件
        mSbDigitalOsmometerEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
                } else {
                    digitalOsmometerChildsLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 查询渗压计信息
     */
    private void queryDigitalPiezometerInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }


    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        osmometerAddress = mEtOsmometerAddress.getText().toString().trim();
        depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();
        depthCorrection = mEtWaterRevised.getText().toString().trim();
        osmometerLength = mEtOsmometerCord.getText().toString().trim();
        nozzelHeight = mEtNozzelHeight.getText().toString().trim();

        if (TextUtils.isEmpty(osmometerAddress)) {
            ToastUtils.show("请输入渗压计地址");
            mEtOsmometerAddress.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(osmometerAddress);
            if (value < 0 || value > 255) {
                ToastUtils.show("请输入正确的渗压计地址!");
                mEtOsmometerAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的渗压计地址!");
            mEtOsmometerAddress.requestFocus();
            return false;
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getThreshold())).equals(depthTriggerValue)) {
            if (TextUtils.isEmpty(depthTriggerValue)) {
                ToastUtils.show("请输入水位报警值");
                return false;
            }
            try {
                int value = Integer.parseInt(depthTriggerValue);
                if (value < 1 || value > 65535) {
                    ToastUtils.show("请输入正确的水位报警值!");
                    mEtWaterAlarmValue.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水位报警值!");
                mEtWaterAlarmValue.requestFocus();
                return false;
            }
        } else {
            depthTriggerValue = "";
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getCorrval())).equals(depthCorrection)) {
            if (TextUtils.isEmpty(depthCorrection)) {
                ToastUtils.show("请输入水深修正值");
                return false;
            }
            try {
                double value = Double.parseDouble(depthCorrection);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的水深修正值!");
                mEtWaterRevised.requestFocus();
                return false;
            }
        }else {
            depthCorrection = "";
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getRopelen())).equals(osmometerLength)) {
            if (TextUtils.isEmpty(osmometerLength)) {
                ToastUtils.show("请输入渗压计绳长");
                return false;
            }
            try {
                double value = Double.parseDouble(osmometerLength);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的渗压计绳长!");
                mEtOsmometerCord.requestFocus();
                return false;
            }
        }else {
            osmometerLength = "";
        }

        if (!decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getTubealti())).equals(nozzelHeight)) {
            if (TextUtils.isEmpty(nozzelHeight)) {
                ToastUtils.show("请输入管口高程值");
                return false;
            }
            try {
                double value = Double.parseDouble(nozzelHeight);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的管口高程值!");
                mEtNozzelHeight.requestFocus();
                return false;
            }
        }else {
            nozzelHeight = "";
        }
        return true;
    }

    private void processSave() {
        DasDigitalPiezometerEntity entity = new DasDigitalPiezometerEntity();
        entity.setAddr(osmometerAddress);
        entity.setThreshold(depthTriggerValue);
        entity.setCorrval(depthCorrection);
        entity.setRopelen(osmometerLength);
        entity.setTubealti(nozzelHeight);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO, entity);
        showProgressDialog("处理中...");
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
            dismissProgressDialog();
            showDispatchFailedDialog();
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(0);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog() {
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
            case DAS_MD_GET_DIGITAL_PIEZOMETER_INFO: {//
                IOTCommandResult<DasDigitalPiezometerInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询数字渗压计参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                digitalPiezometerInfo = commandResult.getResult();
                initDigitalPiezometerInfo();
            }
            break;

            case DAS_MD_SET_DIGITAL_PIEZOMETER_INFO: {//
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置数字渗压计参数出错!", cmdResult.getReason());
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

    private void initDigitalPiezometerInfo() {
        if (digitalPiezometerInfo == null) {
            Timber.e("DasDigitalPiezometerInfo 为空!");
            digitalPiezometerInfo = new DasDigitalPiezometerInfo();
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
            return;
        }
        if (digitalPiezometerInfo.getSw().equals("0")) {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(false);
            digitalOsmometerChildsLayout.setVisibility(View.GONE);
        } else {
            mSbDigitalOsmometerEnable.setCheckedImmediatelyNoEvent(true);
            digitalOsmometerChildsLayout.setVisibility(View.VISIBLE);
        }
        try {
            osmometerAddress = digitalPiezometerInfo.getAddr();
            depthTriggerValue = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getThreshold()));
            depthCorrection = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getCorrval()));
            osmometerLength = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getRopelen()));
            nozzelHeight = decimalFormat.format(Double.parseDouble(digitalPiezometerInfo.getTubealti()));

            mEtOsmometerAddress.setText(osmometerAddress);
            mEtWaterAlarmValue.setText(depthTriggerValue);
            mEtWaterRevised.setText(depthCorrection);
            mEtOsmometerCord.setText(osmometerLength);
            mEtNozzelHeight.setText(nozzelHeight);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
        osmometerAddress = mEtOsmometerAddress.getText().toString().trim();
        depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();
        depthCorrection = mEtWaterRevised.getText().toString().trim();
        osmometerLength = mEtOsmometerCord.getText().toString().trim();
        nozzelHeight = mEtNozzelHeight.getText().toString().trim();
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
        if (osmometerAddress != null && !osmometerAddress.equals(mEtOsmometerAddress.getText().toString().trim())) {
            return true;
        }
        if (depthTriggerValue != null && !depthTriggerValue.equals(mEtWaterAlarmValue.getText().toString().trim())) {
            return true;
        }
        if (depthCorrection != null && !depthCorrection.equals(mEtWaterRevised.getText().toString().trim())) {
            return true;
        }
        if (osmometerLength != null && !osmometerLength.equals(mEtOsmometerCord.getText().toString().trim())) {
            return true;
        }
        if (nozzelHeight != null && !nozzelHeight.equals(mEtNozzelHeight.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}