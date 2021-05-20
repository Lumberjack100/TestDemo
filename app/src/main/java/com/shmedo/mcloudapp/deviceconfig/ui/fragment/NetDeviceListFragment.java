package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceOnlineTypeStatistic;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeInfo;
import com.shmedo.mcloudapp.deviceconfig.model.params.QueryDeviceTypeParam;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.projects.adapter.DeviceTypeAdapter;
import com.shmedo.mcloudapp.projects.model.PageInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.param.QueryProjectDevice;
import com.shmedo.mcloudapp.projects.ui.activity.DeviceSearchActivity;
import com.shmedo.mcloudapp.projects.view.SlidingConflictRecyclerView;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetDeviceListFragment extends BaseFragment {

    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.tv_online_num)
    TextView tvOnlineNum;

    @BindView(R.id.tv_offline_num)
    TextView tvOfflineNum;

    @BindView(R.id.tv_online_rate)
    TextView tvOnlineRate;

    @BindView(R.id.recyclerview_device_type)
    SlidingConflictRecyclerView mRecyclerViewDeviceType;

    @BindView(R.id.recyclerview_device)
    RecyclerView mRecyclerViewDevice;

    private DeviceTypeAdapter deviceTypeAdapter;
    private DeviceInfoAdapter deviceInfoAdapter;
    private List<DeviceOnlineTypeStatistic> deviceTypeStatisticList = new ArrayList<>();
    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();

    private static final int PAGE_SIZE = 15;
    private PageInfo pageInfo;
    private int companyID = -100;
    private int deviceTypeID = -1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_work_device_list;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pageInfo = new PageInfo(1);
        initDeviceTypeAdapter();
        initDeviceInfoAdapter();
        initRefreshLayout();
        initLoadMore();

        // 进入页面，刷新数据
        queryDeviceType();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (companyID != MCloudApp.getCompanyID()) {
            companyID = MCloudApp.getCompanyID();
            deviceTypeID = -1;
            clearData();
            startLoading();
            swipeRefresh.setRefreshing(true);
            queryCompanyDeviceOnlineTypeStatistics();
        }
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDevices();
            }
        });
    }

    private void initDeviceTypeAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewDeviceType.setLayoutManager(linearLayoutManager);
//        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.transparent), DensityUtil.Dp2Px(getActivity(), 1), 0);
//        mRecyclerViewDeviceType.addItemDecoration(mItemDecoration);
        deviceTypeAdapter = new DeviceTypeAdapter(deviceTypeStatisticList);
        deviceTypeAdapter.setAnimationEnable(true);
        deviceTypeAdapter.setAnimationFirstOnly(false);
        deviceTypeAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                DeviceOnlineTypeStatistic deviceOnlineTypeStatistic = deviceTypeStatisticList.get(position);
                if (deviceOnlineTypeStatistic.isChecked()) {
                    return;
                }
                for (DeviceOnlineTypeStatistic typeInfo : deviceTypeStatisticList) {
                    typeInfo.setChecked(false);
                }
                deviceOnlineTypeStatistic.setChecked(true);
                deviceTypeAdapter.notifyDataSetChanged();

                //点击选中最后一个 Item 时,使RecyclerView滚动到底
                if (position == deviceTypeStatisticList.size() - 1) {
                    mRecyclerViewDeviceType.scrollToPosition(adapter.getItemCount() - 1);
                }

                deviceTypeID = deviceOnlineTypeStatistic.getDeviceTypeID();
                swipeRefresh.setRefreshing(true);
