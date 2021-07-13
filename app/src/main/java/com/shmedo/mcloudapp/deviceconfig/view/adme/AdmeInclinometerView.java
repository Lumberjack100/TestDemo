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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeInclinometerEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeInclinometerInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/13 <br/>
 * 描述：     测斜仪参数配置页面
 */
public class AdmeInclinometerView extends LinearLayout {
    @BindView(R.id.tv_inclinometer_type)
    TextView mTvInclinometerType;

    @BindView(R.id.tv_low_power_mode)
    TextView mTvLowPowerMode;

    @BindView(R.id.et_collector_address)
    ClearEditText mEtCollectorAddress;//采集器地址

    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddress;//Mac 地址

    @BindView(R.id.et_collection_interval)
    ClearEditText mEtCollectionInterval;

    @BindView(R.id.et_solving_interval)
    ClearEditText mEtSolvingInterval;

    @BindView(R.id.et_sleep_time)
    ClearEditText mEtSleepTime;

    @BindView(R.id.et_correction_value)
    ClearEditText mEtCorrectionValue;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_inclinometer_type)
    ViewGroup inclinometerTypeLayout;

    @BindView(R.id.ll_low_power_mode)
    ViewGroup lowPowerModeLayout;

    @BindView(R.id.ll_collector_address)
    ViewGroup collectorAddressLayout;

    @BindView(R.id.ll_mac_address)
    ViewGroup macAddressLayout;

    @BindView(R.id.ll_collection_interval)
    ViewGroup collectionIntervalLayout;

    @BindView(R.id.ll_solving_interval)
    ViewGroup solvingIntervalLayout;

    @BindView(R.id.ll_sleep_time)
    ViewGroup sleepTimeLayout;

    @BindView(R.id.ll_correction_value)
    ViewGroup correctionValueLayout;

    private int inclinometerTypePos;
    private int lowPowerModePos;

    private String inclinometerTypeOld;//测斜仪类型
    private String inclinometerType;// 测斜仪类型
    private String lowPowerModeOld;//低功耗模式
    private String lowPowerMode;// 低功耗模式
    private String address;// 采集器地址/Mac 地址
    private String collectionInterval;//采集器采集间隔
    private String solvingInterval;//采集器解算间隔
    private String sleepTime;//休眠时间
    private String correctionValue;//测斜仪修正值

    private DecimalFormat decimalFormat = new DecimalFormat();
    public AdmeInclinometerInfo admeInclinometerInfo;

    public AdmeInclinometerView(Context context) {
        this(context, null);
    }

    public AdmeInclinometerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeInclinometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_inclinometer_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtCollectorAddress.setHint("0-32");

        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mEtMacAddress.setHint("XXXXXXXXXXXX");

        mEtCollectionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSolvingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSleepTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
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
     * 选择低功耗模式
     */
    public void showLowPowerModeDialog(Context context) {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"关闭", "开启"},
                        null, lowPowerModePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                lowPowerModePos = position;
                                mTvLowPowerMode.setText(text);
                                if (position == 0) {
                                    lowPowerMode = "0";
                                } else {
                                    lowPowerMode = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    public boolean checkValueIsValid() {
        if (!inclinometerType.equals("NullKey")) {
            if (inclinometerType.equals("0")) {
                address = mEtCollectorAddress.getText().toString().trim();
                if (TextUtils.isEmpty(address)) {
                    ToastUtils.show("请输入采集器地址!");
                    mEtCollectorAddress.requestFocus();
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
        }
        if (!collectionInterval.equals("NullKey")) {
            collectionInterval = mEtCollectionInterval.getText().toString().trim();
            if (TextUtils.isEmpty(collectionInterval)) {
                ToastUtils.show("请输入采集器采集间隔!");
                mEtCollectionInterval.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(collectionInterval);
                if (value < 1) {
                    ToastUtils.show("请输入正确的采集器采集间隔!");
                    mEtCollectionInterval.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的采集器采集间隔!");
                mEtCollectionInterval.requestFocus();
                return false;
            }
        }
        if (!solvingInterval.equals("NullKey")) {
            solvingInterval = mEtSolvingInterval.getText().toString().trim();
            if (TextUtils.isEmpty(solvingInterval)) {
                ToastUtils.show("请输入采集器解算间隔!");
                mEtSolvingInterval.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(solvingInterval);
                if (value < 1) {
                    ToastUtils.show("请输入正确的采集器解算间隔!");
                    mEtSolvingInterval.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的采集器解算间隔!");
                mEtSolvingInterval.requestFocus();
                return false;
            }
        }
        if (!sleepTime.equals("NullKey")) {
            sleepTime = mEtSleepTime.getText().toString().trim();
            if (TextUtils.isEmpty(sleepTime)) {
                ToastUtils.show("请输入休眠时间!");
                mEtSleepTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(sleepTime);
                if (value < 1) {
                    ToastUtils.show("请输入正确的休眠时间!");
                    mEtSleepTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的休眠时间!");
                mEtSleepTime.requestFocus();
                return false;
            }
        }
        if (!correctionValue.equals("NullKey")) {
            correctionValue = mEtCorrectionValue.getText().toString().trim();
            if (TextUtils.isEmpty(correctionValue)) {
                ToastUtils.show("测斜仪修正值!");
                mEtCorrectionValue.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(correctionValue);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测斜仪修正值!");
                mEtCorrectionValue.requestFocus();
                return false;
            }
        }
        return true;
    }

    public String getSetCommand() {
        String command = "";
        try {
            AdmeInclinometerEntity entity = new AdmeInclinometerEntity();
            entity.setInctype(admeInclinometerInfo.getInctype().equals("NullKey") ? "NullKey" : inclinometerType);
            entity.setLowpower(admeInclinometerInfo.getLowpower().equals("NullKey") ? "NullKey" : lowPowerMode);
            entity.setAddress(admeInclinometerInfo.getAddress().equals("NullKey") ? "NullKey" : address);
            entity.setCollinval(admeInclinometerInfo.getCollinval().equals("NullKey") ? "NullKey" : collectionInterval);
            entity.setCalcinval(admeInclinometerInfo.getCalcinval().equals("NullKey") ? "NullKey" : solvingInterval);
            entity.setDormancytime(admeInclinometerInfo.getDormancytime().equals("NullKey") ? "NullKey" : sleepTime);
            decimalFormat.applyPattern("#.####");
            entity.setInterupdate(admeInclinometerInfo.getInterupdate().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(correctionValue)));

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_INCLINOMETER, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    public void initParamConfigInfo() {
        if (admeInclinometerInfo == null) {
            Timber.e("AdmeInclinometerInfo is Null!");
            admeInclinometerInfo = new AdmeInclinometerInfo();
            return;
        }
        inclinometerTypeOld = admeInclinometerInfo.getInctype().trim();
        inclinometerType = admeInclinometerInfo.getInctype().trim();
        lowPowerModeOld = admeInclinometerInfo.getLowpower().trim();
        lowPowerMode = admeInclinometerInfo.getLowpower().trim();
        address = admeInclinometerInfo.getAddress().trim();
        collectionInterval = admeInclinometerInfo.getCollinval().trim();
        solvingInterval = admeInclinometerInfo.getCalcinval().trim();
        sleepTime = admeInclinometerInfo.getDormancytime().trim();
        correctionValue = admeInclinometerInfo.getInterupdate().trim();

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
        if (lowPowerModeOld.equals("NullKey")) {
            lowPowerModeLayout.setVisibility(View.GONE);
        } else {
            if (lowPowerModeOld.equals("0")) {
                lowPowerModePos = 0;
                mTvLowPowerMode.setText("关闭");
            } else {
                lowPowerModePos = 1;
                mTvLowPowerMode.setText("开启");
            }
        }

        try {
            if (collectionInterval.equals("NullKey")) {
                collectionIntervalLayout.setVisibility(View.GONE);
            } else {
                mEtCollectionInterval.setText(collectionInterval);
            }
            if (solvingInterval.equals("NullKey")) {
                solvingIntervalLayout.setVisibility(View.GONE);
            } else {
                mEtSolvingInterval.setText(solvingInterval);
            }
            if (sleepTime.equals("NullKey")) {
                sleepTimeLayout.setVisibility(View.GONE);
            } else {
                mEtSleepTime.setText(sleepTime);
            }
            if (correctionValue.equals("NullKey")) {
                correctionValueLayout.setVisibility(View.GONE);
            } else {
                decimalFormat.applyPattern("#.####");
                correctionValue = decimalFormat.format(Double.parseDouble(correctionValue));
                mEtCorrectionValue.setText(correctionValue);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doAfterSetting() {
//        if (admeInclinometerInfo != null) {
//            admeInclinometerInfo.setInctype(inclinometerType);
//            admeInclinometerInfo.setLowpower(lowPowerMode);
//            admeInclinometerInfo.setAddress(address);
//            admeInclinometerInfo.setCollinval(collectionInterval);
//            admeInclinometerInfo.setCalcinval(solvingInterval);
//            admeInclinometerInfo.setDormancytime(sleepTime);
//            admeInclinometerInfo.setInterupdate(correctionValue);
//        }
        //TODO #gh#  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
        inclinometerTypeOld = inclinometerType;
        lowPowerModeOld = lowPowerMode;
    }

    public boolean checkValueIsChange(boolean configPageEditableChanged) {
        if (!configPageEditableChanged)
            return false;

        if (inclinometerTypeOld != null && !inclinometerTypeOld.equals("NullKey") && inclinometerType != null && !inclinometerTypeOld.equals(inclinometerType)) {
            return true;
        }
        if (lowPowerModeOld != null && !lowPowerModeOld.equals("NullKey") && lowPowerMode != null && !lowPowerModeOld.equals(lowPowerMode)) {
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
        if (collectionInterval != null && !collectionInterval.equals("NullKey") && !collectionInterval.equals(mEtCollectionInterval.getText().toString().trim())) {
            return true;
        }
        if (solvingInterval != null && !solvingInterval.equals("NullKey") && !solvingInterval.equals(mEtSolvingInterval.getText().toString().trim())) {
            return true;
        }
        if (sleepTime != null && !sleepTime.equals("NullKey") && !sleepTime.equals(mEtSleepTime.getText().toString().trim())) {
            return true;
        }
        if (correctionValue != null && !correctionValue.equals("NullKey") && !correctionValue.equals(mEtCorrectionValue.getText().toString().trim())) {
            return true;
        }
        return false;
    }

    public void onEditableChanged(boolean isEditable) {
        inclinometerTypeLayout.setEnabled(isEditable);
        lowPowerModeLayout.setEnabled(isEditable);
        mEtCollectorAddress.setEnabled(isEditable);
        mEtMacAddress.setEnabled(isEditable);
        mEtCollectionInterval.setEnabled(isEditable);
        mEtSolvingInterval.setEnabled(isEditable);
        mEtSleepTime.setEnabled(isEditable);
        mEtCorrectionValue.setEnabled(isEditable);

        if (isEditable) {
            mTvInclinometerType.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);
            mTvLowPowerMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);
            mEtCollectorAddress.setHint("0-32");
            mEtMacAddress.setHint("XXXXXXXXXXXX");
            mEtCollectionInterval.setHint("请输入");
            mEtSolvingInterval.setHint("请输入");
            mEtSleepTime.setHint("请输入");
            mEtCorrectionValue.setHint("请输入");
        } else {
            mTvInclinometerType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvLowPowerMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mEtCollectorAddress.setHint("");
            mEtMacAddress.setHint("");
            mEtCollectionInterval.setHint("");
            mEtSolvingInterval.setHint("");
            mEtSleepTime.setHint("");
            mEtCorrectionValue.setHint("");
//            initParamConfigInfo();
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
