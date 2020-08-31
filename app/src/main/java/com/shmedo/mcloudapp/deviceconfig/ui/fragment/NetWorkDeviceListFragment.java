package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceOnlineStatistic;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceOnlineTypeStatistic;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceNetWorkConfigActivity;
import com.shmedo.mcloudapp.entity.PageResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.projects.adapter.DeviceTypeAdapter;
import com.shmedo.mcloudapp.projects.model.PageInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.param.QueryProjectDevice;
import com.shmedo.mcloudapp.projects.ui.activity.DeviceSearchActivity;
import com.shmedo.mcloudapp.projects.view.SlidingConflictRecyclerView;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetWorkDeviceListFragment extends BaseFragment {
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

    private static final int PAGE_SIZE = 10;
    private PageInfo pageInfo;
    private int companyID;
    private int deviceTypeID = -1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_work_device_list;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initUserData();

        pageInfo = new PageInfo(1);
        initRefreshLayout();
        initDeviceTypeAdapter();
        initDeviceInfoAdapter();
        initLoadMore();

        // 进入页面，刷新数据
        queryCompanyDeviceOnlineStatistics();
        queryCompanyDeviceOnlineTypeStatistics();
        swipeRefresh.setRefreshing(true);
        deviceTypeID = -1;
        refresh();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 进入页面，刷新数据
//        swipeRefresh.setRefreshing(true);
//        queryCompanyDeviceOnlineStatistics();
//        queryCompanyDeviceOnlineTypeStatistics();
//        deviceTypeID = -1;
//        refresh();
    }

    private void initUserData() {
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getDepartments() != null && userInfo.getDepartments().size() > 0) {
            companyID = userInfo.getDepartments().get(0).getCompanyID();
        }
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void initDeviceTypeAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewDeviceType.setLayoutManager(linearLayoutManager);
        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.transparent), DensityUtil.Dp2Px(getActivity(), 20), 0);
        mRecyclerViewDeviceType.addItemDecoration(mItemDecoration);
        deviceTypeAdapter = new DeviceTypeAdapter(deviceTypeStatisticList);
        deviceTypeAdapter.setAnimationEnable(true);
        deviceTypeAdapter.setAnimationFirstOnly(false);
        deviceTypeAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                DeviceOnlineTypeStatistic deviceOnlineTypeStatistic = (DeviceOnlineTypeStatistic) deviceTypeStatisticList.get(position);
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
                refresh();
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
                DeviceNetWorkConfigActivity.startActivity(mActivity, deviceInfo);
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

    @OnClick({R.id.search_container})
    public void onClick(View v) {
        if (v.getId() == R.id.search_container) {
            DeviceSearchActivity.startActivity(getActivity());
        }
    }


    /**
     * 刷新
     */
    private void refresh() {
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


    /**
     * 查询公司设备在线统计信息
     */
    private void queryCompanyDeviceOnlineStatistics() {
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, String.valueOf(companyID));
        MDRetrofit.getInstance()
                .createService()
                .QueryCompanyDeviceOnlineStatistics(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<DeviceOnlineStatistic>() {
                    @Override
                    public void Success(DeviceOnlineStatistic data, String message) {
                        updateTopView(data);
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("服务器连接失败--%s", message);
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
                    public void Success(List<DeviceOnlineTypeStatistic> data, String message) {
                        setDeviceTypeData(data);
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("服务器连接失败--%s", message);
                    }
                });
    }

    private void queryCompanyDevice() {
        QueryProjectDevice parameter = new QueryProjectDevice();
        parameter.setCompanyID(companyID);
        parameter.setDeviceType(deviceTypeID);
        parameter.setDeviceStatus("启用");
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
                    public void Success(PageResult<ProjectDeviceInfo> data, String message) {
                        swipeRefresh.setRefreshing(false);
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);

                        if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                            return;
                        }

                        if (pageInfo.isFirstPage()) {
                            //如果是加载的第一页数据，用setNew
                            deviceInfoList.clear();
                            deviceInfoList.addAll(data.getCurrentPageData());
                            deviceInfoAdapter.notifyDataSetChanged();
                        } else {
                            //不是第一页，则用add
                            deviceInfoList.addAll(data.getCurrentPageData());
                            deviceInfoAdapter.notifyDataSetChanged();
                        }

                        if (data.getCurrentPageData().size() < PAGE_SIZE) {
                            //如果不够一页,显示没有更多数据布局
                            deviceInfoAdapter.getLoadMoreModule().loadMoreEnd();

                        } else {
                            deviceInfoAdapter.getLoadMoreModule().loadMoreComplete();
                        }
                        // page加一
                        pageInfo.nextPage();
                    }

                    @Override
                    public void Failure(String message) {
                        Timber.w("请求失败--%s", message);
                        swipeRefresh.setRefreshing(false);
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        deviceInfoAdapter.getLoadMoreModule().loadMoreFail();
                    }
                });
    }

    private void updateTopView(DeviceOnlineStatistic deviceOnlineStatistic) {
        if (deviceOnlineStatistic == null) {
            return;
        }
        tvOnlineNum.setText(String.valueOf(deviceOnlineStatistic.getOnlineCount()));
        tvOfflineNum.setText(String.valueOf(deviceOnlineStatistic.getOfflineCount()));

        DecimalFormat df = new DecimalFormat("#.#");//格式化小数
        String rate = df.format(deviceOnlineStatistic.getOnlinePercent() * 100) + "%";
        SpannableString spannableString = new SpannableString(rate);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(18, true);
        spannableString.setSpan(absoluteSizeSpan, rate.indexOf("%"), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvOnlineRate.setText(new SpannedString(spannableString));
    }

    private void setDeviceTypeData(List<DeviceOnlineTypeStatistic> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        deviceTypeStatisticList.clear();
        DeviceOnlineTypeStatistic deviceOnlineTypeStatistic = new DeviceOnlineTypeStatistic();
        deviceOnlineTypeStatistic.setDeviceTypeName("全部");
        deviceOnlineTypeStatistic.setDeviceTypeID(-1);
        deviceOnlineTypeStatistic.setChecked(true);
        deviceTypeStatisticList.add(deviceOnlineTypeStatistic);
        deviceTypeStatisticList.addAll(dataList);
        deviceTypeAdapter.notifyDataSetChanged();
    }


}
