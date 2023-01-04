package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.model.das.DasCollectorInfo;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.DASSensorAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DASSensorItem;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/1/3 <br/>
 * 描述：   扩展传感器列表页面
 */
public class BleCommonSensorListFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview_sensor)
    RecyclerView mRecyclerViewSensor;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private DASSensorAdapter sensorAdapter;
    private List<DASSensorItem> sensorItemList = new ArrayList<>();
    private DASSensorItem curSensorItem;

    //以传感器的通道号为 Key,DasExternalSensorInfo 对象为 Value
    private HashMap<String, DasExternalSensorInfo> sensorHashMap = new HashMap<>();
    private ArrayList<String> addressList = new ArrayList<>();

    private DasCollectorInfo collectorInfo;
    private int accessSum = 0;  //接入扩展传感器总数
    private int sensorIndex = 0;//接入的传感器索引号
    private int curItemPosition = 0;
    private boolean isEnableNewSensor = false;//是启用新传感器还是编辑现有传感器
    private boolean isVibratingWireSensor = false;//是否振弦式传感器
    private ActivityResultLauncher<Intent> resultLauncher;

    public static BleCommonSensorListFragment newInstance() {
        return new BleCommonSensorListFragment();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_common_sensor_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initExtendSensorAdapter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }
    private void initExtendSensorAdapter() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerViewSensor.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewSensor.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        sensorAdapter = new DASSensorAdapter(sensorItemList);
        sensorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (collectorInfo == null || TextUtils.isEmpty(collectorInfo.getType())) {
                    ToastUtils.show("未获取到采集器信息，请先刷新");
                    return;
                }
                processItemClick(position);
            }
        });
        sensorAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                curItemPosition = position;
                DASSensorItem sensorItem = sensorItemList.get(position);
                if (sensorItem.isAddButton()) {
                    return true;
                }
                if (sensorItemList.size() <= 2) {
                    ToastUtils.show("最少保留一个传感器!");
                    return true;
                }
                warnDeleteSensorItem();
                return true;
            }
        });
        mRecyclerViewSensor.setAdapter(sensorAdapter);
    }
    private void processItemClick(int position) {

    }
    private void warnDeleteSensorItem() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示")
                .content("确定移除传感器?")
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
                        removeSensor();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void clear() {
        accessSum = 0;
        sensorIndex = 0;
        curSensorItem = null;
        sensorHashMap.clear();
    }
    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                clear();
                initDefaultSensorItem();
                queryCollectorInfo();
            }
        });
    }
}