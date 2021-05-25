package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.e40.BDSBean;
import com.shmedo.configlibrary.iot.model.e40.CurrentExtendStateInfo;
import com.shmedo.configlibrary.iot.model.e40.GLOBean;
import com.shmedo.configlibrary.iot.model.e40.GPSBean;
import com.shmedo.configlibrary.iot.model.e40.SatelitteBean;
import com.shmedo.configlibrary.iot.model.e40.SensorBean;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.util.DeviceCurrentRunStateUtils;
import com.shmedo.mcloudapp.deviceconfig.view.RingProgressView;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：      E40 4G模式设备运行状态页面
 */
public class NetE40CurrentStateFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    /**
     * 基本信息
     */
    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_sim_card_number)
    TextView mTVSimCardNumber;

    @BindView(R.id.tv_imei_number)
    TextView mTvImeiNumber;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    @BindView(R.id.tv_board_type)
    TextView mTvBoardType;

    @BindView(R.id.tv_device_external_voltage)
    TextView mTvDeviceExternalVoltage;//设备外部电压

    /**
     * 存储状态
     */
    @BindView(R.id.ramProgress)
    RingProgressView ramProgress;

    @BindView(R.id.flashProgress)
    RingProgressView flashProgress;

    @BindView(R.id.tfCardProgress)
    RingProgressView tfcardProgress;

    @BindView(R.id.tv_ram)
    TextView mTvStorageRam;

    @BindView(R.id.tv_flash)
    TextView mTvStorageFlash;

    @BindView(R.id.tv_tfcard)
    TextView mTvStorageTfCard;

    /**
     * 数据中心
     */
    @BindView(R.id.tv_signal_strength)
    TextView mTvSignalStrength;

    @BindView(R.id.tv_network_mode)
    TextView mTvNetworkMode;//网络模式

    @BindView(R.id.tv_link_one_status)
    TextView mTvLinkOneStatus;

    @BindView(R.id.tv_link_two_status)
    TextView mTvLinkTwoStatus;

    @BindView(R.id.tv_link_three_status)
    TextView mTvLinkThreeStatus;

    @BindView(R.id.tv_link_four_status)
    TextView mTvLinkFourStatus;

    /**
     * 太阳能控制器
     */
    @BindView(R.id.solarInfo)
    View solarInfoLayout;

    @BindView(R.id.tv_solar_status)
    TextView mTvSolarStatus;

    @BindView(R.id.tv_solar_voltage)
    TextView mTvSolarVoltage;

    @BindView(R.id.tv_battery_voltage)
    TextView mTvBatteryVoltage;

    @BindView(R.id.tv_consume_voltage)
    TextView mTvConsumeVoltage;

    /**
     * Mems
     */
    @BindView(R.id.memsInfo)
    View memsInfoLayout;

    @BindView(R.id.tv_mems_status)
    TextView mTvMemsStatus;

    @BindView(R.id.tv_inclination)
    TextView mTvInclination;

    @BindView(R.id.sensor_recyclerView)
    RecyclerView sensorRecyclerView;

    /**
     * GNSS
     */
    @BindView(R.id.tv_gnss_time)
    TextView mTvGnssTime;

    @BindView(R.id.tv_gnss_position)
    TextView mTvGnssPosition;

    @BindView(R.id.tv_bd_satellite_total_num)
    TextView mTvBDSatelliteTotalNum;

    @BindView(R.id.tv_bd_red_num)
    TextView mTvBDSatelliteRedNum;

    @BindView(R.id.tv_bd_blue_num)
    TextView mTvBDSatelliteBlueNum;

    @BindView(R.id.tv_bd_green_num)
    TextView mTvBDSatelliteGreenNum;

    @BindView(R.id.tv_gps_satellite_total_num)
    TextView mTvGpsSatelliteTotalNum;

    @BindView(R.id.tv_gps_red_num)
    TextView mTvGpsSatelliteRedNum;

    @BindView(R.id.tv_gps_blue_num)
    TextView mTvGpsSatelliteBlueNum;

    @BindView(R.id.tv_gps_green_num)
    TextView mTvGpsSatelliteGreenNum;

    @BindView(R.id.tv_glo_satellite_total_num)
    TextView mTvGloSatelliteTotalNum;

    @BindView(R.id.tv_glo_red_num)
    TextView mTvGloSatelliteRedNum;

    @BindView(R.id.tv_glo_blue_num)
    TextView mTvGloSatelliteBlueNum;

    @BindView(R.id.tv_glo_green_num)
    TextView mTvGloSatelliteGreenNum;

    private List<SensorBean> sensorList = new ArrayList<>();
    private CommonAdapter sensorAdapter;

    private CurrentExtendStateInfo extendStateInfo;

    public static NetE40CurrentStateFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40CurrentStateFragment fragment = new NetE40CurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.e40_current_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSensorAdapter();
        initRefreshLayout();
        // 进入页面，刷新数据
        swipeRefresh.setRefreshing(true);
        queryStateInfo();
    }

    /**
     * 初始化主传感器适配器
     */
    private void initSensorAdapter() {
        sensorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sensorRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, DensityUtil.Dp2Px(mActivity, 10f), getResources().getColor(R.color.transparent)));
        sensorAdapter = new CommonAdapter<SensorBean>(getActivity(), R.layout.item_e40_sensor_status, sensorList) {
            @Override
            protected void convert(CommonViewHolder holder, SensorBean sensorBean, int position) {
                holder.setText(R.id.tv_address, "通道" + sensorBean.getAddr());
                holder.setText(R.id.tv_type, getSensorNameByTypeCode(sensorBean.getType()));
                holder.setText(R.id.tv_status, sensorBean.getStatus() ? "正常" : "异常");
                holder.setText(R.id.tv_value, sensorBean.getVaule() + "");

                if (sensorBean.getStatus()) {
                    holder.setTextColorRes(R.id.tv_status, R.color.text_color_3AD094);
                } else {
                    holder.setTextColorRes(R.id.tv_status, R.color.red);
                }
            }
        };
        sensorRecyclerView.setAdapter(sensorAdapter);
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryStateInfo();
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryStateInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_EX_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取设备的卫星状态
     */
    private void querySatelitteInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_SATELITTE);
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
            swipeRefresh.setRefreshing(false);
            ToastUtils.show("下发指令失败");
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
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        swipeRefresh.setRefreshing(false);
        ToastUtils.show("查询设备状态响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        swipeRefresh.setRefreshing(false);
        ToastUtils.show("查询设备状态响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
//        swipeRefresh.setRefreshing(false);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_EX_STATUS: {
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    swipeRefresh.setRefreshing(false);
                    String errMsg = String.format("%s %s", "查询设备状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                String content = commandResult.getResult();
                initStatusInfo(content);
                querySatelitteInfo();
            }
            break;

            case E40_MD_GET_SATELITTE: {
                swipeRefresh.setRefreshing(false);
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询卫星数据出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                String content = commandResult.getResult();
                initSatelittleInfo(content);
            }
            break;

            default:
                break;
        }
    }

    private void initStatusInfo(String content) {
        try {
            content = content.replace("\\", "");
            extendStateInfo = GsonFactory.getGson().fromJson(content, CurrentExtendStateInfo.class);
            if (extendStateInfo != null) {
                //基本信息
                if (extendStateInfo.getBase() != null) {
                    mTvDeviceSn.setText(extendStateInfo.getBase().getSn());
                    mTVSimCardNumber.setText(extendStateInfo.getBase().getIccid());
                    mTvImeiNumber.setText(extendStateInfo.getBase().getImei());
                    mTvFirmwareVersion.setText(extendStateInfo.getBase().getVersion());
                    mTvBoardType.setText(extendStateInfo.getBase().getOem());
                    mTvDeviceExternalVoltage.setText(String.format("%s V", extendStateInfo.getBase().getVolt() + ""));
                }
                //存储状态
                if (extendStateInfo.getStorage() != null) {
                    try {
                        int ramValue = Integer.parseInt(extendStateInfo.getStorage().getRam().replace("%", ""));
                        int flashValue = Integer.parseInt(extendStateInfo.getStorage().getFlash().replace("%", ""));
                        int tfCradValue = Integer.parseInt(extendStateInfo.getStorage().getTfcard().replace("%", ""));

                        if (ramValue <= 80) {
                            ramProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_normal));
                        } else if (ramValue <= 100) {
                            ramProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_warn));
                        } else {
                            ramProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_abnormal));
                            mTvStorageRam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_storage_error, 0);
                        }

                        if (flashValue <= 80) {
                            flashProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_normal));
                        } else if (flashValue <= 100) {
                            flashProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_warn));
                        } else {
                            flashProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_abnormal));
                            mTvStorageFlash.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_storage_error, 0);
                        }

                        if (tfCradValue <= 80) {
                            tfcardProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_normal));
                        } else if (tfCradValue <= 100) {
                            tfcardProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_warn));
                        } else {
                            tfcardProgress.setRingProgressColor(getResources().getColor(R.color.storage_ring_progress_abnormal));
                            mTvStorageTfCard.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_storage_error, 0);
                        }
                        ramProgress.setCurrentProgress(ramValue);
                        ramProgress.postInvalidate();

                        flashProgress.setCurrentProgress(flashValue);
                        flashProgress.postInvalidate();

                        tfcardProgress.setCurrentProgress(tfCradValue);
                        tfcardProgress.postInvalidate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                //网络状态
                if (extendStateInfo.getNet() != null) {
                    if (extendStateInfo.getNet().get_$4g().toLowerCase().equals("on")) {
                        mTvSignalStrength.setVisibility(View.VISIBLE);
                        mTvSignalStrength.setBackgroundResource(R.drawable.bg_corner_2dp_stroke_1dp_50e9b9);
                        mTvSignalStrength.setText(DeviceCurrentRunStateUtils.getOperatorType2(extendStateInfo.getNet().getIsp()));
                        mTvNetworkMode.setCompoundDrawablesWithIntrinsicBounds(DeviceCurrentRunStateUtils.getSignalResIdByCSQValue(extendStateInfo.getNet().getCsq()), 0, 0, 0);
                    } else {
                        mTvSignalStrength.setVisibility(View.GONE);
                        mTvNetworkMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    }
                    mTvNetworkMode.setText(extendStateInfo.getNet().get_$4g().toLowerCase().equals("on") ? extendStateInfo.getNet().getType() : "本地网络");
                    initLinkStatus(mTvLinkOneStatus, extendStateInfo.getNet().getSocket1());
                    initLinkStatus(mTvLinkTwoStatus, extendStateInfo.getNet().getSocket2());
                    initLinkStatus(mTvLinkThreeStatus, extendStateInfo.getNet().getSocket3());
                    initLinkStatus(mTvLinkFourStatus, extendStateInfo.getNet().getSocket4());
                }
                //太阳能控制器
                if (extendStateInfo.getSolar() != null) {
                    if (extendStateInfo.getSolar().getSw().toLowerCase().equals("on")) {
                        solarInfoLayout.setVisibility(View.VISIBLE);
                        setDeviceStatus(mTvSolarStatus, extendStateInfo.getSolar().getStatus());
                        mTvSolarVoltage.setText(String.format("%sV", extendStateInfo.getSolar().getSloarvolt()));
                        mTvBatteryVoltage.setText(String.format("%sV", extendStateInfo.getSolar().getBatvolt()));
                        mTvConsumeVoltage.setText(String.format("%sV", extendStateInfo.getSolar().getPayloadvolt()));
                    } else {
                        solarInfoLayout.setVisibility(View.GONE);
                    }
                }
                //Mems
                if (extendStateInfo.getMems() != null) {
                    if (extendStateInfo.getMems().getSw().toLowerCase().equals("on")) {
                        memsInfoLayout.setVisibility(View.VISIBLE);
                        setDeviceStatus(mTvMemsStatus, extendStateInfo.getMems().getStatus());
                        mTvInclination.setText(String.format("%s,%s,%s", extendStateInfo.getMems().getX(), extendStateInfo.getMems().getY(), extendStateInfo.getMems().getZ()));
                    } else {
                        memsInfoLayout.setVisibility(View.GONE);
                    }
                }
                //主传感器
                sensorList.clear();
                sensorList.addAll(extendStateInfo.getSensor());
                sensorAdapter.notifyDataSetChanged();

                //GNSS 信息
                if (extendStateInfo.getGnss() != null) {
                    mTvGnssTime.setText(extendStateInfo.getGnss().getTime());
                    mTvGnssPosition.setText(String.format("%s,%s", extendStateInfo.getGnss().getLon(), extendStateInfo.getGnss().getLat()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initSatelittleInfo(String content) {
        try {
//            content = "{\"UTCTime\":\"2021-05-24 05:50:45.0\",\"GPS\":{\"G01\":{\"AZ\":40.595654,\"EL\":32.298524,\"L1\":42,\"L2\":31,\"L3\":0},\"G03\":{\"AZ\":85.327445,\"EL\":51.693389,\"L1\":48,\"L2\":42,\"L3\":0},\"G06\":{\"AZ\":246.994703,\"EL\":27.985387,\"L1\":43,\"L2\":43,\"L3\":0},\"G14\":{\"AZ\":242.780189,\"EL\":80.830086,\"L1\":48,\"L2\":41,\"L3\":0},\"G17\":{\"AZ\":333.197570,\"EL\":51.668473,\"L1\":44,\"L2\":45,\"L3\":0},\"G19\":{\"AZ\":306.554044,\"EL\":32.334989,\"L1\":41,\"L2\":28,\"L3\":0},\"G21\":{\"AZ\":0,\"EL\":0,\"L1\":41,\"L2\":24,\"L3\":0},\"G22\":{\"AZ\":53.791276,\"EL\":32.508679,\"L1\":45,\"L2\":32,\"L3\":0},\"G28\":{\"AZ\":308.186403,\"EL\":66.967555,\"L1\":45,\"L2\":36,\"L3\":0},\"G30\":{\"AZ\":0,\"EL\":0,\"L1\":38,\"L2\":36,\"L3\":0}},\"GLO\":{\"R04\":{\"SAT\":\"R04\",\"AZ\":80.115497,\"EL\":26.621261,\"L1\":47,\"L2\":45,\"L3\":0},\"R05\":{\"SAT\":\"R05\",\"AZ\":134.297591,\"EL\":18.717331,\"L1\":48,\"L2\":43,\"L3\":0},\"R09\":{\"SAT\":\"R09\",\"AZ\":212.377262,\"EL\":46.523622,\"L1\":51,\"L2\":48,\"L3\":0},\"R19\":{\"SAT\":\"R19\",\"AZ\":0,\"EL\":0,\"L1\":35,\"L2\":39,\"L3\":0},\"R20\":{\"SAT\":\"R20\",\"AZ\":0,\"EL\":0,\"L1\":38,\"L2\":44,\"L3\":0}},\"BDS\":{\"C01\":{\"SAT\":\"C01\",\"AZ\":0,\"EL\":0,\"L1\":47,\"L2\":49,\"L3\":0},\"C02\":{\"SAT\":\"C02\",\"AZ\":0,\"EL\":0,\"L1\":40,\"L2\":45,\"L3\":0},\"C03\":{\"SAT\":\"C03\",\"AZ\":0,\"EL\":0,\"L1\":46,\"L2\":47,\"L3\":0},\"C04\":{\"SAT\":\"C04\",\"AZ\":0,\"EL\":0,\"L1\":44,\"L2\":48,\"L3\":0},\"C05\":{\"SAT\":\"C05\",\"AZ\":0,\"EL\":0,\"L1\":0,\"L2\":38,\"L3\":0},\"C07\":{\"SAT\":\"C07\",\"AZ\":0,\"EL\":0,\"L1\":48,\"L2\":50,\"L3\":0},\"C08\":{\"SAT\":\"C08\",\"AZ\":0,\"EL\":0,\"L1\":45,\"L2\":47,\"L3\":0},\"C10\":{\"SAT\":\"C10\",\"AZ\":0,\"EL\":0,\"L1\":45,\"L2\":46,\"L3\":0},\"C13\":{\"SAT\":\"C13\",\"AZ\":236.050225,\"EL\":49.063911,\"L1\":46,\"L2\":45,\"L3\":0}}}";
            content = content.replace("\\", "");
            content = content.replace("\"GPS\":{", "\"GPS\":[");
            content = content.replace("},\"GLO\":{", "],\"GLO\":[");
            content = content.replace("},\"BDS\":{", "],\"BDS\":[");
            content = content.replace("}}}", "}]}");
            content = content.replaceAll("\"[GRC]\\d{2}\":", "");

            SatelitteBean satelitteBean = GsonFactory.getGson().fromJson(content, SatelitteBean.class);
            if (satelitteBean != null) {
                List<BDSBean> bdsBeanList = satelitteBean.getBdsBeanList();
                List<GPSBean> gpsBeanList = satelitteBean.getGpsBeanList();
                List<GLOBean> gloBeanList = satelitteBean.getGloBeanList();

                mTvBDSatelliteTotalNum.setText(String.format("总数：%d", bdsBeanList.size()));
                mTvGpsSatelliteTotalNum.setText(String.format("总数：%d", gpsBeanList.size()));
                mTvGloSatelliteTotalNum.setText(String.format("总数：%d", gloBeanList.size()));

                int bdRedNum = 0, bdBlueNum = 0, bdGreenNum = 0, gpsRedNum = 0, gpsBlueNum = 0, gpsGreenNum = 0, gloRedNum = 0, gloBlueNum = 0, gloGreenNum = 0;
                for (BDSBean bdsBean : bdsBeanList) {
                    if (bdsBean.getL1() < 25) {
                        bdRedNum += 1;
                    } else if (bdsBean.getL1() < 35) {
                        bdBlueNum += 1;
                    } else {
                        bdGreenNum += 1;
                    }
                }
                mTvBDSatelliteRedNum.setText(String.valueOf(bdRedNum));
                mTvBDSatelliteBlueNum.setText(String.valueOf(bdBlueNum));
                mTvBDSatelliteGreenNum.setText(String.valueOf(bdGreenNum));

                for (GPSBean gpsBean : gpsBeanList) {
                    if (gpsBean.getL1() < 25) {
                        gpsRedNum += 1;
                    } else if (gpsBean.getL1() < 35) {
                        gpsBlueNum += 1;
                    } else {
                        gpsGreenNum += 1;
                    }
                }
                mTvGpsSatelliteRedNum.setText(String.valueOf(gpsRedNum));
                mTvGpsSatelliteBlueNum.setText(String.valueOf(gpsBlueNum));
                mTvGpsSatelliteGreenNum.setText(String.valueOf(gpsGreenNum));

                for (GLOBean gloBean : gloBeanList) {
                    if (gloBean.getL1() < 25) {
                        gloRedNum += 1;
                    } else if (gloBean.getL1() < 35) {
                        gloBlueNum += 1;
                    } else {
                        gloGreenNum += 1;
                    }
                }
                mTvGloSatelliteRedNum.setText(String.valueOf(gloRedNum));
                mTvGloSatelliteBlueNum.setText(String.valueOf(gloBlueNum));
                mTvGloSatelliteGreenNum.setText(String.valueOf(gloGreenNum));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 中心状态
     *
     * @param
     * @param linkStatus
     */
    private void initLinkStatus(TextView tvLinkStatus, int linkStatus) {
        if (linkStatus == 0) {
            tvLinkStatus.setText("未开启");
            tvLinkStatus.setTextColor(GlobalUtil.getColor(R.color.device_unopened_platform));
        } else if (linkStatus == 1) {
            tvLinkStatus.setText("已连接");
            tvLinkStatus.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
        } else if (linkStatus == 2) {
            tvLinkStatus.setText("未连接");
            tvLinkStatus.setTextColor(GlobalUtil.getColor(R.color.device_not_connected_platform));
        }
    }

    private void setDeviceStatus(TextView textView, boolean status) {
        if (status) {
            textView.setText("正常");
            textView.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));
        } else {
            textView.setText("异常");
            textView.setTextColor(Color.RED);
        }
    }

    /**
     * 根据传感器的编号返回对应的名称
     *
     * @param typeCode
     * @return
     */
    public String getSensorNameByTypeCode(int typeCode) {
        switch (typeCode) {
            case 1:
                return "压电雨量计";

            case 2:
                return "翻斗雨量计";

            default:
                return "未知类型";
        }
    }
}
