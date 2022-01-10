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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
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
import com.shmedo.mcloudapp.deviceconfig.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceStatisticInfo;
import com.shmedo.mcloudapp.deviceconfig.model.PageInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceSearchActivity;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ServiceAddressType;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

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
 * 4G模式下设备列表页面
 */
public class NetDeviceListFragment extends BaseFragment {
    @BindView(R.id.tv_online_num)
    TextView tvOnlineNum;

    @BindView(R.id.tv_offline_num)
    TextView tvOfflineNum;

    @BindView(R.id.tv_online_rate)
    TextView tvOnlineRate;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview_device)
    RecyclerView mRecyclerViewDevice;

    private DeviceInfoAdapter deviceInfoAdapter;
    private List<DeviceInfo> deviceInfoList = new ArrayList<>();

    private static final int PAGE_SIZE = 30;
    private PageInfo pageInfo;
    private int companyID = -100;
    private int productID = -1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_device_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pageInfo = new PageInfo(1);
        initDeviceInfoAdapter();
        initLoadMore();
        initRefreshLayout();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (companyID != MCloudApp.getCompanyID()) {
            companyID = MCloudApp.getCompanyID();
            clearData();
            startLoading();
            mRefreshLayout.setEnableLoadMore(false);
            //是否在刷新的时候禁止内容的一切手势操作（默认false）
            mRefreshLayout.setDisableContentWhenRefresh(true);
            mRefreshLayout.autoRefresh();
        }
    }

    private void initDeviceInfoAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(10);//每一个矩形的间距
        mRecyclerViewDevice.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewDevice.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        deviceInfoAdapter = new DeviceInfoAdapter(deviceInfoList);
        deviceInfoAdapter.setAnimationEnable(true);
        deviceInfoAdapter.setAnimationFirstOnly(false);
        deviceInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                DeviceInfo deviceInfo = deviceInfoList.get(position);
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
                queryDeviceList();
            }
        });
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
        // 是否自定加载下一页（默认为true）
        deviceInfoAdapter.getLoadMoreModule().setAutoLoadMore(true);
        // 当数据不满一页时，是否继续自动加载（默认为true）
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                refreshDevices();
            }
        });
    }

    @OnClick({R.id.search_placeholder})
    public void onClick(View v) {
        if (v.getId() == R.id.search_placeholder) {
            DeviceSearchActivity.startActivity(getActivity());
        }
    }

    /**
     * 下拉刷新
     */
    private void refreshDevices() {
        getDeviceStatByCompanyID();

        // 这里的作用是防止下拉刷新的时候还可以上拉加载
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(false);
        // 下拉刷新，需要重置页数
        pageInfo.reset();
        queryDeviceList();
    }

    /**
     * 查询公司设备在线统计信息
     */
    private void getDeviceStatByCompanyID() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);
        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .getDeviceStatByCompanyID(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<DeviceStatisticInfo>() {
                    @Override
                    protected void onResponse(DeviceStatisticInfo data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                DecimalFormat df = new DecimalFormat("#.##");//格式化小数
                                String rate = df.format(data.getOnlinePercent()) + "%";
                                updateTopView(data.getOnlineCount(), data.getOfflineCount(), rate);
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
     * 查询公司设备列表
     */
    private void queryDeviceList() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            jsonObjectRequest.put("productID", productID == -1 ? "" : productID);
            jsonObjectRequest.put("tokenAndVersion", false);
            jsonObjectRequest.put("pageSize", PAGE_SIZE);
            jsonObjectRequest.put("currentPage", pageInfo.getPage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS)
                .getDeviceList(MCloudApp.getAccessToken(), body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<PageResult<DeviceInfo>>() {
                    @Override
                    protected void onResponse(PageResult<DeviceInfo> data, ErrorInfo errorInfo) {
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (mRefreshLayout.isRefreshing())
                                    mRefreshLayout.finishRefresh();
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    if (deviceInfoList.size() == 0) {
                                        showNoContentView(StringUtils.getString(R.string.empty_no_data));
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
                                filterDevices(data.getCurrentPageData());

                            } else { //Code!=0
                                if (mRefreshLayout.isRefreshing())
                                    mRefreshLayout.finishRefresh(false);
                                deviceInfoAdapter.getLoadMoreModule().loadMoreFail();
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
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
     * 安装在线、离线排序加载
     */
    private void filterDevices(List<DeviceInfo> tempList) {
        deviceInfoList.addAll(tempList);
        deviceInfoAdapter.notifyDataSetChanged();
        if (tempList.size() < PAGE_SIZE) {
            //如果不够一页,显示没有更多数据布局
            deviceInfoAdapter.getLoadMoreModule().loadMoreEnd();
        } else {
            deviceInfoAdapter.getLoadMoreModule().loadMoreComplete();
        }
        // page加一
        pageInfo.nextPage();
    }

    /**
     * 加载完成
     */
    @Override
    protected void loadFinished() {
        super.loadFinished();
        mRefreshLayout.setVisibility(View.VISIBLE);
        mRecyclerViewDevice.setVisibility(View.VISIBLE);
    }

    @Override
    protected void loadFailed(String msg) {
        super.loadFailed(msg);
        if (msg == null) {
            if (mRefreshLayout.isRefreshing())
                mRefreshLayout.finishRefresh(false);
            mRefreshLayout.setVisibility(View.GONE);
            mRecyclerViewDevice.setVisibility(View.GONE);
            showBadNetworkView(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoading();
                    mRefreshLayout.autoRefresh();
                }
            });
        } else {
            showLoadErrorView(msg);
        }
    }

    private void clearData() {
        updateTopView(0, 0, "0%");
        productID = -1;
        deviceInfoList.clear();
        deviceInfoAdapter.notifyDataSetChanged();
    }

    private void updateTopView(int onlineCount, int offlineCount, String rate) {
        tvOnlineNum.setText(String.valueOf(onlineCount));
        tvOfflineNum.setText(String.valueOf(offlineCount));
        SpannableString spannableString = new SpannableString(rate);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(18, true);
        spannableString.setSpan(absoluteSizeSpan, rate.indexOf("%"), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvOnlineRate.setText(new SpannedString(spannableString));
    }
}
