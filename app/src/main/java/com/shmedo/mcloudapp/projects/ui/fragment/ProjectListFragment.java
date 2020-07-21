package com.shmedo.mcloudapp.projects.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.NewMainActivity;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.ProjectMultiItemAdapter;
import com.shmedo.mcloudapp.projects.adapter.ProjectSimpleAdapter;
import com.shmedo.mcloudapp.projects.model.ProjectBaseInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectFilterScope;
import com.shmedo.mcloudapp.projects.model.ProjectViewMode;
import com.shmedo.mcloudapp.projects.model.param.ProjectBaseInfoParam;
import com.shmedo.mcloudapp.projects.ui.ViewProjectsInMapActivity;
import com.shmedo.mcloudapp.projects.view.ProjectFilterDrawerView;
import com.shmedo.mcloudapp.util.GsonFactory;

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
public class ProjectListFragment extends BaseFragment implements ProjectFilterDrawerView.OnFilterResultListener {
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.filter_drawer_layout)
    ProjectFilterDrawerView filterDrawerView;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_search_hint)
    TextView mTvSearchHint;

    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.recycler_project)
    RecyclerView mRecyclerProject;

    private NewMainActivity activity;

    private ProjectViewMode curViewMode = ProjectViewMode.VIEW_SIMPLE;
    private ProjectFilterScope curFilterScope = ProjectFilterScope.ALL;

    private ProjectSimpleAdapter projectSimpleAdapter;
    private ProjectMultiItemAdapter projectMultiItemAdapter;
    private List<ProjectBaseInfo> projectBaseInfoList = new ArrayList<>();
    private List<ProjectDetailInfo> projectDetailInfoList = new ArrayList<>();

    @Override
    protected int initContentView() {
        return R.layout.fragment_project_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (NewMainActivity) getActivity();
        initMultiItemAdapter();
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshProjects();
            }
        });

        // 进入页面，刷新数据
        swipeRefresh.setRefreshing(true);
        refreshProjects();
    }

    private void initView() {
        mToolbarTitle.setText("项目列表");
        mRecyclerProject.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initMultiItemAdapter() {
        projectMultiItemAdapter = new ProjectMultiItemAdapter();
        mRecyclerProject.setAdapter(projectMultiItemAdapter);
    }

    private void initSimpleAdapter() {
        projectSimpleAdapter = new ProjectSimpleAdapter();
        projectSimpleAdapter.setAnimationEnable(true);
        projectSimpleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
//                ProjectDetailInfo projectDetailInfo = projectSimpleAdapter.getItem(position);
                ToastUtils.show("onItemChildClick: " + position);
            }
        });
        mRecyclerProject.setAdapter(projectSimpleAdapter);
    }


    @OnClick({R.id.iv_view_in_map, R.id.iv_filter})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_view_in_map://在地图中浏览项目
                ViewProjectsInMapActivity.startActivity(getActivity());
                break;

            case R.id.iv_filter://推出项目筛选条件抽屉窗口
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                }
                break;
        }
    }

    @Override
    public void onFilterResult(ProjectViewMode projectViewMode, ProjectFilterScope filterScope) {
        curViewMode = projectViewMode;
        refreshProjects();
    }

    private void refreshProjects() {
        switch (curViewMode) {
            case VIEW_SIMPLE:
                queryUserListProject();
                break;

            case VIEW_GROUP_BY_LEVEL:
                getLevelProjList();
                break;

            case VIEW_GROUP_BY_REGION:
                queryUserRegionProject();
                break;

            case VIEW_GROUP_BY_TYPE:
                queryUserTypeProject();
                break;
        }
    }

    /**
     * 查询当前用户的项目列表(列表方式、不分页)
     */
    private void queryUserListProject() {
        ProjectBaseInfoParam parameter = new ProjectBaseInfoParam(null, "");
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryUserListProject(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<ProjectBaseInfo>>() {
                    @Override
                    public void Success(List<ProjectBaseInfo> data, String message) {
                        if (null != data && data.size() != 0) {
                            projectBaseInfoList.addAll(data);

                            List<Integer> projectIDs = new ArrayList<>();
                            for (ProjectBaseInfo baseInfo : data) {
                                projectIDs.add(baseInfo.getProjID());
                            }
                            QueryProjectListInfo(projectIDs);
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    /**
     * 查询当前用户的项目列表（自定义分级方式，不包含空节点）
     */
    private void getLevelProjList() {

    }

    /**
     * 查询当前用户的项目列表(行政区域方式)
     */
    private void queryUserRegionProject() {

    }

    /**
     * 查询当前用户的项目列表(项目类型方式)
     */
    private void queryUserTypeProject() {

    }

    private void QueryProjectListInfo(List<Integer> projectIDs) {
        String json = GsonFactory.getGson().toJson(projectIDs);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryProjectListInfo(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<ProjectDetailInfo>>() {
                    @Override
                    public void Success(List<ProjectDetailInfo> data, String message) {
                        swipeRefresh.setRefreshing(false);
                        if (null != data && data.size() != 0) {
                            processProjectResults(data);
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void processProjectResults(List<ProjectDetailInfo> data) {
        initSimpleAdapter();
        projectSimpleAdapter.setNewInstance(data);
    }


}
