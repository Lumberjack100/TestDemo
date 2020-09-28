package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.DeviceInfoAdapter;
import com.shmedo.mcloudapp.projects.model.PageInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.projects.model.param.QueryProjectDevice;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.ResponseHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class DeviceSearchActivity extends BaseActivity {
    private static final String PROJECT_NAME = "project_name";

    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private DeviceInfoAdapter deviceInfoAdapter;
    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();

    private static final int PAGE_SIZE = 10;
    private PageInfo pageInfo;
    private int companyID = 1;
    private String keyWords;// 要输入的poi搜索关键字
    private String projectName;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, DeviceSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String projectName) {
        Intent intent = new Intent(context, DeviceSearchActivity.class);
        intent.putExtra(PROJECT_NAME, projectName);
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
        companyID = MCloudApp.getCompanyID();
        parseIntent();
        initView();
        initDeviceInfoAdapter();
        initLoadMore();
        pageInfo = new PageInfo(1);
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(PROJECT_NAME)) {
            projectName = intent.getStringExtra(PROJECT_NAME);
        }
    }

    private void initView() {
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
                        // 当按了搜索之后关闭软键盘
                        KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                        refresh();
                    }
                    return true;
                }
                return false;
            }
        });
    }


    private void initDeviceInfoAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(this, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        deviceInfoAdapter = new DeviceInfoAdapter(deviceInfoList);
        deviceInfoAdapter.setAnimationEnable(true);
        deviceInfoAdapter.setAnimationFirstOnly(false);
        deviceInfoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ProjectDeviceInfo deviceInfo = deviceInfoList.get(position);
                DeviceConfigActivity.startActivity(DeviceSearchActivity.this, deviceInfo);
            }
        });
        mRecyclerView.setAdapter(deviceInfoAdapter);
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

    @OnClick({R.id.iv_back, R.id.tv_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;

            case R.id.tv_search:
                if (TextUtils.isEmpty(mEtKeyWords.getText().toString().trim())) {
                    ToastUtils.show("请输入搜索内容");
                    return;
                }
                keyWords = mEtKeyWords.getText().toString();
                // 当按了搜索之后关闭软键盘
                KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                refresh();
                break;
        }
    }


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
     * 开始查询接口
     */
    private void queryCompanyDevice() {
        // 方式一：直接传入 layout id
        deviceInfoAdapter.setEmptyView(R.layout.loading_view);

        QueryProjectDevice parameter = new QueryProjectDevice();
        parameter.setCompanyID(companyID);
        parameter.setProjectName(TextUtils.isEmpty(projectName) ? "" : projectName);
        parameter.setDeviceType(-1);
        parameter.setSn(keyWords);
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
                                deviceInfoList.addAll(data.getCurrentPageData());
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
                        } else {
                            deviceInfoAdapter.setEmptyView(getErrorView());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        deviceInfoAdapter.getLoadMoreModule().setEnableLoadMore(true);
                        deviceInfoAdapter.getLoadMoreModule().loadMoreFail();
//                        if (deviceInfoList.size() == 0) {
//                            deviceInfoAdapter.setEmptyView(getErrorView());
//                        }
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
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
