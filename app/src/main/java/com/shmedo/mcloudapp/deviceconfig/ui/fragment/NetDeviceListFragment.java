package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import static autodispose2.AutoDispose.autoDisposable;

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

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.toast.ToastUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceOnlineTypeStatistic;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeInfo;
import com.shmedo.mcloudapp.deviceconfig.model.params.QueryDeviceTypeParam;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.projects.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.projects.adapter.DeviceTypeAdapter;
import com.shmedo.mcloudapp.projects.model.PageInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.param.QueryProjectDevice;
import com.shmedo.mcloudapp.projects.ui.activity.DeviceSearchActivity;
import com.shmedo.mcloudapp.projects.view.SlidingConflictRecyclerView;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetDeviceListFragment extends BaseFragment {
    @BindView(R.id.tv_online_num)
    TextView tvOnlineNum;

    @BindView(R.id.tv_offline_num)
    TextView tvOfflineNum;

    @BindView(R.id.tv_online_rate)
    TextView tvOnlineRate;

    @BindView(R.id.recyclerview_device_type)
    SlidingConflictRecyclerView mRecyclerViewDeviceType;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview_device)
    RecyclerView mRecyclerViewDevice;

    private DeviceTypeAdapter deviceTypeAdapter;
    private DeviceInfoAdapter deviceInfoAdapter;
    private List<DeviceOnlineTypeStatistic> deviceTypeStatisticList = new ArrayList<>();
    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();

    private static final int PAGE_SIZE = 30;
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
//        queryDeviceType();
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (companyID != MCloudApp.getCompanyID()) {
//            companyID = MCloudApp.getCompanyID();
//            deviceTypeID = -1;
//            clearData();
//            startLoading();
//            mRefreshLayout.setEnableLoadMore(false);
//            //是否在刷新的时候禁止内容的一切手势操作（默认false）
//            mRefreshLayout.setDisableContentWhenRefresh(true);
//            mRefreshLayout.autoRefresh();
//            queryCompanyDeviceOnlineTypeStatistics();
//        }
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                refreshDevices();
            }
        });
    }

    private void initDeviceTypeAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewDeviceType.setLayoutManager(linearLayoutManager);
//        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.transparent), ConvertUtils.dp2px(1), 0);
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
                mRefreshLayout.autoRefresh();
//                updateTopView(deviceOnlineTypeStatistic);
                refreshDevices();
            }
        });
        mRecyclerViewDeviceType.setAdapter(deviceTypeAdapter);
    }

    private void initDeviceInfoAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
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
                DeviceConfigActivity.startActivity(mActivity, deviceInfo);
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

        String json = GsonUtils.toJson(parameter);
        RequestBody body = RequestBody.create(RequestHeader.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .queryProduct(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<PageResult<DeviceTypeInfo>>() {
                    @Override
                    protected void onResponse(PageResult<DeviceTypeInfo> data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    return;
                                }

                                //更新到本地数据库
                                DaoManager.getInstance().getDaoSession().getDeviceTypeInfoDao().insertOrReplaceInTx(data.getCurrentPageData());
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
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
        RequestBody body = RequestBody.create(RequestHeader.JSON_TYPE, String.valueOf(companyID));
        MDRetrofit.getInstance()
                .createService()
                .getDeviceStatByCompanyID(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<List<DeviceOnlineTypeStatistic>>() {
                    @Override
                    protected void onResponse(List<DeviceOnlineTypeStatistic> data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.size() == 0) {
                                    mRefreshLayout.finishRefresh(false);
                                    showNoContentView(StringUtils.getString(R.string.empty_no_data));
                                    return;
                                }
                                setDeviceTypeData(data);

                            } else {
                                mRefreshLayout.finishRefresh(false);
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                                loadFailed(StringUtils.getString(R.string.fetch_data_failed) + ": " + errorInfo.getCode());
                            }
                        } else {
                            loadFailed(StringUtils.getString(R.string.unknown_error) + ": " + errorInfo.getCode());
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
            mRefreshLayout.finishRefresh(false);
            showNoContentView(StringUtils.getString(R.string.empty_no_data));
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

        String json = GsonUtils.toJson(parameter);
        RequestBody body = RequestBody.create(RequestHeader.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .getDeviceList(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<PageResult<ProjectDeviceInfo>>() {
                    @Override
                    protected void onResponse(PageResult<ProjectDeviceInfo> data, ErrorInfo errorInfo) {
                        mRefreshLayout.finishRefresh();
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
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
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mRefreshLayout.finishRefresh(false);
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

        List<ProjectDeviceInfo> onlineList = new ArrayList<>();
        List<ProjectDeviceInfo> offlineList = new ArrayList<>();
        for (ProjectDeviceInfo deviceInfo : deviceInfoList) {
            if (deviceInfo.isOnline()) {
                onlineList.add(deviceInfo);
            } else {
                offlineList.add(deviceInfo);
            }
        }
        deviceInfoList.clear();
        deviceInfoList.addAll(onlineList);
        deviceInfoList.addAll(offlineList);
    }

    @Override
    protected void loadFailed(String msg) {
        super.loadFailed(msg);
        if (msg == null) {
            mRecyclerViewDeviceType.setVisibility(View.GONE);
            mRefreshLayout.setVisibility(View.GONE);
            mRecyclerViewDevice.setVisibility(View.GONE);
            showBadNetworkView(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoading();
                    mRefreshLayout.autoRefresh();
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
        mRefreshLayout.setVisibility(View.VISIBLE);
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
