package com.shmedo.mcloudapp.projects.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonViewHolder;
import com.shmedo.mcloudapp.common.ui.activity.NewMainActivity;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.ProjectMultipleItemAdapter;
import com.shmedo.mcloudapp.projects.model.CustomLevelProjectInfo;
import com.shmedo.mcloudapp.projects.model.ProjectBaseInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectItem;
import com.shmedo.mcloudapp.projects.model.ProjectState;
import com.shmedo.mcloudapp.projects.model.ProjectViewMode;
import com.shmedo.mcloudapp.projects.model.RegionProjectInfo;
import com.shmedo.mcloudapp.projects.model.TypeProjectInfo;
import com.shmedo.mcloudapp.projects.model.param.ProjectBaseInfoParam;
import com.shmedo.mcloudapp.projects.ui.activity.OutOfDateProjectGuideActivity;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.DateUtil;
import com.shmedo.mcloudapp.util.GlideUtils;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewProjectFragment extends BaseFragment {
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.recyclerview)
    SwipeRecyclerView mRecyclerView;

    @BindView(R.id.recyclerview_group)
    RecyclerView mRecyclerViewGroup;

    private NewMainActivity activity;
    private int userId;
    private UserInfo userInfo;

    private ProjectViewMode projectViewMode = ProjectViewMode.VIEW_SIMPLE;
    private ProjectState projectState = ProjectState.ALL;

    private CommonAdapter simpleAdapter;
    private ProjectMultipleItemAdapter groupAdapter;
    private Map<Integer, ProjectDetailInfo> detailInfoMap = new LinkedHashMap<>();
    private List<ProjectBaseInfo> tempBaseInfoList = new LinkedList<>();
    private List<ProjectItem> projectItems = new ArrayList<>();
    private List<ProjectItem> tempProjectItems = new ArrayList<>();

    @Override
    protected int initContentView() {
        return R.layout.fragment_new_project;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        return view;
    }

    private void initView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerViewGroup.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (NewMainActivity) getActivity();
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            userId = user.getId();
        }
        initSimpleAdapter();
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

    private void initSimpleAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.transparent), 0, DensityUtil.Dp2Px(getActivity(), 14));
        mRecyclerView.addItemDecoration(mItemDecoration);
        simpleAdapter = new CommonAdapter<ProjectItem>(getActivity(), R.layout.item_project_info_normal, projectItems) {
            @Override
            protected void convert(CommonViewHolder holder, final ProjectItem projectItem, final int position) {
                if (projectItem.getObject() instanceof String) {
                    return;
                }
                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                ImageView mIvThumbnail = holder.getView(R.id.ic_thumbnail);
                GlideUtils.loadImage(MCloudApp.getContext(), detailInfo.getImagePath(), mIvThumbnail, R.drawable.ic_project_default, R.drawable.ic_photo);
                holder.setText(R.id.tv_project_name, detailInfo.getProjectName());
                holder.setText(R.id.tv_create_time, detailInfo.getBuildTime());
                holder.setText(R.id.tv_company_name, detailInfo.getCompanyName());
            }
        };
        mRecyclerView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ProjectItem projectItem = projectItems.get(position);
