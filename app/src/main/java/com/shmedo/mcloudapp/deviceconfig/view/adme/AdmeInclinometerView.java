package com.shmedo.mcloudapp.deviceconfig.view.adme;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
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
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/13 <br/>
 * 描述：     测斜仪参数配置页面
 */
public class AdmeInclinometerView extends LinearLayout {

    @BindView(R.id.tv_inclinometer_version)
    TextView mTvInclinometerVersion; //测斜仪版本

    @BindView(R.id.tv_low_power_mode)
    TextView mTvMode;

    @BindView(R.id.tv_compensate_way)
    TextView mTvCompensateWay;//补偿方式

    @BindView(R.id.et_torsion_angle)
    ClearEditText mEtTorsionAngle;//扭转角γ

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

    @BindView(R.id.ll_inclinometer_version)
    ViewGroup inclinometerVersionLayout;

    @BindView(R.id.ll_low_power_mode)
    ViewGroup lowModeLayout;

    @BindView(R.id.ll_compensate_way)
    ViewGroup compensateWayLayout;

    @BindView(R.id.ll_torsion_angle)
    ViewGroup torsionAngleLayout;

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

    private String inclinometerVersion;// 测斜仪版本
    private String mode;// 测量工作模式
    private String compensateWay;// 补偿方式
    private String torsionAngle;// 扭转角γ

    private String address;// 采集器地址/Mac 地址
    private String collectionInterval;//采集器采集间隔
    private String solvingInterval;//采集器解算间隔
    private String sleepTime;//休眠时间
    private String correctionValue;//测斜仪修正值

    private final String[] versions = new String[]{"V2.1", "V3.0"};
    private final String[] modes = new String[]{"蓝牙关测量关", "蓝牙开测量关", "蓝牙关测量开", "蓝牙开测量开"};
    private final String[] compensateWays = new String[]{"X+Y轴无扭转角补偿", "X轴扭转角补偿"};

    private DecimalFormat decimalFormat = new DecimalFormat();
    public AdmeInclinometerInfo admeInclinometerInfo = new AdmeInclinometerInfo();