//                updateTopView(deviceOnlineTypeStatistic);
                refreshDevices();
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
                ProjectDeviceInfo deviceInfo = deviceInfoList.get(position);
                int deviceType = AppContants.DeviceType.DAS;
                if (deviceInfo.getDeviceTypeName().contains("DAS")) {
                    deviceType = AppContants.DeviceType.DAS;
                } else if (deviceInfo.getDeviceTypeName().contains("ADME")) {
                    ToastUtils.show("此设备暂不支持网络配置");
                    return;
                } else if (deviceInfo.getDeviceTypeName().contains("M20")) {
                    deviceType = AppContants.DeviceType.M20;
                } else if (deviceInfo.getDeviceTypeName().contains("E40") || deviceInfo.getDeviceTypeName().contains("E60")) {
                    deviceType = AppContants.DeviceType.E40;
                } else if (deviceInfo.getDeviceTypeName().contains("VMS")) {
                    deviceType = AppContants.DeviceType.VMS;
                }
                DeviceConfigActivity.startActivity(mActivity, deviceInfo, deviceType);
            }
        });
        mRecyclerViewDevice.setAdapter(deviceInfoAdapter);
    }

    /**
     * 初始化加载更多
     */
    private void initLoadMore() {
        deviceInfoAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
        // 是否自定加载下一页（默认为true）
        deviceInfoAdapter.getLoadMoreModule().setAutoLoadMore(true);
        // 当数据不满一页时，是否继续自动加载（默认为true）
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
    }

    @OnClick({R.id.search_placeholder})
    public void onClick(View v) {
        if (v.getId() == R.id.search_placeholder) {
            DeviceSearchActivity.startActivity(getActivity());
        }
    }

    /**
     * 刷新
     */
    private void refreshDevices() {
        // 这里的作用是防止下拉刷新的时候还可以上拉加载
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(false);
        // 下拉刷新，需要重置页数
        pageInfo.reset();
        queryCompanyDevice();
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        queryCompanyDevice();
    }

    private void processOnlineData(DeviceOnlineTypeStatistic deviceOnlineTypeStatistic) {
        int onlineCount = 0;
        int offlineCount = 0;

        if (deviceOnlineTypeStatistic.getDeviceTypeName().equals("全部")) {
            for (DeviceOnlineTypeStatistic typeStatistic : deviceTypeStatisticList) {
                if (typeStatistic.getDeviceTypeName().equals("全部")) {
                    continue;
                }
                onlineCount += typeStatistic.getOnlineCount();
                offlineCount += typeStatistic.getOfflineCount();
            }
        } else {
            onlineCount = deviceOnlineTypeStatistic.getOnlineCount();
            offlineCount = deviceOnlineTypeStatistic.getOfflineCount();
        }

        DecimalFormat df = new DecimalFormat("#.#");//格式化小数
        String rate;
        if ((onlineCount + offlineCount) == 0) {
            rate = "0%";
        } else {
            rate = df.format((float) onlineCount / (onlineCount + offlineCount) * 100) + "%";
        }

        updateTopView(onlineCount, offlineCount, rate);
    }

    private void updateTopView(int onlineCount, int offlineCount, String rate) {
        tvOnlineNum.setText(String.valueOf(onlineCount));
        tvOfflineNum.setText(String.valueOf(offlineCount));

        SpannableString spannableString = new SpannableString(rate);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(18, true);
        spannableString.setSpan(absoluteSizeSpan, rate.indexOf("%"), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvOnlineRate.setText(new SpannedString(spannableString));
    }

    /**
     * 查询设备类型列表
     */
    private void queryDeviceType() {
        QueryDeviceTypeParam parameter = new QueryDeviceTypeParam();
        parameter.setDeviceTypeName(null);
        parameter.setPageSize(20);
        parameter.setCurrentPage(1);

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryDeviceType(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<DeviceTypeInfo>>() {
                    @Override
                    protected void onResponse(PageResult<DeviceTypeInfo> data, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    return;
                                }

                                //更新到本地数据库
                                DaoManager.getInstance().getDaoSession().getDeviceTypeInfoDao().insertOrReplaceInTx(data.getCurrentPageData());
                            } else {
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    /**
     * 查询公司设备类型在线统计信息
     */
    private void queryCompanyDeviceOnlineTypeStatistics() {
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, String.valueOf(companyID));
        MDRetrofit.getInstance()
                .createService()
                .QueryCompanyDeviceOnlineTypeStatistics(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<DeviceOnlineTypeStatistic>>() {
                    @Override
                    protected void onResponse(List<DeviceOnlineTypeStatistic> data, ErrCode errCode) {
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.size() == 0) {
                                    swipeRefresh.setRefreshing(false);
                                    showNoContentView(GlobalUtil.getString(R.string.empty_no_data));
                                    return;
                                }
                                setDeviceTypeData(data);

                            } else {
                                swipeRefresh.setRefreshing(false);
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                                loadFailed(GlobalUtil.getString(R.string.fetch_data_failed) + ": " + errCode.getCode());
                            }
                        } else {
                            loadFailed(GlobalUtil.getString(R.string.unknown_error) + ": " + errCode.getCode());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                        loadFailed(null);
                    }
                });
    }

    /**
     * 过滤掉不支持物联网协议的设备
     *
     * @param dataList
     */
    private void setDeviceTypeData(List<DeviceOnlineTypeStatistic> dataList) {
        deviceTypeStatisticList.clear();
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        DeviceOnlineTypeStatistic deviceOnlineTypeStatistic = new DeviceOnlineTypeStatistic();
        deviceOnlineTypeStatistic.setDeviceTypeName("全部");
        deviceOnlineTypeStatistic.setDeviceTypeID(-1);
        deviceOnlineTypeStatistic.setChecked(true);
        deviceTypeStatisticList.add(deviceOnlineTypeStatistic);

        for (DeviceOnlineTypeStatistic typeStatistic : dataList) {
            //去除不支持物联网协议的 DAG、TPS、VIR 等设备
            if (typeStatistic.getDeviceTypeID() == 5 || typeStatistic.getDeviceTypeID() == 8
                    || typeStatistic.getDeviceTypeID() == 9
                    || typeStatistic.getDeviceTypeID() == 11
                    || typeStatistic.getDeviceTypeID() == 12
                    || typeStatistic.getDeviceTypeID() == 15) {
                continue;
            }

            deviceTypeStatisticList.add(typeStatistic);
        }
        deviceTypeAdapter.notifyDataSetChanged();
        if (deviceTypeStatisticList.size() <= 1) {
            swipeRefresh.setRefreshing(false);
            showNoContentView(GlobalUtil.getString(R.string.empty_no_data));
        } else {
            processOnlineData(deviceTypeStatisticList.get(0));
            refreshDevices();
        }
    }

    /**
     * 查询公司设备列表
     */
    private void queryCompanyDevice() {
        QueryProjectDevice parameter = new QueryProjectDevice();
        parameter.setCompanyID(companyID);
        parameter.setDeviceType(deviceTypeID);
//        parameter.setDeviceStatus("启用");
        parameter.setPageSize(PAGE_SIZE);
        parameter.setCurrentPage(pageInfo.getPage());

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryCompanyDevice(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<ProjectDeviceInfo>>() {
                    @Override
                    protected void onResponse(PageResult<ProjectDeviceInfo> data, ErrCode errCode) {
                        swipeRefresh.setRefreshing(false);
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    if (deviceInfoList.size() == 0) {
                                        deviceInfoAdapter.setEmptyView(R.layout.empty_view);
                                    } else {
                                        //显示没有更多数据布局
                                        deviceInfoAdapter.getLoadMoreModule().loadMoreEnd();
                                    }
                                    return;
                                }

                                //如果是加载的第一页数据，清空列表
                                if (pageInfo.isFirstPage()) {
                                    deviceInfoList.clear();
                                }
                                filterIOTProtocolDevices(data.getCurrentPageData());
                                deviceInfoAdapter.notifyDataSetChanged();

                                if (data.getCurrentPageData().size() < PAGE_SIZE) {
                                    //如果不够一页,显示没有更多数据布局
                                    deviceInfoAdapter.getLoadMoreModule().loadMoreEnd();

                                } else {
                                    deviceInfoAdapter.getLoadMoreModule().loadMoreComplete();
                                }
                                // page加一
                                pageInfo.nextPage();
                            } else {
                                deviceInfoAdapter.getLoadMoreModule().loadMoreFail();
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefresh.setRefreshing(false);
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        deviceInfoAdapter.getLoadMoreModule().loadMoreFail();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }


    /**
     * 筛选出支持米度物联网协议的设备
     */
    private void filterIOTProtocolDevices(List<ProjectDeviceInfo> deviceInfos) {
        for (ProjectDeviceInfo deviceInfo : deviceInfos) {
            //去除不支持物联网协议的 DAG(5)、PVS(8)、VIR(9)、智能化设备(11)、专业监测设备(12)、智能声光报警器(15)
            if (deviceInfo.getDeviceTypeID() == 5
                    || deviceInfo.getDeviceTypeID() == 8
                    || deviceInfo.getDeviceTypeID() == 9
                    || deviceInfo.getDeviceTypeID() == 11
                    || deviceInfo.getDeviceTypeID() == 12
                    || deviceInfo.getDeviceTypeID() == 15) {
                continue;
            }

            deviceInfoList.add(deviceInfo);
        }
    }

    @Override
    protected void loadFailed(String msg) {
        super.loadFailed(msg);
        if (msg == null) {
            mRecyclerViewDeviceType.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.GONE);
            mRecyclerViewDevice.setVisibility(View.GONE);
            showBadNetworkView(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoading();
                    swipeRefresh.setRefreshing(true);
                    queryCompanyDeviceOnlineTypeStatistics();
                }
            });
        } else {
            showLoadErrorView(msg);
        }
    }

    /**
     * 加载feeds完成，将feeds显示出来，将加载等待控件隐藏。
     */
    @Override
    protected void loadFinished() {
        super.loadFinished();
        mRecyclerViewDeviceType.setVisibility(View.VISIBLE);
        swipeRefresh.setVisibility(View.VISIBLE);
        mRecyclerViewDevice.setVisibility(View.VISIBLE);
    }

    private void clearData() {
        updateTopView(0, 0, "0%");

        deviceTypeStatisticList.clear();
        deviceInfoList.clear();
        deviceTypeAdapter.notifyDataSetChanged();
        deviceInfoAdapter.notifyDataSetChanged();
    }
}
