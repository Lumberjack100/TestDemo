package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorConfigEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorSensorParamsEntity;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.ble.model.CollectorSensorParamsInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.CmdResponseMessage;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.projects.adapter.DASSensorAdapter;
import com.shmedo.mcloudapp.projects.model.DASSensorItem;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * DAS扩展传感器配置页面
 */
public class BleDASExternalSensorConfigFragment extends BaseBleConnectFragment {
    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    private DASSensorAdapter sensorAdapter;
    private List<DASSensorItem> sensorItemList = new ArrayList<>();

    private String collectorName;
    private String collectorModel = "";//采集器类型
    private int accessSum;              //接入扩展传感器总数
    private int sensorIndex = 0;//接入的传感器索引号
    private StringBuilder sbcollectorSensor;//采集器接入扩展传感器配置信息

    //以传感器的通道号为 Key,CollectorSensorParamsInfo 对象为 Value
    protected HashMap<String, CollectorSensorParamsInfo> collectorSensorHashMap = new HashMap<>();
    protected CollectorSensorParamsInfo defaultCollectorSensorParamsInfo = new CollectorSensorParamsInfo();


    public static BleDASExternalSensorConfigFragment newInstance(String collectorModel) {
        BleDASExternalSensorConfigFragment fragment = new BleDASExternalSensorConfigFragment();
        Bundle args = new Bundle();
        args.putString(AppContants.Extras.COLLECTOR_MODE, collectorModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectorModel = getArguments().getString(AppContants.Extras.COLLECTOR_MODE);
            collectorName = BlueResultParserUtil.getCollectorName(CollectorModel.value(collectorModel));
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_d_a_s_external_sensor_config;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initExtendSensorAdapter();
        queryCollectorInfo();
    }

    private void initExtendSensorAdapter() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(getActivity(), 15);//每一个矩形的间距
        mRecyclerViewSensor.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewSensor.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        sensorAdapter = new DASSensorAdapter(sensorItemList);
        sensorAdapter.setAnimationEnable(true);
        sensorAdapter.setAnimationFirstOnly(false);
        sensorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {

            }
        });
        mRecyclerViewSensor.setAdapter(sensorAdapter);
    }


    /**
     * 查询采集器配置信息
     */
    private void queryCollectorInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", 20000);
        CollectorConfigEntity collectorConfigEntity = new CollectorConfigEntity(collectorModel);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, collectorConfigEntity);
        sendCommonCommandImmediately(command);
        Timber.d("查询采集器配置信息===%s", command);
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    private void queryExtendSensorConfigInfo() {
        if (sensorIndex >= accessSum) {
            return;
        }

        String address = StringUtil.formatStringTwo(sensorIndex + "");
        CollectorSensorParamsEntity entity = new CollectorSensorParamsEntity(collectorModel, address);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER, entity);
        sendCommonCommand(command);
        Timber.d("获取 %s 采集器 %s 通道的传感器参数===%s", collectorName, address, command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
//            sendCollector();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        if (messageEvent instanceof CmdResponseMessage) {
            if (!isActive) {
                return;
            }
            setResultData((CmdResponseMessage) messageEvent);
        } else {
            super.onMessageEvent(messageEvent);
        }
    }

    private void setResultData(CmdResponseMessage responseMessage) {
        String cmdStr = responseMessage.getResult();
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case COLLECTOR_CONFIG://采集器配置信息 100
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    Timber.e("查询采集器配置信息指令出错!");
                    return;
                }
                CollectorConfigInfo collectorConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (collectorConfigInfo == null) {
                    Timber.e("采集器配置信息为空!");
                    stopProgressRunnable();
                    return;
                }

                accessSum = collectorConfigInfo.getAccessSum();
                // 查询传感器配置信息前,重置accessNumFlag、sbcollectorSensor参数
                sensorIndex = 0;
                sbcollectorSensor = new StringBuilder();
                queryExtendSensorConfigInfo();
                break;

            case COLLECTOR_CHANNEL_SENSOR_PARAMETER://获取XX采集器YY通道的传感器参数 101
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    stopProgressRunnable();
                    Timber.e("查询采集器配置信息指令出错!");
                    return;
                }

                processCollectorSensorParamsInfo(cmdStr);
                sensorIndex++;
                queryExtendSensorConfigInfo();

                if (sensorIndex >= accessSum) {
                    stopProgressRunnable();
                    if (!collectorSensorHashMap.values().isEmpty()) {
                        defaultCollectorSensorParamsInfo = (CollectorSensorParamsInfo) collectorSensorHashMap.values().toArray()[0];
                    }
                    initSensorItems();
                    return;
                }
                break;
        }
    }

    /**
     * 初始化数字渗压计参数
     */
    private void processCollectorSensorParamsInfo(String cmdStr) {
        CollectorSensorParamsInfo mCollectorParamsInfoSub = ResultParserUtil.getEntityObject(cmdStr);
        if (mCollectorParamsInfoSub == null) {
            Timber.e("%s 采集器 %s 通道的传感器参数为空!", collectorModel, StringUtil.formatStringTwo(sensorIndex + ""));
//            queryOsmometerParameterInfo = new QueryOsmometerParameterInfo();
            return;
        }
        Timber.d("%s 采集器 %s 通道的传感器参数-------%s", collectorModel, StringUtil.formatStringTwo(sensorIndex + ""), mCollectorParamsInfoSub.toString());
        collectorSensorHashMap.put(mCollectorParamsInfoSub.getChannelNumber(), mCollectorParamsInfoSub);
    }

    private void initSensorItems() {
        DASSensorItem sensorItem;
        int num = collectorSensorHashMap.values().size();
        for (int i = 0; i < num; i++) {
            sensorItem = new DASSensorItem(R.drawable.ic_sensor_holder);
            sensorItemList.add(sensorItem);
        }
        sensorItem = new DASSensorItem(R.drawable.ic_add_sensor);
        sensorItemList.add(sensorItem);
        sensorAdapter.notifyDataSetChanged();
    }
}
