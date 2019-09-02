package com.shmedo.mcloudapp.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.QueryProjectDeviceAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.PageResult;
import com.shmedo.mcloudapp.entity.ProjectDeviceInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.parameter.QueryProjectDeviceParamter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.DividerItemDecoration;
import com.shmedo.mcloudapp.views.LoadingDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import okhttp3.RequestBody;

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

    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.ce_find_sn) ClearEditText mCeFindSn;
    @BindView(R.id.btn_find) Button mBtnFind;
    @BindView(R.id.tv_update_time) TextView mTvUpdateTime;
    @BindView(R.id.btn_update) Button mBtnUpdate;
    @BindView(R.id.recyclerDevice) RecyclerView mRecyclerDevice;
    @BindView(R.id.refreshLayout) SmartRefreshLayout mRefreshLayout;
    private LoadingDialog mLoadingDialog;
    private List<ProjectDeviceInfo> deviceInfoList = new ArrayList<>();
    private QueryProjectDeviceAdapter deviceAdapter;
    //private PageResult<ProjectDeviceInfo> pageResult;
    private int pageNo = 1;
    private int projID;

    @Override protected int initContentView() {
        return R.layout.activity_device_manage_detail;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        initView();
        initData();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mLoadingDialog = new LoadingDialog(this);
        mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setOnRefreshLoadMoreListener(this);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        mRefreshLayout.setRefreshFooter(new ClassicsFooter(this));
        deviceAdapter = new QueryProjectDeviceAdapter(this, deviceInfoList);
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
        mRecyclerDevice.setAdapter(deviceAdapter);
    }


    private void initData() {

        projID = ((SystemDataInfo) getIntent().getSerializableExtra("queryProjectDevice")).getProID();
        Log.i("adu", "====projid===" + projID);
        mToolbarTitle.setText(((SystemDataInfo) getIntent().getSerializableExtra("queryProjectDevice")).getProjName());
        queryProjectDevice(projID, "", 5, pageNo);

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
        Log.i("adu","====json===="+json);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        MDRetrofit.getInstance()
            .createService()
            .QueryProjectDevice(CommonVariable.getAccessToken(), body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new BaseObserver<PageResult<ProjectDeviceInfo>>() {

                @Override
                public void Success(PageResult<ProjectDeviceInfo> infoList, String message) {
                    mLoadingDialog.dismiss();
                    if (infoList != null) {
                        List<ProjectDeviceInfo> list = infoList.getCurrentPageData();
                        if (list.size() != 0){
                            deviceInfoList.clear();
                            deviceInfoList.addAll(list);
                            deviceAdapter.notifyDataSetChanged();
                        }else {
                            ToastUtil.showSToast("没有更多数据了");
                            mRefreshLayout.finishLoadMoreWithNoMoreData();
                            //
                            //if (projID!=0){
                            //    queryProjectDevice(projID, "", 5, 1);
                            //}
                        }
                    }else {
                        ToastUtil.showSToast("没有了没有了");
                        mRefreshLayout.finishRefresh();
                    }
                }
                @Override public void Failure(String message) {
                    mLoadingDialog.dismiss();
                    Log.i("adu", "服务器连接失败--" + message);
                    ToastUtil.showSToast("服务器连接失败");
                }
            });
    }



    @OnClick({ R.id.btn_find, R.id.btn_update })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_find:
                //搜索按钮

                break;
            case R.id.btn_update:
                //更新
                break;
        }
    }

    //上拉加载更多
    @Override public void onLoadMore(RefreshLayout refreshLayout) {
        pageNo++;
        if (CommonVariable.isNetworkConnected()) {
            if (projID!=0){
                Log.i("adu","===pageNo上拉加载====="+pageNo);
                queryProjectDevice(projID, "", 5, pageNo);
            }
            refreshLayout.finishRefresh();
        }else {
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(true);
            ToastUtil.showSToast("请检查网络连接");
        }


    }

    //下拉刷新
    @Override public void onRefresh(RefreshLayout refreshLayout) {
        if (CommonVariable.isNetworkConnected()) {
            pageNo = 1;
          if (projID!=0){
              Log.i("adu","===pageNo下拉刷新====="+pageNo);
              queryProjectDevice(projID, "", 5, pageNo);
          }
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(false);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(true);
            ToastUtil.showSToast("请检查网络连接");
        }
    }
}
