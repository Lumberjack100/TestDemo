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
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.NewMainActivity;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.ProjectItemAdapter;
import com.shmedo.mcloudapp.projects.model.CustomLevelProjectInfo;
import com.shmedo.mcloudapp.projects.model.ProjectBaseInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectItem;
import com.shmedo.mcloudapp.projects.model.ProjectState;
import com.shmedo.mcloudapp.projects.model.ProjectViewMode;
import com.shmedo.mcloudapp.projects.model.RegionProjectInfo;
import com.shmedo.mcloudapp.projects.model.TypeProjectInfo;
import com.shmedo.mcloudapp.projects.model.param.ProjectBaseInfoParam;
import com.shmedo.mcloudapp.projects.ui.ViewProjectsInMapActivity;
import com.shmedo.mcloudapp.projects.ui.activity.OutOfDateProjectGuideActivity;
import com.shmedo.mcloudapp.projects.view.HeaderSearchView;
import com.shmedo.mcloudapp.projects.view.ProjectFilterDrawerView;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.DateUtil;
import com.shmedo.mcloudapp.util.GsonFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectListFragment extends BaseFragment implements ProjectFilterDrawerView.OnFilterResultListener, HeaderSearchView.OnSearchClickListener {
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.filter_drawer_layout)
    ProjectFilterDrawerView filterDrawerView;

    @BindView(R.id.header_search_view)
    HeaderSearchView headerSearchView;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_search_hint)
    TextView mTvSearchHint;

    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.recycler_project)
    RecyclerView mRecyclerProject;

    private NewMainActivity activity;
    private int userId;
    private UserInfo userInfo;

    private ProjectViewMode projectViewMode = ProjectViewMode.VIEW_SIMPLE;
    private ProjectState projectState = ProjectState.ALL;

    private ProjectItemAdapter itemAdapter;
    private List<ProjectBaseInfo> projectBaseInfoList = new ArrayList<>();
    private Map<Integer, ProjectDetailInfo> detailInfoMap = new LinkedHashMap<>();
    private List<ProjectItem> projectItems = new ArrayList<>();
    private List<ProjectItem> tempProjectItems = new ArrayList<>();

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

    private void initView() {
        mToolbarTitle.setText("项目列表");
        headerSearchView.setSearchHint("搜索项目");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (NewMainActivity) getActivity();
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            userId=user.getId();
        }
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

        filterDrawerView.setOnFilterSetListener(this);
        headerSearchView.setOnSearchClickListener(this);
    }

    private void initMultiItemAdapter() {
        mRecyclerProject.setLayoutManager(new LinearLayoutManager(getActivity()));
        itemAdapter = new ProjectItemAdapter(R.layout.listitem_project_header, R.layout.listitem_project_baseinfo, projectItems);
        itemAdapter.setAnimationEnable(true);
        itemAdapter.setAnimationFirstOnly(false);
        itemAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                ProjectItem projectItem = itemAdapter.getItem(position);
                if (projectItem.isHeader())
                    return;

                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                if (detailInfo.isOutOfDate()) {
                    OutOfDateProjectGuideActivity.startActivity(activity, detailInfo.getProjectName(), detailInfo.getRegisterTime());
                }

            }
        });
        mRecyclerProject.setAdapter(itemAdapter);
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
    public void onFilterResult(ProjectViewMode viewMode, ProjectState state) {
        mDrawerLayout.closeDrawer(GravityCompat.END);
        projectViewMode = viewMode;
        projectState = state;
        swipeRefresh.setRefreshing(true);
        refreshProjects();
    }

    @Override
    public void onSearch(String keyWord) {
        projectItems.clear();
        for (ProjectItem item : tempProjectItems) {
            if (item.getObject() instanceof ProjectDetailInfo) {
                ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                if (detailInfo.getProjectName().contains(keyWord)) {
                    projectItems.add(item);
                }
            }
        }
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchViewSwitch(boolean isHolderView) {
        swipeRefresh.setEnabled(isHolderView);

        if (isHolderView) {
            filterProjectsByState();
        }
    }

    private void refreshProjects() {
        switch (projectViewMode) {
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
                        if (data == null || data.size() == 0) {
                            swipeRefresh.setRefreshing(false);
                            return;
                        }

                        projectBaseInfoList.addAll(data);
                        List<Integer> projectIDs = new ArrayList<>();
                        for (ProjectBaseInfo baseInfo : data) {
                            projectIDs.add(baseInfo.getProjID());
                        }
                        QueryProjectListInfo(projectIDs);
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
        ProjectBaseInfoParam parameter = new ProjectBaseInfoParam(null, "");
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .GetLevelProjList(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<CustomLevelProjectInfo>>() {
                    @Override
                    public void Success(List<CustomLevelProjectInfo> data, String message) {
                        swipeRefresh.setRefreshing(false);
                        if (data == null || data.size() == 0) {
                            return;
                        }
                        setCustomLevelModeAdapterData(data);
                    }

                    @Override
                    public void Failure(String message) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    /**
     * 查询当前用户的项目列表(行政区域方式)
     */
    private void queryUserRegionProject() {
        ProjectBaseInfoParam parameter = new ProjectBaseInfoParam(null, "");
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryUserRegionListProject(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<RegionProjectInfo>>() {
                    @Override
                    public void Success(List<RegionProjectInfo> data, String message) {
                        swipeRefresh.setRefreshing(false);
                        if (data == null || data.size() == 0) {
                            return;
                        }
                        setRegionModeAdapterData(data);
                    }

                    @Override
                    public void Failure(String message) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    /**
     * 查询当前用户的项目列表(项目类型方式)
     */
    private void queryUserTypeProject() {
        ProjectBaseInfoParam parameter = new ProjectBaseInfoParam(null, "");
        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryUserTypeProject(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<TypeProjectInfo>>() {
                    @Override
                    public void Success(List<TypeProjectInfo> data, String message) {
                        swipeRefresh.setRefreshing(false);
                        if (data == null || data.size() == 0) {
                            return;
                        }
                        setTypeModeAdapterData(data);
                    }

                    @Override
                    public void Failure(String message) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    /**
     * 查询警报阈值列表
     *
     * @param projectIDs
     */
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
                        if (data == null || data.size() == 0) {
                            return;
                        }

                        detailInfoMap.clear();
                        for (ProjectDetailInfo detailInfo : data) {
                            detailInfo.setUserId(userId);
                            Date registerDate = new Date();
                            try {
                                registerDate = DateUtil.stringToDate(detailInfo.getRegisterTime(), "yyyy-MM-dd HH:mm:ss");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            detailInfo.setOutOfDate(registerDate.before(new Date()));
                            detailInfoMap.put(detailInfo.getProjectID(), detailInfo);
                        }

                        setSimpleModeAdapterData(data);

                        DaoManager.getInstance().getDaoSession().getProjectDetailInfoDao().insertOrReplaceInTx(data);
                    }

                    @Override
                    public void Failure(String message) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    /**
     * 设置展示简单列表适配器数据
     *
     * @param infos
     */
    private void setSimpleModeAdapterData(List<ProjectDetailInfo> infos) {
        tempProjectItems.clear();
        for (ProjectDetailInfo info : infos) {
            tempProjectItems.add(new ProjectItem(false, info));
        }
        filterProjectsByState();
    }

    /**
     * 设置按照自定义分级分组展示适配器数据
     *
     * @param infos
     */
    private void setCustomLevelModeAdapterData(List<CustomLevelProjectInfo> infos) {
        tempProjectItems.clear();
        for (CustomLevelProjectInfo info : infos) {
            tempProjectItems.add(new ProjectItem(true, info.getLevelName()));
            if (info.getLevelProjs() != null) {
                for (ProjectBaseInfo baseInfo : info.getLevelProjs()) {
                    ProjectDetailInfo detailInfo = detailInfoMap.get(baseInfo.getProjID());
                    if (detailInfo != null)
                        tempProjectItems.add(new ProjectItem(false, detailInfo));
                }
            }
        }
        filterProjectsByState();
    }

    /**
     * 设置按照行政区域分组展示适配器数据
     *
     * @param infos
     */
    private void setRegionModeAdapterData(List<RegionProjectInfo> infos) {
        tempProjectItems.clear();
        for (RegionProjectInfo info : infos) {
            tempProjectItems.add(new ProjectItem(true, info.getRegionFullName()));
            if (info.getProjects() != null) {
                for (ProjectBaseInfo baseInfo : info.getProjects()) {
                    ProjectDetailInfo detailInfo = detailInfoMap.get(baseInfo.getProjID());
                    if (detailInfo != null)
                        tempProjectItems.add(new ProjectItem(false, detailInfo));
                }
            }
        }
        filterProjectsByState();
    }

    /**
     * 设置按照项目类型分组展示适配器数据
     *
     * @param infos
     */
    private void setTypeModeAdapterData(List<TypeProjectInfo> infos) {
        tempProjectItems.clear();
        for (TypeProjectInfo info : infos) {
            tempProjectItems.add(new ProjectItem(true, info.getProjTypeName()));
            if (info.getProjects() != null) {
                for (ProjectBaseInfo baseInfo : info.getProjects()) {
                    ProjectDetailInfo detailInfo = detailInfoMap.get(baseInfo.getProjID());
                    if (detailInfo != null)
                        tempProjectItems.add(new ProjectItem(false, detailInfo));
                }
            }
        }

        filterProjectsByState();
    }

    private void filterProjectsByState() {
        projectItems.clear();
        switch (projectState) {
            case ALL:
                projectItems.addAll(tempProjectItems);
                break;

            case ON_LINE://筛选出在线的项目
                for (ProjectItem item : tempProjectItems) {
                    if (item.getObject() instanceof ProjectDetailInfo) {
                        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                        if (detailInfo.isIsValid()) {
                            projectItems.add(item);
                        }
                    }
                }
                break;

            case OFF_LINE://筛选出离线的项目
                for (ProjectItem item : tempProjectItems) {
                    if (item.getObject() instanceof ProjectDetailInfo) {
                        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                        if (!detailInfo.isIsValid()) {
                            projectItems.add(item);
                        }
                    }
                }
                break;

            case OUT_OF_DATE://筛选出过期的项目
                for (ProjectItem item : tempProjectItems) {
                    if (item.getObject() instanceof ProjectDetailInfo) {
                        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                        if (detailInfo.isOutOfDate()) {
                            projectItems.add(item);
                        }
                    }
                }
                break;
        }

        itemAdapter.notifyDataSetChanged();
    }

}
