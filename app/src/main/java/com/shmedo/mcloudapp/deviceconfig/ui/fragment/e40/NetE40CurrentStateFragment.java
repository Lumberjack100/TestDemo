package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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
import com.shmedo.configlibrary.iot.model.e40.CurrentExtendStateInfo;
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
    @BindView(R.id.pb_storage_ram)
    ProgressBar pBarStorageRam;

    @BindView(R.id.pb_storage_flash)
    ProgressBar pBarStorageFlash;

    @BindView(R.id.pb_storage_tfcard)
    ProgressBar pBarStorageTfcard;

    @BindView(R.id.tv_storage_ram)
    TextView mTvStorageRam;

    @BindView(R.id.tv_storage_flash)
    TextView mTvStorageFlash;

    @BindView(R.id.tv_storage_tfcard)
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

    @BindView(R.id.gnss_recyclerView)
    RecyclerView gnssRecyclerView;

    private List<SensorBean> sensorList = new ArrayList<>();
    private List<SatelitteBean> satelitteBeanList = new ArrayList<>();
    private CommonAdapter sensorAdapter, satelitteAdapter;

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
        initSatelitteAdapter();
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

    /**
     * 初始化GNSS适配器
     */
    private void initSatelitteAdapter() {
        gnssRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        gnssRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, DensityUtil.Dp2Px(mActivity, 10f), getResources().getColor(R.color.transparent)));
        satelitteAdapter = new CommonAdapter<SatelitteBean>(getActivity(), R.layout.item_e40_gnss_status, satelitteBeanList) {
            @Override
            protected void convert(CommonViewHolder holder, SatelitteBean satelitteBean, int position) {
                if (satelitteBean.getType() == 1) {
                    holder.setText(R.id.tv_titel, "北斗搜星数");
                } else if (satelitteBean.getType() == 2) {
                    holder.setText(R.id.tv_titel, "GLONASS搜星数");
                } else if (satelitteBean.getType() == 3) {
                    holder.setText(R.id.tv_titel, "GPS搜星数");
                }
                holder.setText(R.id.tv_satellite_total_num, "总数:" + satelitteBean.getMax());
                holder.setText(R.id.tv_red_num, String.valueOf(satelitteBean.getLow()));
                holder.setText(R.id.tv_yellow_num, String.valueOf(satelitteBean.getMid()));
                holder.setText(R.id.tv_green_num, String.valueOf(satelitteBean.getHigh()));
            }
        };
        gnssRecyclerView.setAdapter(satelitteAdapter);
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
        swipeRefresh.setRefreshing(false);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_EX_STATUS: {
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询设备状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                String content = commandResult.getResult();
                initStatusInfo(content);
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

                        ramValue = 120;
                        if (ramValue <= 50) {
                            pBarStorageRam.setProgressDrawable(getResources().getDrawable(R.drawable.storage_low_progress_indeterminate_horizontal));
                        } else {
                            pBarStorageRam.setProgressDrawable(getResources().getDrawable(R.drawable.storage_high_progress_indeterminate_horizontal));
                        }

                        if (flashValue <= 50) {
                            pBarStorageFlash.setProgressDrawable(getResources().getDrawable(R.drawable.storage_low_progress_indeterminate_horizontal));
                        } else {
                            pBarStorageFlash.setProgressDrawable(getResources().getDrawable(R.drawable.storage_high_progress_indeterminate_horizontal));
                        }

                        if (tfCradValue <= 50) {
                            pBarStorageTfcard.setProgressDrawable(getResources().getDrawable(R.drawable.storage_low_progress_indeterminate_horizontal));
                        } else {
                            pBarStorageTfcard.setProgressDrawable(getResources().getDrawable(R.drawable.storage_high_progress_indeterminate_horizontal));
                        }

                        pBarStorageRam.setProgress(ramValue);
                        pBarStorageFlash.setProgress(flashValue);
                        pBarStorageTfcard.setProgress(tfCradValue);

                        mTvStorageRam.setText(ramValue > 100 ? "错误" : extendStateInfo.getStorage().getRam());
                        mTvStorageFlash.setText(flashValue > 100 ? "错误" : extendStateInfo.getStorage().getFlash());
                        mTvStorageTfCard.setText(flashValue > 100 ? "错误" : extendStateInfo.getStorage().getTfcard());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                //网络状态
                if (extendStateInfo.getNet() != null) {
                    if (extendStateInfo.getNet().get_$4g().toLowerCase().equals("on")) {
                        mTvSignalStrength.setVisibility(View.VISIBLE);
                        mTvSignalStrength.setCompoundDrawablesWithIntrinsicBounds(0, 0, DeviceCurrentRunStateUtils.getSignalResIdByCSQValue(extendStateInfo.getNet().getCsq()), 0);
                        mTvSignalStrength.setText(DeviceCurrentRunStateUtils.getOperatorType2(extendStateInfo.getNet().getIsp()));
                    } else {
                        mTvSignalStrength.setVisibility(View.GONE);
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

                    List<SatelitteBean> satelitte = extendStateInfo.getGnss().getSatelitte();
                    satelitteBeanList.clear();
                    satelitteBeanList.addAll(satelitte);
                    satelitteAdapter.notifyDataSetChanged();
                }
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
