package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.adapter.SearchHistoryAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.PageInfo;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.SearchViewModel;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ResponseWrapper;
import com.shmedo.mcloudapp.network.ServiceAddressType;
import com.shmedo.mcloudapp.network.api.ApiService;
import com.shmedo.mcloudapp.util.CacheUtil;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

public class DeviceSearchActivity extends BaseActivity {
    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.search_historyRv)
    RecyclerView mRvSearchHistory;

    @BindView(R.id.scrollView)
    NestedScrollView scrollViewHistory;

    private SearchHistoryAdapter searchHistoryAdapter;
    private List<String> historyList = new ArrayList<>();

    private DeviceInfoAdapter deviceInfoAdapter;
    private List<DeviceInfo> deviceInfoList = new ArrayList<>();

    private SearchViewModel searchViewModel;


    private static final int PAGE_SIZE = 10;
    private PageInfo pageInfo = new PageInfo(1);
    private String keyWords;// 要输入的搜索关键字

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, DeviceSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSearchView();
        initHistoryAdapter();
        initDeviceInfoAdapter();
        initLoadMore();
        initViewModel();
        setHistoryKeyWordsVisibility(true);
    }

    private void initSearchView() {
        mEtKeyWords.setHint("设备SN搜索");
        mEtKeyWords.requestFocus();
        mEtKeyWords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(textView.getText())) {
                        ToastUtils.show("请输入搜索内容");
                    } else {
                        keyWords = textView.getText().toString();
                        //当按了搜索之后关闭软键盘
                        com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
                        updateKey();
                        refresh();
                    }
                    return true;
                }
                return false;
            }
        });
        mEtKeyWords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(String.valueOf(s))) {
                    setHistoryKeyWordsVisibility(true);
                }
            }
        });
    }

    private void initHistoryAdapter() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        //方向 主轴为水平方向，起点在左端
        layoutManager.setFlexDirection(FlexDirection.ROW);
        //左对齐
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        mRvSearchHistory.setLayoutManager(layoutManager);
        mRvSearchHistory.setHasFixedSize(false);
        mRvSearchHistory.setNestedScrollingEnabled(false);
        searchHistoryAdapter = new SearchHistoryAdapter(historyList);
        searchHistoryAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                keyWords = historyList.get(position);
                mEtKeyWords.setText(keyWords);
                mEtKeyWords.setSelection(keyWords.length());
                //关闭软键盘
                com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
                updateKey();
                refresh();
            }
        });
        mRvSearchHistory.setAdapter(searchHistoryAdapter);
    }

    private void initDeviceInfoAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        deviceInfoAdapter = new DeviceInfoAdapter(deviceInfoList);
        deviceInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                DeviceInfo deviceInfo = deviceInfoList.get(position);
                DeviceConfigActivity.startActivity(DeviceSearchActivity.this, deviceInfo);
            }
        });
        mRecyclerView.setAdapter(deviceInfoAdapter);
        mRecyclerView.setHasFixedSize(true);
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
        //是否自动加载下一页（默认为true）
        deviceInfoAdapter.getLoadMoreModule().setAutoLoadMore(true);
        //当自动加载开启，同时数据不满一屏时，是否继续执行自动加载更多(默认为true)）
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
    }

    private void initViewModel() {
        searchViewModel = getActivityScopeViewModel(SearchViewModel.class);
        searchViewModel.getHistoryData().observe(this, strings -> {
            historyList.clear();
            historyList.addAll(strings);
            searchHistoryAdapter.notifyDataSetChanged();
            CacheUtil.INSTANCE.setSearchHistoryData(GsonUtils.toJson(strings));
        });
        //加载搜索历史数据
        searchViewModel.requestHistoryData();
    }

    /**
     * 更新搜索词
     */
    private void updateKey() {
        ArrayList<String> tempList = searchViewModel.getHistoryData().getValue();
        if (tempList != null) {
            //当搜索历史中包含该数据时 删除
            if (tempList.contains(keyWords))
                tempList.remove(keyWords);
            //如果集合的size 有15个以上了，删除最后一个
            if (tempList.size() > 25)
                tempList.remove(tempList.size() - 1);

            //添加新数据到第一条
            tempList.add(0, keyWords);
            searchViewModel.setHistoryData(tempList);
        }
    }

    private void setHistoryKeyWordsVisibility(boolean isShow) {
        if (isShow) {
            mRecyclerView.setVisibility(View.GONE);
            scrollViewHistory.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            scrollViewHistory.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.iv_back, R.id.search_clear, R.id.tv_search})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_back) {//
            finish();
        } else if (id == R.id.search_clear) {//
            searchViewModel.setHistoryData(new ArrayList<>());

        } else if (id == R.id.tv_search) {
            if (TextUtils.isEmpty(String.valueOf(mEtKeyWords.getText()).trim())) {
                ToastUtils.show("请输入搜索内容");
                return;
            }
            keyWords = mEtKeyWords.getText().toString();
            // 当按了搜索之后关闭软键盘
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
            updateKey();
            refresh();
        }
    }

    private void refresh() {
        setHistoryKeyWordsVisibility(false);
        deviceInfoList.clear();
        deviceInfoAdapter.notifyDataSetChanged();
        //这里的作用是防止下拉刷新的时候还可以上拉加载
        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(false);
        //下拉刷新，需要重置页数
        pageInfo.reset();
        deviceInfoAdapter.setEmptyView(R.layout.loading_view);
        queryDeviceList();
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        queryDeviceList();
    }

    private void queryDeviceList() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("companyID", MCloudApp.getCompanyID());
            jsonObjectRequest.put("deviceToken", keyWords.trim().toUpperCase());
            jsonObjectRequest.put("tokenAndVersion", false);
            jsonObjectRequest.put("deviceStatus", "启用");
            jsonObjectRequest.put("pageSize", PAGE_SIZE);
            jsonObjectRequest.put("currentPage", pageInfo.getPage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(String.valueOf(jsonObjectRequest), RequestHeader.JSON_TYPE);
        ApiService apiService = MDRetrofit.getInstance().createService(ServiceAddressType.IOT_MANAGER_SERVICE_ADDRESS);
        Observable<ResponseWrapper<PageResult<DeviceInfo>>> observable = MCloudApp.getPermissionTokenList().contains("ListSuperInfo") ? apiService.listSuperDevice(MCloudApp.getAccessToken(), body) : apiService.getDeviceList(MCloudApp.getAccessToken(), body);
        observable.doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new BaseObserver<PageResult<DeviceInfo>>() {
                    @Override
                    protected void onResponse(PageResult<DeviceInfo> data, ErrorInfo errorInfo) {
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    if (pageInfo.isFirstPage())
                                        deviceInfoAdapter.setEmptyView(R.layout.empty_view);
                                    return;
                                }
                                filterDevices(data.getCurrentPageData(), data.getTotalPage());
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                                deviceInfoAdapter.getLoadMoreModule().loadMoreFail();
                                if (pageInfo.isFirstPage())
                                    deviceInfoAdapter.setEmptyView(getErrorView());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        deviceInfoAdapter.getLoadMoreModule().loadMoreFail();
                        if (pageInfo.isFirstPage())
                            deviceInfoAdapter.setEmptyView(getErrorView());
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    /**
     * 在线、离线排序加载
     */
    private void filterDevices(List<DeviceInfo> tempList, int totalPage) {
        for (DeviceInfo deviceInfo : tempList) {
            if (!MCloudApp.getCompanyIdList().contains(deviceInfo.getCompanyID()))
                continue;

            if (deviceInfo.isOnlineStatus()) {
                deviceInfoList.add(0, deviceInfo);
            } else {
                deviceInfoList.add(deviceInfo);
            }
        }
        deviceInfoAdapter.notifyDataSetChanged();
        if (pageInfo.getPage() == totalPage)
            deviceInfoAdapter.getLoadMoreModule().loadMoreEnd();
        else
            deviceInfoAdapter.getLoadMoreModule().loadMoreComplete();

        // page加一
        pageInfo.nextPage();
    }

    private View getErrorView() {
        View errorView = getLayoutInflater().inflate(R.layout.error_view, mRecyclerView, false);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        return errorView;
    }
}
