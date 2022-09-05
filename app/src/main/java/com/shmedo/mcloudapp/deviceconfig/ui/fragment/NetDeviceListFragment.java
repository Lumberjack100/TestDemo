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
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.adapter.ProductAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceStatisticInfo;
import com.shmedo.mcloudapp.deviceconfig.model.PageInfo;
import com.shmedo.mcloudapp.deviceconfig.model.ProductInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceSearchActivity;
import com.shmedo.mcloudapp.deviceconfig.view.SlidingConflictRecyclerView;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ResponseWrapper;
import com.shmedo.mcloudapp.network.ServiceAddressType;
import com.shmedo.mcloudapp.network.api.ApiService;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 4G模式下设备列表页面
 */
public class NetDeviceListFragment extends BaseFragment {
    @BindView(R.id.tv_online_num)
    TextView tvOnlineNum;//在线设备

    @BindView(R.id.tv_offline_num)
    TextView tvOfflineNum;//离线设备

    @BindView(R.id.tv_online_rate)
    TextView tvOnlineRate;//在线率

    @BindView(R.id.recyclerview_device_type)
    SlidingConflictRecyclerView mRecyclerViewProduct;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.recyclerview_device)
    RecyclerView mRecyclerViewDevice;

    private ProductAdapter productAdapter;
    private List<ProductInfo> productList = new ArrayList<>();

    private DeviceInfoAdapter deviceInfoAdapter;
    private List<DeviceInfo> deviceInfoList = new ArrayList<>();

    private static final int PAGE_SIZE = 30;
    private PageInfo pageInfo = new PageInfo(1);
    private int companyID = -100;
    private int productID = -1;
    private boolean isHasListSuperInfoPermission = false;//是否具有 ListSuperInfo 系统权限

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_device_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initProductAdapter();
        initDeviceInfoAdapter();
        initRefreshLayout();
        initLoadMore();
        if (MCloudApp.getPermissionNameList().contains("ListSuperInfo"))
            isHasListSuperInfoPermission = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (companyID != MCloudApp.getCompanyID()) {
            companyID = MCloudApp.getCompanyID();
            loadAllData();
        }
    }

    private void loadAllData() {
        productID = -1;
        startLoading();
        //查询设备在线统计信息
        getDeviceStatByCompanyID();
        //查询产品列表
        queryProducts();
        //刷新设备列表
        mRefreshLayout.autoRefresh();
    }

    /**
     * 初始化产品列表适配器
     */
    private void initProductAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewProduct.setLayoutManager(linearLayoutManager);
        productAdapter = new ProductAdapter(productList);
        productAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ProductInfo productInfo = productList.get(position);
                if (productInfo.isChecked()) {
                    return;
                }
                //清除上一次选中项目的状态
                for (int i = 0; i < productList.size(); i++) {
                    ProductInfo info = productList.get(i);
                    if (info.getId() == productID) {
                        info.setChecked(false);
                        productAdapter.notifyItemChanged(i);
                        break;
                    }
                }
                //更新新选中项目的状态
                productInfo.setChecked(true);
                productAdapter.notifyItemChanged(position);

                int lastVisibleItemPosition = ((LinearLayoutManager) mRecyclerViewProduct.getLayoutManager()).findLastVisibleItemPosition();
                //点击选中最后一个 Item 时,使RecyclerView滚动到底
                if (position == lastVisibleItemPosition) {
                    mRecyclerViewProduct.scrollToPosition(lastVisibleItemPosition);
                }
                productID = productInfo.getId();
                mRefreshLayout.autoRefresh();
            }
        });
        mRecyclerViewProduct.setAdapter(productAdapter);
    }

    /**
     * 初始化设备列表适配器
     */
    private void initDeviceInfoAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(10);//每一个矩形的间距
        mRecyclerViewDevice.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        //设置每个item间距
        mRecyclerViewDevice.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        deviceInfoAdapter = new DeviceInfoAdapter(deviceInfoList);
