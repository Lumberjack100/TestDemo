package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.SatelitteTypeEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.e40.BDSBean;
import com.shmedo.configlibrary.iot.model.e40.CurrentExtendStateInfo;
import com.shmedo.configlibrary.iot.model.e40.GLOBean;
import com.shmedo.configlibrary.iot.model.e40.GPSBean;
import com.shmedo.configlibrary.iot.model.e40.SatelitteBean;
import com.shmedo.configlibrary.iot.model.e40.SensorBean;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.util.DeviceCurrentRunStateUtils;
import com.shmedo.mcloudapp.deviceconfig.view.RingProgressView;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import org.jetbrains.annotations.NotNull;

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
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

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
    private SatelitteBean satelitteBean;

    public static NetE40CurrentStateFragment newInstance(DeviceInfo deviceInfo) {
        NetE40CurrentStateFragment fragment = new NetE40CurrentStateFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.e40_current_state_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initSensorAdapter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    /**
     * 初始化主传感器适配器
     */
    private void initSensorAdapter() {
        sensorRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sensorRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(10f), getResources().getColor(R.color.transparent)));
        sensorAdapter = new CommonAdapter<SensorBean>(getActivity(), R.layout.item_e40_sensor_status, sensorList) {
            @Override
            protected void convert(CommonViewHolder holder, SensorBean sensorBean, int position) {
                holder.setText(R.id.tv_address, "通道" + sensorBean.getAddr());
                holder.setText(R.id.tv_type, getSensorNameByTypeCode(sensorBean.getType()));
                holder.setText(R.id.tv_status, sensorBean.getStatus() ? "正常" : "未接入");
                holder.setText(R.id.tv_value1, sensorBean.getVaule() + "");

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
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                queryStateInfo();
            }
        });
    }

    /**
     * 获取设备的当前状态
     */
    private void queryStateInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_EX_STATUS);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 获取设备的卫星状态
     */
    private void querySatelitteInfo(String type) {
        SatelitteTypeEntity entity = new SatelitteTypeEntity(type);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_SATELITTE, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(deviceInfo.getDeviceToken()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            ToastUtils.show("下发指令失败");
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
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
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
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
        ToastUtils.show("查询设备状态响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_EX_STATUS: {
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询设备状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                String content = commandResult.getResult();
                initStatusInfo(content);
                querySatelitteInfo("BDS");
            }
            break;

            case E40_MD_GET_SATELITTE: {
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询卫星数据出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                String content = commandResult.getResult();
                if (TextUtils.isEmpty(content)) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    return;
                }
                initSatelittleInfo(content);
                if (content.contains("BDS")) {
                    if (satelitteBean != null && satelitteBean.getBdsBeanList() != null) {
                        initBDSInfo(satelitteBean.getBdsBeanList());
                    } else {
                        mTvBDSatelliteRedNum.setText("!");
                        mTvBDSatelliteBlueNum.setText("!");
                        mTvBDSatelliteGreenNum.setText("!");
                    }
                    querySatelitteInfo("GPS");
                } else if (content.contains("GPS")) {
                    if (satelitteBean != null && satelitteBean.getGpsBeanList() != null) {
                        initGPSInfo(satelitteBean.getGpsBeanList());
                    } else {
                        mTvGpsSatelliteRedNum.setText("!");
                        mTvGpsSatelliteBlueNum.setText("!");
                        mTvGpsSatelliteGreenNum.setText("!");
                    }
                    querySatelitteInfo("GLO");
                } else if (content.contains("GLO")) {
                    mRefreshLayout.finishRefresh(true);
                    if (satelitteBean != null && satelitteBean.getGloBeanList() != null) {
                        initGLOInfo(satelitteBean.getGloBeanList());
                    } else {
                        mTvGloSatelliteRedNum.setText("!");
                        mTvGloSatelliteBlueNum.setText("!");
                        mTvGloSatelliteGreenNum.setText("!");
                    }
                }
            }
            break;

            default:
                break;
        }
    }

    private void initStatusInfo(String content) {
        try {
            content = content.replace("\\", "");
            extendStateInfo = GsonUtils.fromJson(content, CurrentExtendStateInfo.class);
            if (extendStateInfo != null) {
                //基本信息
                if (extendStateInfo.getBase() != null) {
                    mTvDeviceSn.setText(extendStateInfo.getBase().getSn());
                    mTVSimCardNumber.setText(extendStateInfo.getBase().getIccid());
                    mTvImeiNumber.setText(extendStateInfo.getBase().getImei());
                    mTvFirmwareVersion.setText(extendStateInfo.getBase().getVersion());
                    mTvBoardType.setText(extendStateInfo.getBase().getOem());

                    double voltage = extendStateInfo.getBase().getVolt();
                    SpannableStringBuilder builder = new SpannableStringBuilder(voltage + "V");
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(voltage <= 10 ? getContext().getResources().getColor(R.color.red) : getContext().getResources().getColor(R.color.text_color_666666));
                    builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mTvDeviceExternalVoltage.setText(builder);
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
            content = content.replace("\\", "");
            content = content.replace("\"BDS\":{", "\"BDS\":[");
            content = content.replace("\"GPS\":{", "\"GPS\":[");
            content = content.replace("\"GLO\":{", "\"GLO\":[");
            content = content.replace("}}}", "}]}");
            content = content.replaceAll("\"[GRC]\\d{2}\":", "");
            satelitteBean = GsonUtils.fromJson(content, SatelitteBean.class);

        } catch (Exception ex) {
            ex.printStackTrace();
            mRefreshLayout.finishRefresh(true);
            mTvBDSatelliteTotalNum.setText("总数：");
            mTvBDSatelliteRedNum.setText("!");
            mTvBDSatelliteBlueNum.setText("!");
            mTvBDSatelliteGreenNum.setText("!");

            mTvGpsSatelliteTotalNum.setText("总数：");
            mTvGpsSatelliteRedNum.setText("!");
            mTvGpsSatelliteBlueNum.setText("!");
            mTvGpsSatelliteGreenNum.setText("!");

            mTvGloSatelliteTotalNum.setText("总数：");
            mTvGloSatelliteRedNum.setText("!");
            mTvGloSatelliteBlueNum.setText("!");
            mTvGloSatelliteGreenNum.setText("!");
        }
    }

    private void initBDSInfo(List<BDSBean> bdsBeanList) {
        mTvBDSatelliteTotalNum.setText(String.format("总数：%d", bdsBeanList.size()));
        int bdRedNum = 0, bdBlueNum = 0, bdGreenNum = 0;
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
    }

    private void initGPSInfo(List<GPSBean> gpsBeanList) {
        mTvGpsSatelliteTotalNum.setText(String.format("总数：%d", gpsBeanList.size()));
        int gpsRedNum = 0, gpsBlueNum = 0, gpsGreenNum = 0;
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
    }

    private void initGLOInfo(List<GLOBean> gloBeanList) {
        mTvGloSatelliteTotalNum.setText(String.format("总数：%d", gloBeanList.size()));
        int gloRedNum = 0, gloBlueNum = 0, gloGreenNum = 0;
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

    /**
     * 中心状态
     *
     * @param
     * @param linkStatus
     */
    private void initLinkStatus(TextView tvLinkStatus, int linkStatus) {
        if (linkStatus == 0) {
            tvLinkStatus.setText("未开启");
            tvLinkStatus.setTextColor(ColorUtils.getColor(R.color.device_unopened_platform));
        } else if (linkStatus == 1) {
            tvLinkStatus.setText("已连接");
            tvLinkStatus.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094));
        } else if (linkStatus == 2) {
            tvLinkStatus.setText("未连接");
            tvLinkStatus.setTextColor(ColorUtils.getColor(R.color.device_not_connected_platform));
        }
    }

    private void setDeviceStatus(TextView textView, boolean status) {
        if (status) {
            textView.setText("正常");
            textView.setTextColor(ColorUtils.getColor(R.color.text_color_3AD094));
        } else {
            textView.setText("未接入");
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