//                if (projectItem.isHeader())
//                    return;

                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                if (detailInfo.isOutOfDate()) {
                    OutOfDateProjectGuideActivity.startActivity(activity, detailInfo.getProjectName(), detailInfo.getRegisterTime());
                }
            }
        });
        mRecyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        mRecyclerView.setOnItemMenuClickListener(mItemMenuClickListener);
        mRecyclerView.setAdapter(simpleAdapter);
    }

    private void initMultiItemAdapter() {
        mRecyclerViewGroup.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupAdapter = new ProjectMultipleItemAdapter( projectItems);
        groupAdapter.setAnimationEnable(true);
        groupAdapter.setAnimationFirstOnly(false);
        groupAdapter.setOnItemClickListener(new com.chad.library.adapter.base.listener.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                ProjectItem projectItem = groupAdapter.getItem(position);
//                if (projectItem.isHeader())
//                    return;

                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                if (detailInfo.isOutOfDate()) {
                    OutOfDateProjectGuideActivity.startActivity(activity, detailInfo.getProjectName(), detailInfo.getRegisterTime());
                }
            }
        });

        mRecyclerViewGroup.setAdapter(groupAdapter);
    }

    /**
     * 菜单创建器，在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int position) {
            int width = getResources().getDimensionPixelSize(R.dimen.dimen_size_70);
            // 1. MATCH_PARENT 自适应高度，保持和Item一样高;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;

            // 只添加Item右侧的菜单。
            {
                SwipeMenuItem addItem = new SwipeMenuItem(getActivity())
                        .setBackground(R.drawable.bg_corner_6dp_blue)
                        .setText("置顶")
                        .setTextColor(Color.WHITE)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(addItem); // 添加菜单到左侧。
            }
        }
    };

    /**
     * RecyclerView的Item的Menu点击监听。
     */
    private OnItemMenuClickListener mItemMenuClickListener = new OnItemMenuClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge, int position) {
            menuBridge.closeMenu();

            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。

            if (direction == SwipeRecyclerView.RIGHT_DIRECTION) {
                ToastUtils.show("list第" + position + "; 右侧菜单第" + menuPosition);
            }
        }
    };

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
                        tempBaseInfoList.clear();
                        tempBaseInfoList.addAll(data);
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

                        //TODO 按照tempBaseInfoList列表顺序对data排序
                        List<ProjectDetailInfo> tempDetailInfoList = new ArrayList<>();
                        detailInfoMap.clear();
                        for (ProjectBaseInfo baseInfo : tempBaseInfoList) {
                            for (ProjectDetailInfo detailInfo : data) {
                                if (baseInfo.getProjID() == detailInfo.getProjectID()) {
                                    //设置置顶标识
                                    detailInfo.setTop(baseInfo.isTop());
                                    //设置用户 Id
                                    detailInfo.setUserId(userId);
                                    Date registerDate = new Date();
                                    try {
                                        registerDate = DateUtil.stringToDate(detailInfo.getRegisterTime(), "yyyy-MM-dd HH:mm:ss");
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    //设置过期标识
                                    detailInfo.setOutOfDate(registerDate.before(new Date()));
                                    detailInfoMap.put(detailInfo.getProjectID(), detailInfo);
                                    tempDetailInfoList.add(detailInfo);
                                    break;
                                }
                            }
                        }

                        setSimpleModeAdapterData(tempDetailInfoList);
                        //更新到本地数据库
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
            tempProjectItems.add(new ProjectItem(info));
        }
        filterSimpleListProjectsByState();
    }

    /**
     * 设置按照自定义分级分组展示适配器数据
     *
     * @param infos
     */
    private void setCustomLevelModeAdapterData(List<CustomLevelProjectInfo> infos) {
        projectItems.clear();
        for (CustomLevelProjectInfo info : infos) {
            List<ProjectItem> subProjectItems = filterGroupListProjectsByState(info.getLevelProjs());
            if (!subProjectItems.isEmpty()) {
                projectItems.add(new ProjectItem(info.getLevelName(), ProjectItem.ITEM_TOP));
                projectItems.addAll(subProjectItems);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    /**
     * 设置按照行政区域分组展示适配器数据
     *
     * @param infos
     */
    private void setRegionModeAdapterData(List<RegionProjectInfo> infos) {
        projectItems.clear();
        for (RegionProjectInfo info : infos) {
            List<ProjectItem> subProjectItems = filterGroupListProjectsByState(info.getProjects());
            if (!subProjectItems.isEmpty()) {
                projectItems.add(new ProjectItem(info.getRegionFullName(), ProjectItem.ITEM_TOP));
                projectItems.addAll(subProjectItems);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    /**
     * 设置按照项目类型分组展示适配器数据
     *
     * @param infos
     */
    private void setTypeModeAdapterData(List<TypeProjectInfo> infos) {
        projectItems.clear();
        for (TypeProjectInfo info : infos) {
            List<ProjectItem> subProjectItems = filterGroupListProjectsByState(info.getProjects());
            if (!subProjectItems.isEmpty()) {
                projectItems.add(new ProjectItem(info.getProjTypeName(), ProjectItem.ITEM_TOP));
                projectItems.addAll(subProjectItems);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    /**
     * 根据项目状态过滤简单项目列表
     */
    private void filterSimpleListProjectsByState() {
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

        simpleAdapter.notifyDataSetChanged();
    }

    /**
     * 根据项目状态过滤分组项目列表
     */
    private List<ProjectItem> filterGroupListProjectsByState(List<ProjectBaseInfo> subBaseInfoList) {
        if (subBaseInfoList == null) {
            return new ArrayList<>();
        }
        List<ProjectItem> subProjectItems = new ArrayList<>();
        for (ProjectBaseInfo baseInfo : subBaseInfoList) {
            ProjectDetailInfo detailInfo = detailInfoMap.get(baseInfo.getProjID());
            if (detailInfo == null)
                continue;

            switch (projectState) {
                case ALL:
                    subProjectItems.add(new ProjectItem(detailInfo, ProjectItem.ITEM_MIDDLE));
                    break;

                case ON_LINE://筛选出在线的项目
                    if (detailInfo.isIsValid()) {
                        subProjectItems.add(new ProjectItem(detailInfo, ProjectItem.ITEM_MIDDLE));
                    }
                    break;

                case OFF_LINE://筛选出离线的项目
                    if (!detailInfo.isIsValid()) {
                        subProjectItems.add(new ProjectItem(detailInfo, ProjectItem.ITEM_MIDDLE));
                    }
                    break;

                case OUT_OF_DATE://筛选出过期的项目
                    if (detailInfo.isOutOfDate()) {
                        subProjectItems.add(new ProjectItem(detailInfo, ProjectItem.ITEM_MIDDLE));
                    }
                    break;
            }
        }

        subProjectItems.get(subProjectItems.size() - 1).setItemType(ProjectItem.ITEM_BOTTOM);
        return subProjectItems;
    }

    private void processTopUserProject(List<Integer> projectIDs) {
        String json = GsonFactory.getGson().toJson(projectIDs);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .TopUserProject(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String data, String message) {
                        swipeRefresh.setRefreshing(true);
                        refreshProjects();
                    }

                    @Override
                    public void Failure(String message) {

                    }
                });
    }

    private void processUnTopUserProject(List<Integer> projectIDs) {
        String json = GsonFactory.getGson().toJson(projectIDs);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .UnTopUserProject(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void Success(String data, String message) {
                        swipeRefresh.setRefreshing(true);
                        refreshProjects();
                    }

                    @Override
                    public void Failure(String message) {

                    }
                });
    }

}