//        deviceInfoAdapter.setAnimationEnable(true);
//        deviceInfoAdapter.setAnimationFirstOnly(false);
        deviceInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                DeviceInfo deviceInfo = deviceInfoList.get(position);
                DeviceConfigActivity.startActivity(mActivity, deviceInfo);
            }
        });
        mRecyclerViewDevice.setAdapter(deviceInfoAdapter);
        mRecyclerViewDevice.setHasFixedSize(true);
    }

    private void initRefreshLayout() {
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                deviceInfoList.clear();
                deviceInfoAdapter.notifyDataSetChanged();
                // 这里的作用是防止下拉刷新的时候还可以上拉加载
                deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(false);
                //下拉刷新，需要重置页数
                pageInfo.reset();
                queryDeviceList();
            }
        });
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
        ApiService apiService = MDRetrofit.getInstance().createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS);
        Observable<ResponseWrapper<DeviceStatisticInfo>> observable = isHasListSuperInfoPermission ? apiService.listSuperDeviceStat(MCloudApp.getAccessToken(), body) : apiService.getDeviceStatByCompanyID(MCloudApp.getAccessToken(), body);
        observable.doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<DeviceStatisticInfo>() {
                    @Override
                    protected void onResponse(DeviceStatisticInfo data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data != null) {
                                    DecimalFormat df = new DecimalFormat("#.##");//格式化小数
                                    String rate = df.format(data.getOnlinePercent()) + "%";
                                    updateTopView(data.getOnlineCount(), data.getOfflineCount(), rate);
                                }
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
     * 查询产品或者系统所有产品列表
     */
    private void queryProducts() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            if (!isHasListSuperInfoPermission)
                jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            jsonObjectRequest.put("pageSize", 100);
            jsonObjectRequest.put("currentPage", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);
        ApiService apiService = MDRetrofit.getInstance().createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS);
        Observable<ResponseWrapper<PageResult<ProductInfo>>> observable = isHasListSuperInfoPermission ? apiService.listSuperProduct(MCloudApp.getAccessToken(), body) : apiService.queryProduct(MCloudApp.getAccessToken(), body);
        observable.doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<PageResult<ProductInfo>>() {
                    @Override
                    protected void onResponse(PageResult<ProductInfo> data, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    mRefreshLayout.finishRefresh(false);
                                    showNoContentView(StringUtils.getString(R.string.empty_no_data));
                                    return;
                                }
                                loadFinished();
                                initProducts(data.getCurrentPageData());

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

    private void initProducts(List<ProductInfo> dataList) {
        productList.clear();
        for (ProductInfo info : dataList) {
            //过滤掉没有设备的产品
            if (info.getDeviceNum() == 0)
                continue;

            ProductType type = ProductType.valueByPrefix(info.getProductToken().toUpperCase());
            if (type == ProductType.UnKnown) {
                continue;
            }
            productList.add(info);
        }
        if (productList.size() == 0) {
            mRefreshLayout.finishRefresh(false);
            showNoContentView(StringUtils.getString(R.string.empty_no_data));
            return;
        }
        //安装产品名排序
        Collections.sort(productList);

        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductName("全部");
        productInfo.setId(-1);
        productInfo.setChecked(true);
        productList.add(0, productInfo);
        productAdapter.notifyDataSetChanged();
        mRecyclerViewProduct.scrollToPosition(0);
    }

    /**
     * 查询公司或者系统所有设备列表
     */
    private void queryDeviceList() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            jsonObjectRequest.put("productID", productID == -1 ? "" : productID);
            jsonObjectRequest.put("tokenAndVersion", false);
            jsonObjectRequest.put("deviceStatus", "启用");
            jsonObjectRequest.put("currentPage", pageInfo.getPage());
            jsonObjectRequest.put("pageSize", PAGE_SIZE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);
        ApiService apiService = MDRetrofit.getInstance().createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS);
        Observable<ResponseWrapper<PageResult<DeviceInfo>>> observable = isHasListSuperInfoPermission ? apiService.listSuperDevice(MCloudApp.getAccessToken(), body) : apiService.getDeviceList(MCloudApp.getAccessToken(), body);
        observable.doOnDispose(() -> Timber.i("Disposing subscription"))
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
                                        deviceInfoAdapter.setEmptyView(R.layout.empty_view);
                                        deviceInfoAdapter.notifyDataSetChanged();
                                    } else {
                                        //显示没有更多数据布局
                                        deviceInfoAdapter.getLoadMoreModule().loadMoreEnd();
                                    }
                                    return;
                                }
                                loadFinished();
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
     * 在线、离线排序加载
     */
    private void filterDevices(List<DeviceInfo> tempList) {
        deviceInfoList.addAll(tempList);
        deviceInfoAdapter.notifyItemRangeInserted(deviceInfoList.size() - tempList.size(), tempList.size());
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
        mRecyclerViewProduct.setVisibility(View.VISIBLE);
        mRecyclerViewDevice.setVisibility(View.VISIBLE);
    }

    @Override
    protected void loadFailed(String msg) {
        super.loadFailed(msg);
        if (msg == null) {
            if (mRefreshLayout.isRefreshing())
                mRefreshLayout.finishRefresh(false);
            mRefreshLayout.setVisibility(View.GONE);
            mRecyclerViewProduct.setVisibility(View.GONE);
            mRecyclerViewDevice.setVisibility(View.GONE);
            showBadNetworkView(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAllData();
                }
            });
        } else {
            showLoadErrorView(msg);
        }
    }

    private void updateTopView(int onlineCount, int offlineCount, String rate) {
        tvOnlineNum.setText(String.valueOf(onlineCount));
        tvOfflineNum.setText(String.valueOf(offlineCount));
        SpannableString spannableString = new SpannableString(rate);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(18, true);
        spannableString.setSpan(absoluteSizeSpan, rate.indexOf("%"), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvOnlineRate.setText(new SpannedString(spannableString));
    }

    @OnClick({R.id.search_placeholder})
    public void onClick(View v) {
        if (v.getId() == R.id.search_placeholder) {
            DeviceSearchActivity.startActivity(getActivity());
        }
    }
}
