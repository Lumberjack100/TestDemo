package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.MultiItemTypeAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.parameter.SystemParameter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.ApiName;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.views.EmptyDataView;
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
 * 创建者:   gh
 * 创建时间:  2020/2/20 14:14
 * 描述：    项目列表页面
 */
public class ProjectListActivity extends BaseActivity implements MultiItemTypeAdapter.OnItemClickListener, OnRefreshListener, OnRefreshLoadMoreListener {

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_right)
    TextView mTvRight;

    @BindView(R.id.recycler_project)
    RecyclerView mRecyclerProject;

    @BindView(R.id.smartRefreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.empty_data)
    EmptyDataView mEmptyData;

    private CommonAdapter adapter;

    private List<SystemDataInfo> projectList = new ArrayList<>();

    private DaoManager manager = DaoManager.getInstance();


    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProjectListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_project_list;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("项目管理");
        mTvRight.setVisibility(View.VISIBLE);
        mTvRight.setText("设备管理");
        initView();
        getProjectList();
//        //自动刷新，getProjectList();
//        mRefreshLayout.autoRefresh();
    }


    private void initView() {
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableAutoLoadMore(false);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(this);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        //mRefreshLayout.setRefreshFooter(new ClassicsFooter(this));

        mRecyclerProject.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerProject.addItemDecoration(new DividerItemDecoration());
        adapter = new CommonAdapter<SystemDataInfo>(this, R.layout.item_project, projectList) {
            @Override
            protected void convert(ViewHolder holder, final SystemDataInfo systemDataInfo, final int position) {
                holder.setText(R.id.tv_projectName, systemDataInfo.getProjName());
                holder.setText(R.id.tv_companyName, "惠山区洛社镇XX社区");
                holder.setText(R.id.tv_createTime, "2018.11.16 09:38");
            }
        };
        adapter.setOnItemClickListener(this);
        mRecyclerProject.setAdapter(adapter);
    }


    @OnClick({R.id.back, R.id.ll_search, R.id.tv_right})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.ll_search:
                Intent intent = new Intent(this, SearchDeviceActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_right:
                DeviceListActivity.startActivity(this);
                break;
        }
    }


    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        DeviceManageDetailActivity.startActivity(ProjectListActivity.this, projectList.get(position));
    }


    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }


    /**
     * 上拉加载更多
     */
    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }


    /**
     * 下拉刷新数据
     */
    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        if (MCloudApp.isIsNetworkConnected()) {
            projectList.clear();
            getProjectList();
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(false);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.setNoMoreData(true);
            ToastUtils.show("请检查网络连接");
        }
    }


    /**
     * 获取当前用户的项目列表
     */
    private void getProjectList() {
        showLoadingDialog("加载数据中...");
        //获取系统
        SystemParameter parameter = new SystemParameter(null);
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService(ApiName.HTTPS)
                .QueryUserListProject(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<SystemDataInfo>>() {
                    @Override
                    public void Success(List<SystemDataInfo> infoList, String message) {
                        dismissLoadingDialog();

                        if (null != infoList && infoList.size() != 0) {
                            for (SystemDataInfo systemDataInfo : infoList) {
                                systemDataInfo.setAccount(MCloudApp.getAccount());
                            }
                            manager.getDaoSession().getSystemDataInfoDao().insertOrReplaceInTx(infoList);

                            projectList.clear();
                            projectList.addAll(infoList);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        dismissLoadingDialog();
                        Timber.w("服务器连接失败--" + message);
                    }
                });
    }

}
