package com.shmedo.mcloudapp.deviceconfig.view.adme;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeBasicConfigEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicConfigInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/13 <br/>
 * 描述：     TODO
 */
public class AdmeBasicParamConfigView extends LinearLayout {
    @BindView(R.id.tv_inclinometer_type)
    TextView mTvInclinometerType;

    @BindView(R.id.et_collector_address)
    ClearEditText mEtCollectorAddress;//采集器地址

    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddress;//Mac 地址

    @BindView(R.id.et_inclination_tube_hole_depth)
    ClearEditText mEtInclinometerTubeHoleDepth;//测斜管孔深(m)

    @BindView(R.id.et_decentralization_speed)
    ClearEditText mEtDecentralizationSpeed;//下放速度(r/min)

    @BindView(R.id.et_decentralization_waiting_time)
    ClearEditText mEtDecentralizationWaitingTime;//下放等待时间(min)

    @BindView(R.id.tv_data_settlement_method)
    TextView mTvDataSettlementMethod;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_inclinometer_type)
    ViewGroup inclinometerTypeLayout;

    @BindView(R.id.ll_collector_address)
    ViewGroup collectorAddressLayout;

    @BindView(R.id.ll_mac_address)
    ViewGroup macAddressLayout;

    @BindView(R.id.ll_inclination_tube_hole_depth)
    ViewGroup inclinometerTubeHoleDepthLayout;

    @BindView(R.id.ll_decentralization_speed)
    ViewGroup decentralizationSpeedLayout;

    @BindView(R.id.ll_decentralization_waiting_time)
    ViewGroup decentralizationWaitingTimeLayout;

    @BindView(R.id.ll_data_settlement_method)
    ViewGroup dataSettlementMethodLayout;

    private int inclinometerTypePos;
    private int dataSettlementMethodPos;

    private String inclinometerTypeOld;//测斜仪类型
    private String inclinometerType;// 测斜仪类型
    private String address;// 采集器地址/Mac 地址
    private String inclinometerTubeHoleDepth;// 测斜管孔深(m)
    private String decentralizationSpeed;// 下放速度(r/min)
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String dataSettlementMethodOld;//数据结算方式
    private String dataSettlementMethod;// 数据结算方式

    private DecimalFormat decimalFormat = new DecimalFormat();

    public AdmeBasicConfigInfo basicConfigParam;


    public AdmeBasicParamConfigView(Context context) {
        this(context, null);
    }

    public AdmeBasicParamConfigView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeBasicParamConfigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_basic_param_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtCollectorAddress.setHint("0-32");

        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mEtMacAddress.setHint("XXXXXXXXXXXX");

        mEtInclinometerTubeHoleDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        mEtDecentralizationSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDecentralizationSpeed.setHint("1-180");

        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationWaitingTime.setHint("1-32");
    }

    /**
     * 选择测斜仪类型
     */
    public void showInclinometerTypeDialog(Context context) {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"433测斜仪", "蓝牙测斜仪"},
                        null, inclinometerTypePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                inclinometerTypePos = position;
                                mTvInclinometerType.setText(text);
                                if (position == 0) {
                                    inclinometerType = "0";
                                    collectorAddressLayout.setVisibility(View.VISIBLE);
                                    macAddressLayout.setVisibility(View.GONE);
                                } else {
                                    inclinometerType = "1";
                                    collectorAddressLayout.setVisibility(View.GONE);
                                    macAddressLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 选择数据结算方式
     */
    public void showDataSettlementMethodDialog(Context context) {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"顶固定法", "底固定法"},
                        null, dataSettlementMethodPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                dataSettlementMethodPos = position;
                                mTvDataSettlementMethod.setText(text);
                                if (position == 0) {
                                    dataSettlementMethod = "0";
                                } else {
                                    dataSettlementMethod = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    public boolean checkValueIsValid() {
        if (inclinometerType.equals("0")) {
            address = mEtCollectorAddress.getText().toString().trim();

            if (TextUtils.isEmpty(address)) {
                ToastUtils.show("请输入采集器地址!");
                mEtMacAddress.requestFocus();
                return false;
            }
            if (Integer.parseInt(address) < 0 || Integer.parseInt(address) > 32) {
                ToastUtils.show("请输入正确的采集器地址!");
                mEtCollectorAddress.requestFocus();
                return false;
            }
        } else {
            address = mEtMacAddress.getText().toString().trim();

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
        }

        if (!inclinometerTubeHoleDepth.equals("NullKey")) {
            inclinometerTubeHoleDepth = mEtInclinometerTubeHoleDepth.getText().toString().trim();
            if (TextUtils.isEmpty(inclinometerTubeHoleDepth)) {
                ToastUtils.show("请输入测斜管孔深!");
                mEtInclinometerTubeHoleDepth.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(inclinometerTubeHoleDepth);
                if (value < 1) {
                    ToastUtils.show("请输入正确的测斜管孔深!");
                    mEtInclinometerTubeHoleDepth.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测斜管孔深!");
                mEtInclinometerTubeHoleDepth.requestFocus();
                return false;
            }
        }
        if (!decentralizationSpeed.equals("NullKey")) {
            decentralizationSpeed = mEtDecentralizationSpeed.getText().toString().trim();
            if (TextUtils.isEmpty(decentralizationSpeed)) {
                ToastUtils.show("请输入下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(decentralizationSpeed);
                if (port < 1 || port > 180) {
                    ToastUtils.show("请输入正确的下放速度!");
                    mEtDecentralizationSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
        }
        if (!decentralizationWaitingTime.equals("NullKey")) {
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
        }
        return true;
    }

    public String getSetCommand() {
        String command = "";
        try {
            AdmeBasicConfigEntity entity = new AdmeBasicConfigEntity();
            entity.setInctype(basicConfigParam.getInctype().equals("NullKey") ? "NullKey" : inclinometerType);
            entity.setAddress(basicConfigParam.getAddress().equals("NullKey") ? "NullKey" : address);

            decimalFormat.applyPattern("#.##");
            entity.setInterdeep(basicConfigParam.getInterdeep().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth)));
            entity.setDownspeed(basicConfigParam.getDownspeed().equals("NullKey") ? "NullKey" : decentralizationSpeed);
            entity.setDownwaitetime(basicConfigParam.getDownwaitetime().equals("NullKey") ? "NullKey" : decentralizationWaitingTime);
            entity.setDatatype(basicConfigParam.getDatatype().equals("NullKey") ? "NullKey" : dataSettlementMethod);

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_BASIC, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    public void initParamConfigInfo() {
        if (basicConfigParam == null) {
            Timber.e("AdmeBasicConfigParam is Null!");
            basicConfigParam = new AdmeBasicConfigInfo();
            return;
        }
        inclinometerTypeOld = basicConfigParam.getInctype().trim();
        inclinometerType = basicConfigParam.getInctype().trim();
        address = basicConfigParam.getAddress().trim();
        inclinometerTubeHoleDepth = basicConfigParam.getInterdeep().trim();
        decentralizationSpeed = basicConfigParam.getDownspeed().trim();
        decentralizationWaitingTime = basicConfigParam.getDownwaitetime().trim();
        dataSettlementMethodOld = basicConfigParam.getDatatype().trim();
        dataSettlementMethod = basicConfigParam.getDatatype().trim();

        if (inclinometerTypeOld.equals("NullKey")) {
            inclinometerTypeLayout.setVisibility(View.GONE);
        } else {
            if (inclinometerTypeOld.equals("0")) {
                inclinometerTypePos = 0;
                mTvInclinometerType.setText("433测斜仪");
                mEtCollectorAddress.setText(address);
                collectorAddressLayout.setVisibility(View.VISIBLE);
                macAddressLayout.setVisibility(View.GONE);
            } else {
                inclinometerTypePos = 1;
                mTvInclinometerType.setText("蓝牙测斜仪");
                mEtMacAddress.setText(address);
                collectorAddressLayout.setVisibility(View.GONE);
                macAddressLayout.setVisibility(View.VISIBLE);
            }
        }
        try {
            if (inclinometerTubeHoleDepth.equals("NullKey")) {
                inclinometerTubeHoleDepthLayout.setVisibility(View.GONE);
            } else {
                decimalFormat.applyPattern("#.##");
                inclinometerTubeHoleDepth = decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth));
                mEtInclinometerTubeHoleDepth.setText(inclinometerTubeHoleDepth);
            }
            if (decentralizationSpeed.equals("NullKey")) {
                decentralizationSpeedLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationSpeed.setText(decentralizationSpeed);
            }
            if (decentralizationWaitingTime.equals("NullKey")) {
                decentralizationWaitingTimeLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationWaitingTime.setText(decentralizationWaitingTime);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (dataSettlementMethodOld.equals("NullKey")) {
            dataSettlementMethodLayout.setVisibility(View.GONE);
        } else {
            if (dataSettlementMethodOld.equals("0")) {
                dataSettlementMethodPos = 0;
                mTvDataSettlementMethod.setText("顶固定法");
            } else {
                dataSettlementMethodPos = 1;
                mTvDataSettlementMethod.setText("底固定法");
            }
        }
    }

    public void doAfterSetting() {
//        if (basicConfigParam != null) {
//            basicConfigParam.setInctype(inclinometerType);
//            basicConfigParam.setAddress(address);
//            basicConfigParam.setInterdeep(inclinometerTubeHoleDepth);
//            basicConfigParam.setDownspeed(decentralizationSpeed);
//            basicConfigParam.setDownwaitetime(decentralizationWaitingTime);
//            basicConfigParam.setDatatype(dataSettlementMethod);
//        }
        //TODO #gh#  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
        inclinometerTypeOld = inclinometerType;
        dataSettlementMethodOld = dataSettlementMethod;
    }

    public boolean checkValueIsChange(boolean configPageEditableChanged) {
        if (!configPageEditableChanged)
            return false;

        if (inclinometerTypeOld != null && !inclinometerTypeOld.equals("NullKey") && inclinometerType != null && !inclinometerTypeOld.equals(inclinometerType)) {
            return true;
        }
        if (address != null) {
            if (inclinometerTypeOld.equals("0") && !inclinometerTypeOld.equals("NullKey") && !address.equals(mEtCollectorAddress.getText().toString().trim())) {
                return true;
            }
            if (inclinometerTypeOld.equals("1") && !inclinometerTypeOld.equals("NullKey") && !address.equals(mEtMacAddress.getText().toString().trim())) {
                return true;
            }
        }
        if (inclinometerTubeHoleDepth != null && !inclinometerTubeHoleDepth.equals("NullKey") && !inclinometerTubeHoleDepth.equals(mEtInclinometerTubeHoleDepth.getText().toString().trim())) {
            return true;
        }
        if (decentralizationSpeed != null && !decentralizationSpeed.equals("NullKey") && !decentralizationSpeed.equals(mEtDecentralizationSpeed.getText().toString().trim())) {
            return true;
        }
        if (decentralizationWaitingTime != null && !decentralizationWaitingTime.equals("NullKey") && !decentralizationWaitingTime.equals(mEtDecentralizationWaitingTime.getText().toString().trim())) {
            return true;
        }
        if (dataSettlementMethodOld != null && !dataSettlementMethodOld.equals("NullKey") && dataSettlementMethod != null && !dataSettlementMethodOld.equals(dataSettlementMethod)) {
            return true;
        }
        return false;
    }

    public void onEditableChanged(boolean isEditable) {
        inclinometerTypeLayout.setEnabled(isEditable);
        mEtCollectorAddress.setEnabled(isEditable);
        mEtMacAddress.setEnabled(isEditable);
        mEtInclinometerTubeHoleDepth.setEnabled(isEditable);
        mEtDecentralizationSpeed.setEnabled(isEditable);
        mEtDecentralizationWaitingTime.setEnabled(isEditable);
        dataSettlementMethodLayout.setEnabled(isEditable);
        if (isEditable) {
            mTvInclinometerType.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);
            mEtCollectorAddress.setHint("0-32");
            mEtMacAddress.setHint("XXXXXXXXXXXX");
            mEtInclinometerTubeHoleDepth.setHint("请输入");
            mEtDecentralizationSpeed.setHint("1-180");
            mEtDecentralizationWaitingTime.setHint("1-32");
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);
        } else {
            mTvInclinometerType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mEtCollectorAddress.setHint("");
            mEtMacAddress.setHint("");
            mEtInclinometerTubeHoleDepth.setHint("");
            mEtDecentralizationSpeed.setHint("");
            mEtDecentralizationWaitingTime.setHint("");
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//            initParamConfigInfo();
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
