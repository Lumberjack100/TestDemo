package com.shmedo.mcloudapp.projects.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.projects.adapter.DeviceTypeAdapter;
import com.shmedo.mcloudapp.projects.model.DeviceTypeInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfoWrapper;
import com.shmedo.mcloudapp.projects.model.param.QueryProjectDevice;
import com.shmedo.mcloudapp.projects.ui.activity.ProjectSearchActivity;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DevicesInProjectFragment extends BaseFragment {
    @BindView(R.id.tv_online_num)
    TextView tvOnlineNum;

    @BindView(R.id.tv_offline_num)
    TextView tvOfflineNum;

    @BindView(R.id.tv_online_rate)
    TextView tvOnlineRate;

    @BindView(R.id.recyclerview_device_type)
    RecyclerView mRecyclerViewDeviceType;

    @BindView(R.id.recyclerview_device)
    RecyclerView mRecyclerViewDevice;

    private DeviceTypeAdapter deviceTypeAdapter;
    private DeviceInfoAdapter deviceInfoAdapter;
    private List<DeviceTypeInfo> deviceTypeInfos = new ArrayList<>();
    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();
    private List<ProjectDeviceInfo> tempDeviceInfoList = new ArrayList<>();
    private Map<String, Integer> deviceTypeMap = new HashMap<>();

    private int userId;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_devices_in_project;
    }

    @Override
    protected void initView() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            userId = user.getId();
        }

        initDeviceTypeAdapter();
        initDeviceInfoAdapter();
//        initTestDeviceData();
        queryCompanyDevice();
    }

    private void initDeviceTypeAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewDeviceType.setLayoutManager(linearLayoutManager);
        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.transparent), DensityUtil.Dp2Px(getActivity(), 20), 0);
        mRecyclerViewDeviceType.addItemDecoration(mItemDecoration);
        deviceTypeAdapter = new DeviceTypeAdapter(deviceTypeInfos);
        deviceTypeAdapter.setAnimationEnable(true);
        deviceTypeAdapter.setAnimationFirstOnly(false);
        deviceTypeAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                DeviceTypeInfo deviceTypeInfo = (DeviceTypeInfo) deviceTypeInfos.get(position);
                if (deviceTypeInfo.isChecked()) {
                    return;
                }

                for (DeviceTypeInfo typeInfo : deviceTypeInfos) {
                    typeInfo.setChecked(false);
                }
                deviceTypeInfo.setChecked(true);
                deviceTypeAdapter.notifyDataSetChanged();

                filterDeviceDataByType(deviceTypeInfo.getName());
            }
        });
        mRecyclerViewDeviceType.setAdapter(deviceTypeAdapter);
    }

    private void initDeviceInfoAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(getActivity(), 15);//每一个矩形的间距
        mRecyclerViewDevice.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewDevice.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        deviceInfoAdapter = new DeviceInfoAdapter(deviceInfoList);
        deviceInfoAdapter.setAnimationEnable(true);
        deviceInfoAdapter.setAnimationFirstOnly(false);
        deviceInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ProjectDeviceInfo deviceInfo = (ProjectDeviceInfo) deviceInfoList.get(position);
            }
        });
        mRecyclerViewDevice.setAdapter(deviceInfoAdapter);
    }


    @OnClick({R.id.search_container})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_container:
                ProjectSearchActivity.startActivity(getActivity());
                break;
        }
    }

    private void queryCompanyDevice() {
        showLoadingDialog("加载中...");
        QueryProjectDevice parameter = new QueryProjectDevice();
        parameter.setCompanyID(1);
        parameter.setDeviceType(-1);
        parameter.setPageSize(20);
        parameter.setCurrentPage(1);

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryCompanyDevice(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ProjectDeviceInfoWrapper>() {
                    @Override
                    public void Success(ProjectDeviceInfoWrapper data, String message) {
                        if (data == null || data.getCurrentPageData() == null) {
                            dismissLoadingDialog();
                            return;
                        }

                        tempDeviceInfoList.clear();
                        deviceInfoList.clear();
                        tempDeviceInfoList.addAll(data.getCurrentPageData());
                        deviceInfoList.addAll(data.getCurrentPageData());
                        deviceInfoAdapter.notifyDataSetChanged();

                        updateTopView();
                        setDeviceTypeData();
                        dismissLoadingDialog();
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        Timber.w("请求失败--%s", message);
                    }
                });
    }

    private void updateTopView() {
        int onlineNum = 0;
        int offlineNum = 0;
        for (ProjectDeviceInfo deviceInfo : deviceInfoList) {
            if (deviceInfo.isOnline()) {
                onlineNum++;
            }
        }

        offlineNum = deviceInfoList.size() - onlineNum;
        DecimalFormat df = new DecimalFormat("#.#");//格式化小数
        String rate = df.format(onlineNum / (float) deviceInfoList.size() * 100) + "%";

        tvOnlineNum.setText(String.valueOf(onlineNum));
        tvOfflineNum.setText(String.valueOf(offlineNum));
        tvOnlineRate.setText(rate);
    }

    private void setDeviceTypeData() {
        deviceTypeMap.clear();
        for (ProjectDeviceInfo deviceInfo : deviceInfoList) {
            if (deviceTypeMap.containsKey(deviceInfo.getDeviceTypeName())) {
                int num = deviceTypeMap.get(deviceInfo.getDeviceTypeName());
                num++;
                deviceTypeMap.put(deviceInfo.getDeviceTypeName(), num);
            } else {
                deviceTypeMap.put(deviceInfo.getDeviceTypeName(), 1);
            }
        }

        DeviceTypeInfo typeInfo = new DeviceTypeInfo("全部", 0);
        typeInfo.setChecked(true);
        deviceTypeInfos.add(typeInfo);
        for (String key : deviceTypeMap.keySet()) {
            typeInfo = new DeviceTypeInfo(key, deviceTypeMap.get(key));
            deviceTypeInfos.add(typeInfo);
        }
        deviceTypeAdapter.notifyDataSetChanged();
    }

    private void filterDeviceDataByType(String typeName) {
        deviceInfoList.clear();
        if (typeName.equals("全部")) {
            deviceInfoList.addAll(tempDeviceInfoList);
            deviceInfoAdapter.notifyDataSetChanged();
            return;
        }

        for (ProjectDeviceInfo deviceInfo : tempDeviceInfoList) {
            if (deviceInfo.getDeviceTypeName().equals(typeName)) {
                deviceInfoList.add(deviceInfo);
            }
        }
        deviceInfoAdapter.notifyDataSetChanged();
    }

}
