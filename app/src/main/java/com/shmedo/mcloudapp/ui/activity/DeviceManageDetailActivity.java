package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.QueryProjectDeviceAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.PageResult;
import com.shmedo.mcloudapp.entity.ProjectDeviceInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.parameter.QueryProjectDeviceParamter;
import com.shmedo.mcloudapp.interfaces.Extras;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.EmptyDataView;
import com.shmedo.mcloudapp.views.LoadingDialog;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   DeviceManagementActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/17 17:17
 * 描述：    设备管理详情页面
 */
public class DeviceManageDetailActivity extends BaseActivity implements OnRefreshListener,
        OnRefreshLoadMoreListener {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.ce_find_sn)
    ClearEditText mCeFindSn;

    @BindView(R.id.btn_find)
    Button mBtnFind;

    @BindView(R.id.tv_update_time)
    TextView mTvUpdateTime;

    @BindView(R.id.btn_update)
    Button mBtnUpdate;

    @BindView(R.id.empty_data)
    EmptyDataView mEmptyData;

    @BindView(R.id.recyclerDevice)
    RecyclerView mRecyclerDevice;

    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    private LoadingDialog mLoadingDialog;
    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();
    private QueryProjectDeviceAdapter deviceAdapter;

    private static final int PAGE_SIZE = 5;//每页请求数据大小
    private int pageNo = 1;
    private int projID;

    private boolean isRefreshOrLoad = true;//true:代表下拉刷新请求  false:代表上拉加载请求


    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context, SystemDataInfo systemDataInfo) {
        Intent intent = new Intent(context, DeviceManageDetailActivity.class);
        intent.putExtra(Extras.QUERY_PROJECT_DEVICE, systemDataInfo);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_device_manage_detail;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initData();
    }


    private void initView() {
        mLoadingDialog = new LoadingDialog(this);
        mRefreshLayout.setEnableAutoLoadMore(false);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setOnRefreshLoadMoreListener(this);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        mRefreshLayout.setRefreshFooter(new ClassicsFooter(this));

        deviceAdapter = new QueryProjectDeviceAdapter(this, deviceInfoList);
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
        mRecyclerDevice.setAdapter(deviceAdapter);
    }


    private void initData() {

        Intent intent = getIntent();
        if (intent != null) {
            SystemDataInfo systemDataInfo = (SystemDataInfo) intent.getSerializableExtra(Extras.QUERY_PROJECT_DEVICE);
            if (systemDataInfo == null) {
                Timber.w("传递的参数 SystemDataInfo 值为 NULL!");
                return;
            }

            projID = systemDataInfo.getProID();
            mToolbarTitle.setText(systemDataInfo.getProjName());
            //自动刷新，会调用queryProjectDevice(projID, "", 5, pageNo);
            mRefreshLayout.autoRefresh();
        }
    }


    /**
     * 查询项目设备
     */
    private void queryProjectDevice(int projId, String deviceName, int pageSize, int currentPage) {
        mLoadingDialog.showNoCancelDialog("数据加载中...");
        QueryProjectDeviceParamter paramter = new QueryProjectDeviceParamter();
        paramter.setProjectID(projId);
        paramter.setDeviceName(deviceName);
        paramter.setPageSize(pageSize);
        paramter.setCurrentPage(currentPage);
        String json = GsonFactory.getGson().toJson(paramter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryProjectDevice(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<ProjectDeviceInfo>>() {

                    @Override
                    public void Success(PageResult<ProjectDeviceInfo> infoList, String message) {
                        mLoadingDialog.dismiss();
                        //下拉刷新
                        if (isRefreshOrLoad) {

                            if (infoList == null || infoList.getCurrentPageData() == null || infoList.getCurrentPageData().size() == 0) {
                                mRefreshLayout.finishRefresh(false);
                                mRefreshLayout.finishRefreshWithNoMoreData();//完成刷新并标记没有更多数据

                            } else {
                                deviceInfoList.clear();
                                deviceInfoList.addAll(infoList.getCurrentPageData());
                                deviceAdapter.notifyDataSetChanged();

                                mRefreshLayout.finishRefresh();
                                if (infoList.getCurrentPageData().size() < PAGE_SIZE) {
                                    mRefreshLayout.finishRefreshWithNoMoreData();//完成刷新并标记没有更多数据
                                }
                            }

                        } else {//上拉加载

                            if (infoList == null || infoList.getCurrentPageData() == null || infoList.getCurrentPageData().size() == 0) {
                                mRefreshLayout.finishLoadMoreWithNoMoreData();//完成加载并标记没有更多数据

                            } else {
                                int oldItemCount = deviceInfoList.size();
                                deviceInfoList.addAll(infoList.getCurrentPageData());
                                deviceAdapter.notifyItemRangeInserted(oldItemCount, infoList.getCurrentPageData().size());

                                if (infoList.getCurrentPageData().size() < PAGE_SIZE) {
                                    mRefreshLayout.finishLoadMoreWithNoMoreData();//完成加载并标记没有更多数据

                                } else {
                                    mRefreshLayout.finishLoadMore();
                                }
                            }
                        }

                        mEmptyData.setVisibility(deviceInfoList.size() == 0 ? View.VISIBLE : View.GONE);
                        mRefreshLayout.setVisibility(deviceInfoList.size() == 0 ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void Failure(String message) {
                        mLoadingDialog.dismiss();

                        if (isRefreshOrLoad) {
                            mRefreshLayout.finishRefresh(false);//表示刷新失败（不会更新时间）
                            mRefreshLayout.finishLoadMoreWithNoMoreData();
                        } else {
                            mRefreshLayout.finishLoadMore(false);//表示加载失败
                        }

                        Timber.w("服务器连接失败--" + message);
                        ToastUtils.show("服务器连接失败");
                    }
                });
    }


    @OnClick({R.id.iv_scan_device, R.id.btn_find, R.id.btn_update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_scan_device:
                //扫描按钮

                break;

            case R.id.btn_find:
                //搜索按钮

                break;

            case R.id.btn_update:
                //更新
                break;
        }
    }


    /**
     * 上拉加载
     *
     * @param refreshLayout
     */
    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
//        if (CommonVariable.isNetworkConnected()) {
        if (projID != 0) {
            Timber.d("上拉加载,pageNo=" + pageNo);

            isRefreshOrLoad = false;
            pageNo++;
            queryProjectDevice(projID, "", PAGE_SIZE, pageNo);
        }
//        } else {
//            refreshLayout.finishRefresh();
//            refreshLayout.setNoMoreData(true);
//            ToastUtils.show("请检查网络连接");
//        }
    }

    /**
     * 下拉刷新
     *
     * @param refreshLayout
     */
    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
//        if (CommonVariable.isNetworkConnected()) {
        if (projID != 0) {
            Timber.d("下拉刷新,pageNo=" + pageNo);

            isRefreshOrLoad = true;
            pageNo = 1;
            queryProjectDevice(projID, "", PAGE_SIZE, pageNo);
        }

//        } else {
//            refreshLayout.finishRefresh();
//            refreshLayout.setNoMoreData(true);
//            ToastUtils.show("请检查网络连接");
//        }
    }
}
