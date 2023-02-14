package com.shmedo.mcloudapp.deviceconfig.view;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.DataCenterEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.enums.StationCode;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.callback.DataCenterConfigListener;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/1 <br/>
 * 描述：     数据中心高级配置视图
 */
public class DataCenterAdvancedConfigView extends LinearLayout {
    private DataCenterConfigListener dataCenterConfigListener;

    @BindView(R.id.contentLayout)
    ViewGroup contentLayout;

    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbCenterEnable;

    @BindView(R.id.tv_transfer_protocol)
    TextView mTvTransferProtocol;

    @BindView(R.id.tv_data_protocol)
    TextView mTvDataProtocol;

    @BindView(R.id.tv_platform_type)
    TextView mTvPlatformType;

    @BindView(R.id.et_data_server_address)
    ClearEditText mEtDataServerAddress;

    @BindView(R.id.et_data_server_port)
    ClearEditText mEtDataServerPort;

    /**
     * MQTT 协议特有配置参数
     */
    @BindView(R.id.et_device_id)
    ClearEditText mEtDeviceId;

    @BindView(R.id.et_device_key)
    ClearEditText mEtDeviceKey;

    @BindView(R.id.et_device_register_address)
    ClearEditText mEtDeviceRegisterAddress;

    @BindView(R.id.et_device_register_port)
    ClearEditText mEtDeviceRegisterPort;

    @BindView(R.id.et_product_id)
    ClearEditText mEtProductId;

    @BindView(R.id.et_device_register_code)
    ClearEditText mEtDeviceRegisterCode;

    /**
     * SL651水文协议特有配置参数
     */
    @BindView(R.id.tv_station_classification)
    TextView mTvStationClassification;

    @BindView(R.id.et_center_station_addr)
    ClearEditText mEtCenterStationAddr;

    @BindView(R.id.et_password)
    ClearEditText mEtPassword;

    @BindView(R.id.et_telemetry_station_addr)
    ClearEditText mEtTelemetryStationAddr;

    @BindView(R.id.hourlyReportEnableSBtn)
    SwitchButton mSbHourlyReportEnable;

    @BindView(R.id.et_data_link_maintenance)
    ClearEditText mEtDataLinkMaintenance;

    @BindView(R.id.et_reissuing_data_valid_days)
    ClearEditText mEtReissuingDataValidDays;

