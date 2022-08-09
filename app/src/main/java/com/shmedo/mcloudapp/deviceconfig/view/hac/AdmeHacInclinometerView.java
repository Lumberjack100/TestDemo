package com.shmedo.mcloudapp.deviceconfig.view.hac;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
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
public class AdmeHacInclinometerView extends LinearLayout {
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

    @BindView(R.id.ll_collection_interval)
    ViewGroup collectionIntervalLayout;

    @BindView(R.id.ll_solving_interval)
    ViewGroup solvingIntervalLayout;

    @BindView(R.id.ll_sleep_time)
    ViewGroup sleepTimeLayout;

    @BindView(R.id.ll_correction_value)
    ViewGroup correctionValueLayout;

    private String address;// 采集器地址/Mac 地址
    private String collectionInterval;//采集器采集间隔
    private String solvingInterval;//采集器解算间隔
    private String sleepTime;//休眠时间
    private String correctionValue;//测斜仪修正值

    private DecimalFormat decimalFormat = new DecimalFormat();
    public AdmeInclinometerInfo admeInclinometerInfo;

    public AdmeHacInclinometerView(Context context) {
        this(context, null);
    }

    public AdmeHacInclinometerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeHacInclinometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_hac_inclinometer_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mEtMacAddress.setHint("XXXXXXXXXXXX");

        mEtCollectionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSolvingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSleepTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }


    public boolean checkValueIsValid() {
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
        address = admeInclinometerInfo.getAddress().trim();
        collectionInterval = admeInclinometerInfo.getCollinval().trim();
        solvingInterval = admeInclinometerInfo.getCalcinval().trim();
        sleepTime = admeInclinometerInfo.getDormancytime().trim();
        correctionValue = admeInclinometerInfo.getInterupdate().trim();

        mEtMacAddress.setText(address);

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

    public void onEditableChanged(boolean isEditable) {
        mEtMacAddress.setEnabled(isEditable);
        mEtCollectionInterval.setEnabled(isEditable);
        mEtSolvingInterval.setEnabled(isEditable);
        mEtSleepTime.setEnabled(isEditable);
        mEtCorrectionValue.setEnabled(isEditable);

        if (isEditable) {
            mEtMacAddress.setHint("XXXXXXXXXXXX");
            mEtCollectionInterval.setHint("请输入");
            mEtSolvingInterval.setHint("请输入");
            mEtSleepTime.setHint("请输入");
            mEtCorrectionValue.setHint("请输入");
        } else {
            mEtMacAddress.setHint("");
            mEtCollectionInterval.setHint("");
            mEtSolvingInterval.setHint("");
            mEtSleepTime.setHint("");
            mEtCorrectionValue.setHint("");
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
