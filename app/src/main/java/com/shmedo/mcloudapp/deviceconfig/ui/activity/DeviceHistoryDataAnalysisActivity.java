package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieHistoryState;
import com.shmedo.mcloudapp.deviceconfig.model.params.QueryCmdStateParam;
import com.shmedo.mcloudapp.entity.PageResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.DateUtil;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.text.DecimalFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class DeviceHistoryDataAnalysisActivity extends BaseActivity {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvTime;

    @BindView(R.id.tv_device_communication_state_flag)
    TextView mTvDeviceCommunicationState;//通信状态(在线、离线、已连接、已断开)

    @BindView(R.id.rl_device_connect_state)
    View deviceConnectStateLayout;

    @BindView(R.id.tv_iot_card_number)
    TextView mTvIotCardNum;

    @BindView(R.id.tv_4g_signal)
    TextView mTv4gSignal;

    @BindView(R.id.tv_power)
    TextView mTvPower;

    @BindView(R.id.tv_external_voltage)
    TextView mTvExternalVoltage;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    @BindView(R.id.tv_location)
    TextView mTvLocation;

    @BindView(R.id.tv_sensor_status)
    TextView mTvSensorStatus;

    private static final int PAGE_SIZE = 5;
    private int companyID;

    private ProjectDeviceInfo projectDeviceInfo;

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, DeviceHistoryDataAnalysisActivity.class);
        intent.putExtra(DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_run_analysis;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        parseIntent();
        setHeadInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (companyID != MCloudApp.getCompanyID()) {
            companyID = MCloudApp.getCompanyID();
            queryCmdState();
        }
    }

    private void initView() {
        mToolbarTitle.setText("数据分析");
        mTvDeviceSn.setVisibility(View.GONE);
        deviceConnectStateLayout.setVisibility(View.GONE);
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(DEVICE_INFO);
        }
    }

    private void setHeadInfo() {
        if (projectDeviceInfo != null) {
            mTvDeviceName.setText(TextUtils.isEmpty(projectDeviceInfo.getName()) ? "" : projectDeviceInfo.getName());
            mTvProductModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "" : projectDeviceInfo.getDeviceTypeName()));
            mTvTime.setText(String.format("传输时间：%s", TextUtils.isEmpty(projectDeviceInfo.getLastActiveTime()) ? "" : projectDeviceInfo.getLastActiveTime()));
            if (projectDeviceInfo.isOnline()) {
                mTvDeviceCommunicationState.setText("在线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(this, R.color.text_color_50E9B9));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceCommunicationState.setText("离线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(this, R.color.sub_title_text_color));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
    }


    @OnClick({R.id.iotCardLayout, R.id.signalLayout, R.id.powerLayout, R.id.externalVoltageLayout, R.id.firmwareVersionLayout, R.id.locationLayout, R.id.sensorStatusLayout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iotCardLayout:
                break;

            case R.id.signalLayout:
                break;

            case R.id.powerLayout:
                break;

            case R.id.externalVoltageLayout:
                break;

            case R.id.firmwareVersionLayout:
                break;

            case R.id.locationLayout:
                break;

            case R.id.sensorStatusLayout:
                break;
        }
    }


    /**
     * 查询设备状态历史
     */
    private void queryCmdState() {
        showLoadingDialog("加载中...");

        String end = TextUtils.isEmpty(projectDeviceInfo.getLastActiveTime()) ? DateUtil.getNowDateString() : projectDeviceInfo.getLastActiveTime();
        Date beginDate = DateUtil.getBackOrAddDate2(DateUtil.stringToDate(end, "yyyy-MM-dd HH:mm:ss"), -5);
        String begin = DateUtil.DateToStrFormat(beginDate, "yyyy-MM-dd HH:mm:ss");

        QueryCmdStateParam parameter = new QueryCmdStateParam();
        parameter.setCompanyID(companyID);
        parameter.setDeviceID(projectDeviceInfo.getId());
        parameter.setBegin(begin);
        parameter.setEnd(end);
        parameter.setPageSize(PAGE_SIZE);
        parameter.setCurrentPage(1);

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryCmdState(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<DevcieHistoryState>>() {
                    @Override
                    protected void onResponse(PageResult<DevcieHistoryState> data, ErrCode errCode) {
                        dismissLoadingDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    return;
                                }
                                updateDeviceState(data.getCurrentPageData().get(0));
                            }else{
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    private void updateDeviceState(DevcieHistoryState devcieHistoryState) {
        if (devcieHistoryState != null) {
            DecimalFormat df = new DecimalFormat("#.#");//格式化小数
            String power = df.format(devcieHistoryState.getBatteryVolt() * 100) + "%";
            String extPowerVolt = df.format(devcieHistoryState.getExtPowerVolt()) + "V";

            mTvIotCardNum.setText("--");
            mTv4gSignal.setText(String.format("%sdBm", devcieHistoryState.getFourGSignal()));
            mTvPower.setText("--");
            mTvExternalVoltage.setText(extPowerVolt);
            mTvFirmwareVersion.setText(TextUtils.isEmpty(devcieHistoryState.getSwVersion()) ? "--" : devcieHistoryState.getSwVersion());
            mTvLocation.setText(TextUtils.isEmpty(devcieHistoryState.getLocation()) ? "--" : devcieHistoryState.getLocation());
            mTvSensorStatus.setText(TextUtils.isEmpty(devcieHistoryState.getSensorErrno()) ? "--" : "解析中...");
        }
    }
}