    private InputFilter numberFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!"-0123456789".contains(source.charAt(i) + "")) {
                    return "";
                }
            }
            return null;
        }
    };

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
        mEtTorsionAngle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4), numberFilter});
        mEtTorsionAngle.setHint("[-90,90]");

        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mEtMacAddress.setHint("XXXXXXXXXXXX");

        mEtCollectionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSolvingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSleepTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mTvInclinometerVersion.setText(versions[0]);
        inclinometerVersion = "0";

        mTvMode.setText(modes[0]);
        mode = "5";

        mTvCompensateWay.setText(compensateWays[0]);
        compensateWay = "0";
    }

    /**
     * 选择测斜仪版本
     */
    public void showInclinometerVersionDialog(Context context) {
        int pos = Arrays.asList(versions).indexOf(String.valueOf(mTvInclinometerVersion.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", versions,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvInclinometerVersion.setText(text);
                                if (position == 0) {
                                    mode = "0";
                                    compensateWayLayout.setVisibility(View.GONE);

                                } else if (position == 1) {
                                    mode = "1";
                                    compensateWayLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择低功耗模式
     */
    public void showLowPowerModeDialog(Context context) {
        int pos = Arrays.asList(modes).indexOf(String.valueOf(mTvMode.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", modes,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvMode.setText(text);
                                if (position == 0) {
                                    mode = "5";
                                } else if (position == 1) {
                                    mode = "7";
                                } else if (position == 2) {
                                    mode = "8";
                                } else {
                                    mode = "9";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 补偿方式
     */
    public void showCompensateWayDialog(Context context) {
        int pos = Arrays.asList(compensateWays).indexOf(String.valueOf(mTvCompensateWay.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", compensateWays,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvCompensateWay.setText(text);
                                if (position == 0) {
                                    compensateWay = "0";
                                    torsionAngleLayout.setVisibility(View.GONE);
                                } else {
                                    compensateWay = "1";
                                    torsionAngleLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    public boolean checkValueIsValid() {
        if (!torsionAngle.equals("NullKey") && compensateWay.equals("1")) {
            torsionAngle = mEtTorsionAngle.getText().toString().trim();
            if (TextUtils.isEmpty(torsionAngle)) {
                ToastUtils.show("请输入扭转角γ!");
                mEtTorsionAngle.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(torsionAngle);
                if (value < -90 || value > 90) {
                    ToastUtils.show("请输入正确的扭转角γ!");
                    mEtTorsionAngle.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的扭转角γ!");
                mEtTorsionAngle.requestFocus();
                return false;
            }
        }

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
            entity.setInctype(admeInclinometerInfo.getInctype().equals("NullKey") ? "NullKey" : admeInclinometerInfo.getInctype());
            entity.setAddress(admeInclinometerInfo.getAddress().equals("NullKey") ? "NullKey" : address);
            entity.setCollinval(admeInclinometerInfo.getCollinval().equals("NullKey") ? "NullKey" : collectionInterval);
            entity.setCalcinval(admeInclinometerInfo.getCalcinval().equals("NullKey") ? "NullKey" : solvingInterval);
            entity.setDormancytime(admeInclinometerInfo.getDormancytime().equals("NullKey") ? "NullKey" : sleepTime);
            decimalFormat.applyPattern("#.####");
            entity.setInterupdate(admeInclinometerInfo.getInterupdate().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(correctionValue)));
            entity.setMode(admeInclinometerInfo.getMode().equals("NullKey") ? "NullKey" : mode);
            entity.setIncversion(admeInclinometerInfo.getIncversion().equals("NullKey") ? "NullKey" : inclinometerVersion);
            entity.setCompenway(admeInclinometerInfo.getCompenway().equals("NullKey") ? "NullKey" : compensateWay);
            entity.setTorangle(admeInclinometerInfo.getTorangle().equals("NullKey") ? "NullKey" : torsionAngle);

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
        inclinometerVersion = admeInclinometerInfo.getIncversion().trim();
        mode = admeInclinometerInfo.getMode().trim();
        compensateWay = admeInclinometerInfo.getCompenway().trim();
        torsionAngle = admeInclinometerInfo.getTorangle().trim();
        address = admeInclinometerInfo.getAddress().trim();
        collectionInterval = admeInclinometerInfo.getCollinval().trim();
        solvingInterval = admeInclinometerInfo.getCalcinval().trim();
        sleepTime = admeInclinometerInfo.getDormancytime().trim();
        correctionValue = admeInclinometerInfo.getInterupdate().trim();

        if (inclinometerVersion.equals("NullKey")) {
            inclinometerVersionLayout.setVisibility(View.GONE);
        } else {
            inclinometerVersionLayout.setVisibility(View.VISIBLE);
            if (inclinometerVersion.equals("0")) {
                mTvInclinometerVersion.setText(versions[0]);
            } else if (inclinometerVersion.equals("1")) {
                mTvInclinometerVersion.setText(versions[1]);
                compensateWayLayout.setVisibility(View.VISIBLE);
                if (compensateWay.equals("0")) {
                    mTvCompensateWay.setText(compensateWays[0]);
                } else if (compensateWay.equals("1")) {
                    mTvCompensateWay.setText(compensateWays[1]);
                    torsionAngleLayout.setVisibility(View.VISIBLE);
                    mEtTorsionAngle.setText(torsionAngle);
                }
            }
        }

        if (mode.equals("NullKey")) {
            lowModeLayout.setVisibility(View.GONE);
        } else {
            if (mode.equals("5")) {
                mTvMode.setText(modes[0]);
            } else if (mode.equals("7")) {
                mTvMode.setText(modes[1]);
            } else if (mode.equals("8")) {
                mTvMode.setText(modes[2]);
            } else if (mode.equals("9")) {
                mTvMode.setText(modes[3]);
            } else {
                mode = "0";
                mTvMode.setText(modes[0]);
            }
        }

        try {
            mEtMacAddress.setText(address);
            macAddressLayout.setVisibility(View.VISIBLE);

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
//        inclinometerTypeOld = inclinometerType;
//        lowPowerModeOld = lowPowerMode;
    }

    public boolean checkValueIsChange(boolean configPageEditableChanged) {
        if (!configPageEditableChanged)
            return false;
//
//        if (inclinometerTypeOld != null && !inclinometerTypeOld.equals("NullKey") && inclinometerType != null && !inclinometerTypeOld.equals(inclinometerType)) {
//            return true;
//        }
//        if (lowPowerModeOld != null && !lowPowerModeOld.equals("NullKey") && lowPowerMode != null && !lowPowerModeOld.equals(lowPowerMode)) {
//            return true;
//        }
//        if (address != null) {
//            if (inclinometerTypeOld.equals("0") && !address.equals(mEtCollectorAddress.getText().toString().trim())) {
//                return true;
//            }
//            if (inclinometerTypeOld.equals("1") && !address.equals(mEtMacAddress.getText().toString().trim())) {
//                return true;
//            }
//        }
//        if (collectionInterval != null && !collectionInterval.equals("NullKey") && !collectionInterval.equals(mEtCollectionInterval.getText().toString().trim())) {
//            return true;
//        }
//        if (solvingInterval != null && !solvingInterval.equals("NullKey") && !solvingInterval.equals(mEtSolvingInterval.getText().toString().trim())) {
//            return true;
//        }
//        if (sleepTime != null && !sleepTime.equals("NullKey") && !sleepTime.equals(mEtSleepTime.getText().toString().trim())) {
//            return true;
//        }
//        if (correctionValue != null && !correctionValue.equals("NullKey") && !correctionValue.equals(mEtCorrectionValue.getText().toString().trim())) {
//            return true;
//        }
        return false;
    }

    public void onEditableChanged(boolean isEditable) {
        inclinometerVersionLayout.setEnabled(isEditable);
        lowModeLayout.setEnabled(isEditable);
        compensateWayLayout.setEnabled(isEditable);
        mEtTorsionAngle.setEnabled(isEditable);
        mEtMacAddress.setEnabled(isEditable);
        mEtCollectionInterval.setEnabled(isEditable);
        mEtSolvingInterval.setEnabled(isEditable);
        mEtSleepTime.setEnabled(isEditable);
        mEtCorrectionValue.setEnabled(isEditable);

        if (isEditable) {
            mTvInclinometerVersion.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mTvMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mTvCompensateWay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mEtTorsionAngle.setHint("[-90,90]");
            mEtMacAddress.setHint("XXXXXXXXXXXX");
            mEtCollectionInterval.setHint("请输入");
            mEtSolvingInterval.setHint("请输入");
            mEtSleepTime.setHint("请输入");
            mEtCorrectionValue.setHint("请输入");
        } else {
            mTvInclinometerVersion.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvCompensateWay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mEtTorsionAngle.setHint("");
            mEtMacAddress.setHint("");
            mEtCollectionInterval.setHint("");
            mEtSolvingInterval.setHint("");
            mEtSleepTime.setHint("");
            mEtCorrectionValue.setHint("");
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