    @BindView(R.id.et_reissuing_data_interval)
    ClearEditText mEtReissuingDataInterval;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_data_protocol)
    ViewGroup dataProtocolLayout;

    @BindView(R.id.ll_mqtt_child_items)
    ViewGroup mqttChildItemsLayout;

    @BindView(R.id.ll_sl651_child_items)
    ViewGroup sl651ChildItemsLayout;

    @BindView(R.id.ll_station_classification)
    ViewGroup stationClassificationLayout;

    @BindView(R.id.rl_hourly_report)
    ViewGroup hourlyReportLayout;

    @BindView(R.id.ll_data_link_maintenance)
    ViewGroup dataLinkMaintenanceLayout;

    @BindView(R.id.ll_reissuing_data_valid_days)
    ViewGroup reissuingDataValidDaysLayout;

    @BindView(R.id.ll_reissuing_data_interval)
    ViewGroup reissuingDataIntervalLayout;

    private ProductType productType = ProductType.DAS;
    private ServerNumber serverNumber;
    public DataCenterInfo dataCenterInfo;

    private String transferProtocol;// 传输协议
    private String dataProtocol;//数据协议
    private String platformType;//平台类型
    private String dataServerAddress;//数据服务器地址
    private String dataServerPort;//数据服务器端口
    /**
     * MQTT 协议特有配置参数
     */
    private String deviceId;//设备 Id
    private String deviceKey;//设备Key
    private String registerAddress;//注册地址
    private String registerPort;//注册端口
    private String productId;//产品 Id
    private String registerCode;//注册码
    /**
     * SL651 水文协议特有配置参数
     */
    private String stationCode;//测站编码
    private String centerStationAddr;//中心站地址
    private String password;//密码
    private String telemetryStationAddr;//遥测站地址
    private String dataLinkMaintenance;//数据链路维持报 0|[10,40]
    private String reissuingDataValidDays;//补发数据有效天数
    private String reissuingDataInterval;//数据补发间隔


    public boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭数据中心操作
    public boolean isResultOK = false;//返回的结果是否是 Activity.RESULT_OK

    public DataCenterAdvancedConfigView(Context context) {
        this(context, null);
    }

    public DataCenterAdvancedConfigView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataCenterAdvancedConfigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.data_center_advanced_config_view, this, true);
        ButterKnife.bind(this);
        initView();
        setSwitchViewListener();
    }

    private void initView() {
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!"0123456789".contains(source.charAt(i) + "")) {
                        return "";
                    }
                }
                return null;
            }
        };
        mEtDataServerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDataServerPort.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtDeviceId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceKey.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceRegisterAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceRegisterPort.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtProductId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtDeviceRegisterCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});

        mEtCenterStationAddr.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5), filter});
        mEtTelemetryStationAddr.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), filter});
        mEtDataLinkMaintenance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2), filter});
        mEtDataLinkMaintenance.setHint("0或者[10,40]");
        mEtReissuingDataValidDays.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3), filter});
        mEtReissuingDataInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4), filter});

        mTvTransferProtocol.setText("MQTT");
        transferProtocol = "MQTT";

        mTvPlatformType.setText("地灾一期");
        platformType = "0";

        //SL651 水文协议测站分类默认编码
        mTvStationClassification.setText(StationCode.PRECIPITATION.getDescription());
        stationCode = StationCode.PRECIPITATION.getCode();
    }

    public void initData(ProductType productType, ServerNumber serverNumber, String serverStatus) {
        this.productType = productType;
        this.serverNumber = serverNumber;
        //数据中心地址为空表示数据中心未启用
        if (!TextUtils.isEmpty(serverStatus) && serverStatus.contains("未开启")) {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
            contentLayout.setVisibility(View.GONE);
        } else {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
            contentLayout.setVisibility(View.VISIBLE);
        }

        if (productType == ProductType.DAS || productType == ProductType.ADME || productType == ProductType.VMS) {
            dataProtocol = "";
            dataProtocolLayout.setVisibility(View.GONE);
        }
    }

    private void setSwitchViewListener() {
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (dataCenterConfigListener != null) {
                    if (!dataCenterConfigListener.onCheckConnect()) {
                        ToastUtils.show("设备已断开连接，暂无法进行操作");
                        mSbCenterEnable.setCheckedImmediatelyNoEvent(!isChecked);
                        return;
                    }
                }
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭数据中心？");
                } else {
                    contentLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title("温馨提示：")
                .content(content)
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
                        contentLayout.setVisibility(View.GONE);
                        //关闭服务器
                        closeDataServer();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 关闭数据中心</br>
     * addr和port设置为空时，关闭该数据中心
     */
    private void closeDataServer() {
        DataCenterEntity dataCenterEntity = new DataCenterEntity();
        dataCenterEntity.setServerNumber(serverNumber);
        dataCenterEntity.setAddr("");
        dataCenterEntity.setPort("");

        isSaveParamOperation = false;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
        if (dataCenterConfigListener != null)
            dataCenterConfigListener.onCloseDataServer(command);
    }

    @OnClick({R.id.ll_transfer_protocol, R.id.ll_data_protocol, R.id.ll_platform_type, R.id.ll_station_classification, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_transfer_protocol) {
            showTransferProtocolDialog();

        } else if (id == R.id.ll_data_protocol) {
            showDataProtocolDialog();

        } else if (id == R.id.ll_platform_type) {
            showPlatformTypeDialog();

        } else if (id == R.id.ll_station_classification) {
            showStationCodeDialog();

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (dataCenterConfigListener != null) {
                if (!dataCenterConfigListener.onCheckConnect()) {
                    ToastUtils.show("设备已断开连接，暂无法进行操作");
                    return;
                }
            }
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    /**
     * 选择传输协议弹框
     */
    private void showTransferProtocolDialog() {
        final String[] protocols = (productType == ProductType.DAS || productType == ProductType.ADME) ?
                new String[]{"TCP-C", "MQTT", "SL651"} : new String[]{"TCP-C", "TCP-S", "MQTT"};
        int pos = Arrays.asList(protocols).indexOf(String.valueOf(transferProtocol));
        pos = (pos == -1) ? 0 : pos;

        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(getContext())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", protocols,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                transferProtocol = text;
                                mTvTransferProtocol.setText(text);

                                if (text.contains("MQTT")) {
                                    mqttChildItemsLayout.setVisibility(View.VISIBLE);
                                    sl651ChildItemsLayout.setVisibility(View.GONE);
                                } else if (text.contains("SL651")) {
                                    mqttChildItemsLayout.setVisibility(View.GONE);
                                    sl651ChildItemsLayout.setVisibility(View.VISIBLE);
                                } else {
                                    mqttChildItemsLayout.setVisibility(View.GONE);
                                    sl651ChildItemsLayout.setVisibility(View.GONE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择数据协议弹框
     */
    private void showDataProtocolDialog() {
        final String[] protocols = new String[]{"CMD", "NMEA", "DIFF_IN", "DIFF_OUT", "RAW_OUT", "RES_OUT"};
        int pos = Arrays.asList(protocols).indexOf(mTvDataProtocol.getText().toString());
        pos = (pos == -1) ? 0 : pos;

        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(getContext())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", protocols,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvDataProtocol.setText(text);
                                switch (text) {
                                    case "CMD":
                                        dataProtocol = "1";
                                        break;

                                    case "NMEA":
                                        dataProtocol = "2";
                                        break;

                                    case "DIFF_IN":
                                        dataProtocol = "3";
                                        break;

                                    case "DIFF_OUT":
                                        dataProtocol = "4";
                                        break;

                                    case "RAW_OUT":
                                        dataProtocol = "5";
                                        break;

                                    case "RES_OUT":
                                        dataProtocol = "6";
                                        break;
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择平台类型弹框
     */
    private void showPlatformTypeDialog() {
        final String[] platforms = new String[]{"地灾一期", "成都理工平台", "MDNET", "地灾二期"};
        int pos = Arrays.asList(platforms).indexOf(mTvPlatformType.getText().toString());
        pos = (pos == -1) ? 0 : pos;

        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(getContext())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", platforms,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvPlatformType.setText(text);
                                switch (text) {
                                    case "地灾一期":
                                        platformType = "0";
                                        break;

                                    case "成都理工平台":
                                        platformType = "1";
                                        break;

                                    case "MDNET":
                                        platformType = "2";
                                        break;

                                    case "地灾二期":
                                        platformType = "3";
                                        break;
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择测站分类编码弹框
     */
    private void showStationCodeDialog() {
        final String[] platforms = StationCode.getDescriptions().toArray(new String[0]);
        int pos = Arrays.asList(platforms).indexOf(mTvStationClassification.getText().toString());
        pos = (pos == -1) ? 0 : pos;

        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(getContext())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", platforms,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvStationClassification.setText(text);
                                stationCode = StationCode.valueByName(text).getCode();
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private boolean checkValueIsValid() {
        dataServerAddress = mEtDataServerAddress.getText().toString();
        dataServerPort = mEtDataServerPort.getText().toString();
        //MQTT 协议参数
        deviceId = mEtDeviceId.getText().toString();
        deviceKey = mEtDeviceKey.getText().toString();
        registerAddress = mEtDeviceRegisterAddress.getText().toString();
        registerPort = mEtDeviceRegisterPort.getText().toString();
        productId = mEtProductId.getText().toString();
        registerCode = mEtDeviceRegisterCode.getText().toString();
        //SL651 水文协议参数
        centerStationAddr = mEtCenterStationAddr.getText().toString();
        password = mEtPassword.getText().toString();
        telemetryStationAddr = mEtTelemetryStationAddr.getText().toString();
        dataLinkMaintenance = mEtDataLinkMaintenance.getText().toString();
        reissuingDataValidDays = mEtReissuingDataValidDays.getText().toString();
        reissuingDataInterval = mEtReissuingDataInterval.getText().toString();


        if (!TextUtils.isEmpty(dataServerPort)) {
            try {
                int port = Integer.parseInt(dataServerPort);
                if (port < 0 || port > 65535) {
                    ToastUtils.show("请输入正确的数据中心端口号!");
                    mEtDataServerPort.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据中心端口号!");
                mEtDataServerPort.requestFocus();
                return false;
            }
        }

        if (!TextUtils.isEmpty(registerPort)) {
            try {
                int port = Integer.parseInt(registerPort);
                if (port < 0 || port > 65535) {
                    ToastUtils.show("请输入正确的设备注册端口号!");
                    mEtDeviceRegisterPort.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的设备注册端口号!");
                mEtDeviceRegisterPort.requestFocus();
                return false;
            }
        }

        if (!TextUtils.isEmpty(dataLinkMaintenance)) {
            try {
                int value = Integer.parseInt(dataLinkMaintenance);
                if (value != 0 && (value < 10 || value > 40)) {
                    ToastUtils.show("请输入正确的数据链路维持报!");
                    mEtDataLinkMaintenance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据链路维持报!");
                mEtDataLinkMaintenance.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(reissuingDataValidDays)) {
            try {
                int value = Integer.parseInt(reissuingDataValidDays);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的补发数据有效天数!");
                mEtReissuingDataValidDays.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(reissuingDataInterval)) {
            try {
                int value = Integer.parseInt(reissuingDataInterval);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据补发间隔!");
                mEtReissuingDataInterval.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * 配置参数指令
     */
    private void processSave() {
        DataCenterEntity dataCenterEntity = new DataCenterEntity();
        dataCenterEntity.setServerNumber(serverNumber);
        dataCenterEntity.setProtocol(transferProtocol);
        dataCenterEntity.setDatatype(dataProtocol);
        dataCenterEntity.setPlattype(platformType);
        dataCenterEntity.setAddr(dataServerAddress);
        dataCenterEntity.setPort(dataServerPort);
        if (transferProtocol != null && transferProtocol.equals("MQTT")) {//MQTT自动注册
            dataCenterEntity.setDeviceid(deviceId);
            dataCenterEntity.setDevicekey(deviceKey);
            dataCenterEntity.setHttpaddr(registerAddress);
            dataCenterEntity.setHttpport(registerPort);
            dataCenterEntity.setProjid(productId);
            dataCenterEntity.setRegcode(registerCode);
        } else if (transferProtocol != null && transferProtocol.equals("SL651")) {
            dataCenterEntity.setType_code(stationCode);
            dataCenterEntity.setCo_address(centerStationAddr);
            dataCenterEntity.setPassword(password);
            dataCenterEntity.setTaddress(telemetryStationAddr);
            dataCenterEntity.setHour_report(mSbHourlyReportEnable.isChecked() ? "1" : "0");
            dataCenterEntity.setData_link(dataLinkMaintenance);
            dataCenterEntity.setValid_day(reissuingDataValidDays);
            dataCenterEntity.setReissue_time(reissuingDataInterval);
        }
        isSaveParamOperation = true;

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SET_DATA_CENTER, dataCenterEntity);
        if (dataCenterConfigListener != null)
            dataCenterConfigListener.onSaveConfig(command);
    }

    public void initDataCenterData() {
        if (dataCenterInfo == null) {
            Timber.e("DataCenterInfo 为空!");
            dataCenterInfo = new DataCenterInfo();
            return;
        }
        transferProtocol = dataCenterInfo.getProtocol();
        dataProtocol = dataCenterInfo.getDatatype();
        platformType = dataCenterInfo.getPlattype();
        dataServerAddress = dataCenterInfo.getAddr();
        dataServerPort = dataCenterInfo.getPort();

        //MQTT 协议参数
        deviceId = dataCenterInfo.getDeviceid();
        deviceKey = dataCenterInfo.getDevicekey();
        registerAddress = dataCenterInfo.getHttpaddr();
        registerPort = dataCenterInfo.getHttpport();
        productId = dataCenterInfo.getProjid();
        registerCode = dataCenterInfo.getRegcode();

        //SL651 水文协议参数
        stationCode = dataCenterInfo.getType_code();
        centerStationAddr = dataCenterInfo.getCo_address();
        password = dataCenterInfo.getPassword();
        telemetryStationAddr = dataCenterInfo.getTaddress();
        dataLinkMaintenance = dataCenterInfo.getData_link();
        reissuingDataValidDays=dataCenterInfo.getValid_day();
        reissuingDataInterval=dataCenterInfo.getReissue_time();

        //传输协议
        mTvTransferProtocol.setText(transferProtocol);
        if (transferProtocol.contains("MQTT")) {
            mqttChildItemsLayout.setVisibility(View.VISIBLE);
            sl651ChildItemsLayout.setVisibility(View.GONE);
        } else if (transferProtocol.contains("SL651")) {
            mqttChildItemsLayout.setVisibility(View.GONE);
            sl651ChildItemsLayout.setVisibility(View.VISIBLE);
        } else {
            mqttChildItemsLayout.setVisibility(View.GONE);
            sl651ChildItemsLayout.setVisibility(View.GONE);
        }
        //数据协议
        switch (dataProtocol) {
            case "1":
                mTvDataProtocol.setText("CMD");
                break;

            case "2":
                mTvDataProtocol.setText("NMEA");
                break;

            case "3":
                mTvDataProtocol.setText("DIFF_IN");
                break;

            case "4":
                mTvDataProtocol.setText("DIFF_OUT");
                break;

            case "5":
                mTvDataProtocol.setText("RAW_OUT");
                break;

            case "6":
                mTvDataProtocol.setText("RES_OUT");
                break;
        }
        //平台
        switch (platformType) {
            case "0":
                mTvPlatformType.setText("地灾一期");
                break;
            case "1":
                mTvPlatformType.setText("成都理工平台");
                break;

            case "2":
                mTvPlatformType.setText("MDNET");
                break;

            case "3":
                mTvPlatformType.setText("地灾二期");
                break;
        }
        mEtDataServerAddress.setText(dataServerAddress);
        mEtDataServerPort.setText(dataServerPort);

        mEtDeviceId.setText(deviceId);
        mEtDeviceKey.setText(deviceKey);
        mEtDeviceRegisterAddress.setText(registerAddress);
        mEtDeviceRegisterPort.setText(registerPort);
        mEtProductId.setText(productId);
        mEtDeviceRegisterCode.setText(registerCode);

        mTvStationClassification.setText(StationCode.valueByCode(stationCode).getDescription());
        mEtCenterStationAddr.setText(centerStationAddr);
        mEtPassword.setText(password);
        mEtTelemetryStationAddr.setText(telemetryStationAddr);
        mSbHourlyReportEnable.setCheckedImmediatelyNoEvent(dataCenterInfo.getHour_report().equals("1"));
        mEtDataLinkMaintenance.setText(dataLinkMaintenance);
        mEtReissuingDataValidDays.setText(reissuingDataValidDays);
        mEtReissuingDataInterval.setText(reissuingDataInterval);
    }

    public void doAfterSetting() {
        isResultOK = true;
    }

    public boolean checkValueIsChange() {
        return false;
    }

    public void setDataCenterConfigListener(DataCenterConfigListener listener) {
        dataCenterConfigListener = listener;
    }

}
