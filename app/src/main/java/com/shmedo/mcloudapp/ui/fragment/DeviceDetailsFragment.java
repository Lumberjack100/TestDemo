package com.shmedo.mcloudapp.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hjq.toast.ToastUtils;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.entity.devicedetails.DeviceInternetStatus;
import com.shmedo.mcloudapp.entity.devicedetails.DeviceStatusOne;
import com.shmedo.mcloudapp.entity.devicedetails.DeviceStatusThree;
import com.shmedo.mcloudapp.entity.devicedetails.DeviceStatusTwo;
import com.shmedo.mcloudapp.entity.devicedetails.DeviceVersionInfo;
import com.shmedo.mcloudapp.entity.devicedetails.OperatorInformation;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.util.ParserDeviceDetailsUtils;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.views.VerticalSwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   DeviceDetailsFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:41
 * 描述：   设备详情
 */
public class DeviceDetailsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.sim_card_number)
    TextView simCardNumber;

    @BindView(R.id.imei_number)
    TextView imeiNumber;

    @BindView(R.id.device_start_code)
    TextView deviceStartCode;

    @BindView(R.id.firmware_version)
    TextView firmwareVersion;

    @BindView(R.id.signal_strength)
    TextView signalStrength;

    @BindView(R.id.link_one_status)
    TextView linkOneStatus;

    @BindView(R.id.link_one_send_data)
    TextView linkOneSendData;

    @BindView(R.id.link_two_status)
    TextView linkTwoStatus;

    @BindView(R.id.link_two_send_data)
    TextView linkTwoSendData;

    @BindView(R.id.link_three_status)
    TextView linkThreeStatus;

    @BindView(R.id.link_three_send_data)
    TextView linkThreeSendData;

    @BindView(R.id.link_one_unsend_data)
    TextView linkOneUnsendData;

    @BindView(R.id.link_two_unsend_data)
    TextView linkTwoUnsendData;

    @BindView(R.id.link_three_unsend_data)
    TextView linkThreeUnsendData;

    @BindView(R.id.solar_status)
    TextView solarStatus;

    @BindView(R.id.solar_voltage)
    TextView solarVoltage;

    @BindView(R.id.battery_voltage)
    TextView batteryVoltage;

    @BindView(R.id.rifa_battery)
    TextView rifaBattery;

    @BindView(R.id.rihao_battery)
    TextView rihaoBattery;

    @BindView(R.id.case_internal_status)
    TextView caseInternalStatus;

    @BindView(R.id.case_internal_temperature)
    TextView caseInternalTemperature;

    @BindView(R.id.case_internal_humidity)
    TextView caseInternalHumidity;

    @BindView(R.id.case_external_status)
    TextView caseExternalStatus;

    @BindView(R.id.case_external_temperature)
    TextView caseExternalTemperature;

    @BindView(R.id.case_external_humidity)
    TextView caseExternalHumidity;

    @BindView(R.id.internal_battery)
    TextView internalBattery;

    @BindView(R.id.external_battery)
    TextView externalBattery;

    @BindView(R.id.sensor_recycle)
    RecyclerView sensorRecycle;

    @BindView(R.id.rain_data)
    TextView rainData;

    @BindView(R.id.alarm_status)
    TextView alarmStatus;

    @BindView(R.id.ll_sensor)
    LinearLayout llSensor;

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @BindView(R.id.refresh)
    VerticalSwipeRefreshLayout refreshLayout;

    @BindView(R.id.switch_type)
    TextView switchType;

    @BindView(R.id.ll_rain)
    LinearLayout llRain;

    @BindView(R.id.ll_alarm)
    LinearLayout llAlarm;

    @BindView(R.id.tv_channel_number)
    TextView tvChannelNumber;

    @BindView(R.id.img_signal_strength)
    ImageView imgSignalStrength;

    //在线率
    @BindView(R.id.link_one_online_rate)
    TextView linkOneOnlineRate;

    @BindView(R.id.link_two_online_rate)
    TextView linkTwoOnlineRate;

    @BindView(R.id.link_three_online_rate)
    TextView linkThreeOnlineRate;
    //安装位置
    @BindView(R.id.tv_install_position)
    TextView tvInstallPosition;

    private boolean onRefreshFirst = false;
    private long prelongTim = 0;

    private BaseDeviceConnectActivity deviceConnectActivity;

    private List<String> sensorList = new ArrayList<>();
    private CommonAdapter adapter;

    private String channelNumber = "";

    @Override
    protected int initContentView() {
        return R.layout.fragment_device_details;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initView();
        initData();
        initAdapter();
        return view;
    }

    private void initView() {
        deviceConnectActivity = (BaseDeviceConnectActivity) getActivity();
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        refreshLayout.setSize(SwipeRefreshLayout.LARGE);
        refreshLayout.setScrollUpChild(scrollView);
        refreshLayout.setOnRefreshListener(this);
    }

    private void initData() {
        deviceConnectActivity.sendCommonCommandImmediately("##040\r\n");
        Timber.i("查询设备版本信息：==##040");
    }


    private void initAdapter() {
        sensorRecycle.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CommonAdapter<String>(getActivity(), R.layout.item_device_details, sensorList) {
            @Override
            protected void convert(ViewHolder holder, String string, int position) {
                //①:②:③，其中①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据
                String[] result = string.split(":");
                holder.setText(R.id.sensor_channel_number, result[0]);
                holder.setText(R.id.sensor_status, ParserDeviceDetailsUtils.setSensorDataStatus(holder.getView(R.id.sensor_status), result[1], deviceConnectActivity));
                if (channelNumber.equals("3")) {
                    holder.setText(R.id.sensor_data, result[2] + "%rh");
                } else if (channelNumber.equals("21")) {
                    holder.setText(R.id.sensor_data, result[2] + "Hz");
                } else {
                    holder.setText(R.id.sensor_data, result[2] + "mm");
                }
            }
        };

        sensorRecycle.setAdapter(adapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (TextUtils.isEmpty(messageEvent) || !messageEvent.startsWith("$$")) {
            return;
        }
        if (messageEvent.startsWith("$$040") ||
                messageEvent.startsWith("$$041") ||
                messageEvent.startsWith("$$042") ||
                messageEvent.startsWith("$$043") ||
                messageEvent.startsWith("$$044") ||
                messageEvent.startsWith("$$014") ||
                messageEvent.startsWith("$$916")) {
            setResultData(messageEvent);
        }
    }

    private void setResultData(String message) {
        Timber.i("DeviceDetailsFragment：======" + message);
        String commandType = StringUtil.extractCommandType(message);
        switch (commandType) {
            case "040":
                DeviceVersionInfo versionInfo = ParserDeviceDetailsUtils.parserVersionInfo(message);
                setVersionInfo(versionInfo);
                deviceConnectActivity.sendCommonCommandImmediately("##9162\r\n");
                Timber.i("查询安装位置：==##9162");
                break;

            case "916":
                String position = ParserDeviceDetailsUtils.parserInstallPosition(message);
                tvInstallPosition.setText(position);
                deviceConnectActivity.sendCommonCommandImmediately("##041\r\n");
                Timber.i("查询设备状态1：==##041");
                break;

            case "041":
                DeviceStatusOne statusOne = ParserDeviceDetailsUtils.parserDeviceStatusOne(message);
                setDeviceStatusOne(statusOne);
                deviceConnectActivity.sendCommonCommandImmediately("##014\r\n");
                Timber.i("查询运营商信息：==##014");
                break;

            case "014":
                OperatorInformation operatorInformation = ParserDeviceDetailsUtils.parserOperatorInformation(message);
                setOperatorInformation(operatorInformation);
                deviceConnectActivity.sendCommonCommandImmediately("##0441\r\n");
                Timber.i("查询网络状态：中心1==##0441");
                break;

            case "044":
                String number = message.replace(CommandResult.COMMAND_RESULT_HEADER, "").substring(3, 4);
                switch (number) {
                    case "1":
                        deviceConnectActivity.sendCommonCommandImmediately("##0442\r\n");
                        Timber.i("查询网络状态：中心2==##0442");
                        DeviceInternetStatus internetStatus1 = ParserDeviceDetailsUtils.parserInternetStatus(message);
                        setInternetStatus(internetStatus1, number);
                        break;

                    case "2":
                        deviceConnectActivity.sendCommonCommandImmediately("##0443\r\n");
                        Timber.i("查询网络状态：中心3==##0443");
                        DeviceInternetStatus internetStatus2 = ParserDeviceDetailsUtils.parserInternetStatus(message);
                        setInternetStatus(internetStatus2, number);
                        break;

                    case "3":
                        DeviceInternetStatus internetStatus3 = ParserDeviceDetailsUtils.parserInternetStatus(message);
                        setInternetStatus(internetStatus3, number);
                        deviceConnectActivity.sendCommonCommandImmediately("##042\r\n");
                        Timber.i("查询设备状态2：：==##042");
                        break;
                }
                break;

            case "042":
                deviceConnectActivity.sendCommonCommandImmediately("##043\r\n");
                Timber.i("查询设备状态3：==##043");
                DeviceStatusTwo statusTwo = ParserDeviceDetailsUtils.parserDeviceStatusTwo(message);
                setDeviceStatusTwo(statusTwo);
                break;

            case "043":
                DeviceStatusThree statusThree = ParserDeviceDetailsUtils.parserDeviceStatusThree(message);
                channelNumber = statusThree.getCollectorModel();
                setDeviceStatusThree(statusThree);
                break;
        }
    }


    //设置设备版本信息  ##040
    private void setVersionInfo(DeviceVersionInfo versionInfo) {
        firmwareVersion.setText(versionInfo.getFirmwareVersion());

    }

    //设置设备状态1  ##041
    private void setDeviceStatusOne(DeviceStatusOne statusOne) {
        mTvDeviceSn.setText(statusOne.getSnNumber());
        simCardNumber.setText(statusOne.getSimNumber());
        imeiNumber.setText(statusOne.getImeiNumber());

        StringBuilder stringBuilder = new StringBuilder();
        String startCode1 = StringUtil.formatStringTwo(statusOne.getStartCodeOne());
        String startCode2 = StringUtil.formatStringTwo(statusOne.getStartCodeTwo());
        deviceStartCode.setText(stringBuilder.append(startCode1).append(startCode2).toString());
    }

    //设置设备状态2  ##042
    private void setDeviceStatusTwo(DeviceStatusTwo statusTwo) {
        //太阳能控制器
        ParserDeviceDetailsUtils.setDeviceStatus(solarStatus, statusTwo.getSolarControllerStatus(), deviceConnectActivity);
        //solarStatus.setText(statusTwo.getSolarControllerStatus());
        solarVoltage.setText(statusTwo.getSolarPanelVoltage() + "V");
        batteryVoltage.setText(statusTwo.getBatteryVoltage() + "V");
        rifaBattery.setText(statusTwo.getDailyPowerGeneration() + "W");
        rihaoBattery.setText(statusTwo.getDailyPowerConsumption() + "W");

        //机箱内部温湿度
        ParserDeviceDetailsUtils.setDeviceStatus(caseInternalStatus, statusTwo.getInternalTempHumidityStatus(), deviceConnectActivity);
        //caseInternalStatus.setText(statusTwo.getInternalTempHumidityStatus());
        caseInternalTemperature.setText(statusTwo.getInternalTemperature() + "°");
        caseInternalHumidity.setText(statusTwo.getInternalHumidity() + "%");

        //机箱外部温湿度
        ParserDeviceDetailsUtils.setDeviceStatus(caseExternalStatus, statusTwo.getExternalTempHumidityStatus(), deviceConnectActivity);
        //caseExternalStatus.setText(statusTwo.getExternalTempHumidityStatus());
        caseExternalTemperature.setText(statusTwo.getExternalTemperature() + "°");
        caseExternalHumidity.setText(statusTwo.getExternalHumidity() + "%");
        //设备电量、电压
        internalBattery.setText(ParserDeviceDetailsUtils.setDeviceInternalBattery(statusTwo.getInternalVoltage()));
        externalBattery.setText(statusTwo.getExternalVoltage() + "V");

        //雨量值 断线报警器状态
        //根据开关量类型来判断降雨量和断线报警器
        String type = statusTwo.getSwitchType();
        switch (type) {
            case "1":
                switchType.setText("雨量计");
                llAlarm.setVisibility(View.GONE);
                llRain.setVisibility(View.VISIBLE);
                rainData.setText(statusTwo.getRainfallStatus() + "mm");
                break;
            case "2":
                switchType.setText("关闭");
                llAlarm.setVisibility(View.GONE);
                llRain.setVisibility(View.GONE);
                break;
            case "3":
                switchType.setText("断线报警器");
                llAlarm.setVisibility(View.VISIBLE);
                llRain.setVisibility(View.GONE);
                if (statusTwo.getRainfallStatus().equals("1.0") || statusTwo.getRainfallStatus().equals("1")) {
                    alarmStatus.setText("已断线");
                    alarmStatus.setTextColor(Color.RED);
                } else if (statusTwo.getRainfallStatus().equals("0.0") || statusTwo.getRainfallStatus().equals("0")) {
                    alarmStatus.setText("未断线");
                    alarmStatus.setTextColor(getResources().getColor(R.color.green_53a659));
                }
                break;
        }
    }

    //设置设备状态3  $$043,150000L,2,   3:0:3.1,   5:0:3.1\r\n
    private void setDeviceStatusThree(DeviceStatusThree statusThree) {
        ParserDeviceDetailsUtils.setChannelNumber(tvChannelNumber, statusThree.getCollectorModel());
        if (statusThree.getCollectorAddress().equals("0")) {
            llSensor.setVisibility(View.GONE);
        } else {
            llSensor.setVisibility(View.VISIBLE);
            List<String> list = statusThree.getSensorStatus();
            sensorList.clear();
            sensorList.addAll(list);
            adapter.notifyDataSetChanged();
        }
    }

    //设置网络状态  ##044
    private void setInternetStatus(DeviceInternetStatus internetStatus, String linkNumber) {
        switch (linkNumber) {
            case "1":
                ParserDeviceDetailsUtils.setLinkStatus(linkOneStatus, linkOneSendData, linkOneUnsendData, internetStatus.getLinkStatus(),
                        internetStatus.getLinkEnable(), internetStatus.getSentData(), internetStatus.getGeneratedData(), deviceConnectActivity);
                if (internetStatus.getOnlineRate() != null) {
                    linkOneOnlineRate.setText(internetStatus.getOnlineRate() + "%");
                }
                break;
            case "2":
                ParserDeviceDetailsUtils.setLinkStatus(linkTwoStatus, linkTwoSendData, linkTwoUnsendData, internetStatus.getLinkStatus(),
                        internetStatus.getLinkEnable(), internetStatus.getSentData(), internetStatus.getGeneratedData(), deviceConnectActivity);
                if (internetStatus.getOnlineRate() != null)
                    linkTwoOnlineRate.setText(internetStatus.getOnlineRate() + "%");
                break;
            case "3":
                ParserDeviceDetailsUtils.setLinkStatus(linkThreeStatus, linkThreeSendData, linkThreeUnsendData, internetStatus.getLinkStatus(),
                        internetStatus.getLinkEnable(), internetStatus.getSentData(), internetStatus.getGeneratedData(), deviceConnectActivity);
                if (internetStatus.getOnlineRate() != null)
                    linkThreeOnlineRate.setText(internetStatus.getOnlineRate() + "%");
                break;
        }
    }

    //设置运营商信息  ##014
    private void setOperatorInformation(OperatorInformation operatorInformation) {
        ParserDeviceDetailsUtils.setSignalStrength(imgSignalStrength, operatorInformation.getSignalStrength());
        signalStrength.setText(ParserDeviceDetailsUtils.setOperatorType(operatorInformation.getOperatorType()));

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onRefresh() {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            refreshLayout.setRefreshing(false);
            ToastUtils.show("设备已断开连接,无法刷新指令");
            return;
        }

        if (!onRefreshFirst) {
            //发送指令
            ToastUtils.show("刷新指令成功");
            deviceConnectActivity.sendCommonCommandImmediately("##040\r\n");
            onRefreshFirst = true;
        }
        if (prelongTim == 0) {
            prelongTim = (new Date()).getTime();
            refreshLayout.setRefreshing(false);
        } else {
            long curTime = (new Date()).getTime();
            long tenTime = curTime - prelongTim;
            //如果下拉刷新超过10s再次发送指令
            if (tenTime >= 10000) {
                //发送指令
                ToastUtils.show("刷新指令成功！");
                deviceConnectActivity.sendCommonCommandImmediately("##040\r\n");
                prelongTim = 0;
                onRefreshFirst = false;
                refreshLayout.setRefreshing(false);
            } else {
                ToastUtils.show("发送指令间隔需超过10s");
                refreshLayout.setRefreshing(false);
            }
        }

    }
}
