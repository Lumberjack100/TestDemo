package com.shmedo.mcloudapp.projects.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gyf.immersionbar.ImmersionBar;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.interfaces.SimpleCallback;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.NewMainActivity;
import com.shmedo.mcloudapp.common.ui.fragment.BaseTranslucentFragment;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.adapter.ProjectMultipleItemAdapter;
import com.shmedo.mcloudapp.projects.helper.ProjectImageHelper;
import com.shmedo.mcloudapp.projects.model.CustomLevelProjectInfo;
import com.shmedo.mcloudapp.projects.model.ProjectBaseInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectItem;
import com.shmedo.mcloudapp.projects.model.enums.ProjectState;
import com.shmedo.mcloudapp.projects.model.enums.ProjectGroupViewMode;
import com.shmedo.mcloudapp.projects.model.RegionProjectInfo;
import com.shmedo.mcloudapp.projects.model.IndustryTypeProjectInfo;
import com.shmedo.mcloudapp.projects.model.param.ProjectBaseInfoParam;
import com.shmedo.mcloudapp.projects.ui.activity.DevicesInProjectActivity;
import com.shmedo.mcloudapp.projects.ui.activity.ProjectSearchActivity;
import com.shmedo.mcloudapp.projects.ui.activity.ViewProjectsInMapActivity;
import com.shmedo.mcloudapp.projects.view.ProjectExpiredGuideDialog;
import com.shmedo.mcloudapp.projects.view.ProjectFilterPopupView;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.DateUtil;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
public class ProjectListFragment extends BaseTranslucentFragment {

    @BindView(R.id.toolbar_top_divider)
    View toolBarTopDivider;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.search_container)
    ViewGroup searchLayout;

    @BindView(R.id.iv_top_bg)
    ImageView ivTopBg;

    @BindView(R.id.iv_view_in_map)
    ImageView ivMap;

    @BindView(R.id.iv_filter)
    ImageView ivFilter;

    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;

    @BindView(R.id.recyclerview)
    SwipeRecyclerView mRecyclerView;

    @BindView(R.id.recyclerview_group)
    RecyclerView mRecyclerViewGroup;

    private FilterProjectDialog projectDialog;
    private ProjectFilterPopupView popupView;

    private NewMainActivity activity;
    private int userId;
    private int companyID;
    private UserInfo userInfo;

    private ProjectGroupViewMode projectGroupViewMode = ProjectGroupViewMode.SIMPLE_LIST;
    private ProjectState projectState = ProjectState.ALL;

    private CommonAdapter simpleAdapter;
    private ProjectMultipleItemAdapter multiAdapter;
    private Map<Integer, ProjectDetailInfo> detailInfoMap = new LinkedHashMap<>();
    private List<ProjectBaseInfo> tempBaseInfoList = new LinkedList<>();
    private List<ProjectItem> tempProjectItems = new ArrayList<>();
    private List<ProjectItem> simpleProjectItems = new ArrayList<>();
    private List<ProjectItem> multiProjectItems = new ArrayList<>();

    private int topBgImageTranslucentScrollDistance;//状态栏设置完全透明时顶部背景底图所需滚动的距离
    private float alpha = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_project_list;
    }


    @Override
    protected void initView() {
        ViewGroup.LayoutParams bannerParams = ivTopBg.getLayoutParams();
        ViewGroup.LayoutParams titleBarParams = mToolbar.getLayoutParams();
        //计算公式=底图高度-toolbar高度-状态栏高度-人为定义的偏差(这里取值30)
        topBgImageTranslucentScrollDistance = bannerParams.height - titleBarParams.height - ImmersionBar.getStatusBarHeight(mActivity) - DensityUtil.Dp2Px(getActivity(), 30);

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateSystemBarColor();
        activity = (NewMainActivity) getActivity();
        userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null) {
            if (userInfo.getUser() != null) {
                UserInfo.UserBean user = userInfo.getUser();
                userId = user.getId();
            }
            if (userInfo.getDepartments() != null && userInfo.getDepartments().size() > 0) {
                companyID = userInfo.getDepartments().get(0).getCompanyID();
            }
        }

        initSimpleAdapter();
        initMultiItemAdapter();
        setListener();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshProjects();
            }
        });

        refreshProjects();
    }

    private void initSimpleAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.transparent), 0, DensityUtil.Dp2Px(getActivity(), 14));
        mRecyclerView.addItemDecoration(mItemDecoration);
        simpleAdapter = new CommonAdapter<ProjectItem>(getActivity(), R.layout.item_project_info_normal, simpleProjectItems) {
            @Override
            protected void convert(CommonViewHolder holder, final ProjectItem projectItem, final int position) {
                if (!(projectItem.getObject() instanceof ProjectDetailInfo)) {
                    return;
                }

                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                holder.setImageResource(R.id.ic_thumbnail, ProjectImageHelper.getSensorResourceID(detailInfo.getProjectTypeID()));
                holder.setText(R.id.tv_project_name, detailInfo.getProjectName());
                holder.setText(R.id.tv_company_name, detailInfo.getCompanyName());
                holder.setText(R.id.tv_create_time, detailInfo.getBuildTime());
                holder.setVisibleOrGone(R.id.tv_top_flag, detailInfo.isTop());
                holder.setVisibleOrGone(R.id.iv_outdate_flag, detailInfo.isOutOfDate());
            }
        };
        mRecyclerView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ProjectItem projectItem = simpleProjectItems.get(position);
                if (!(projectItem.getObject() instanceof ProjectDetailInfo)) {
                    return;
                }

                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                //已过期的项目，针对非米度公司的用户进行限制操作
                if (detailInfo.isOutOfDate() && companyID != 1) {
                    ProjectExpiredGuideDialog customPopup = new ProjectExpiredGuideDialog(getActivity(), detailInfo.getRegisterTime());
                    new XPopup.Builder(getContext())
                            .asCustom(customPopup)
                            .show();
                } else {
                    DevicesInProjectActivity.startActivity(activity);
                }
            }
        });
        mRecyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        mRecyclerView.setOnItemMenuClickListener(mItemMenuClickListener);
        mRecyclerView.setAdapter(simpleAdapter);
    }

    private void initMultiItemAdapter() {
        mRecyclerViewGroup.setLayoutManager(new LinearLayoutManager(getActivity()));
        multiAdapter = new ProjectMultipleItemAdapter(multiProjectItems);
        multiAdapter.setAnimationEnable(true);
        multiAdapter.setAnimationFirstOnly(false);
        multiAdapter.setOnItemClickListener(new com.chad.library.adapter.base.listener.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                ProjectItem projectItem = multiAdapter.getItem(position);
                if (projectItem.getItemType() == ProjectItem.ITEM_TOP)
                    return;

                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                //已过期的项目，针对非米度公司的用户进行限制操作
                if (detailInfo.isOutOfDate() && companyID != 1) {
                    ProjectExpiredGuideDialog customPopup = new ProjectExpiredGuideDialog(getActivity(), detailInfo.getRegisterTime());
                    new XPopup.Builder(getContext())
                            .asCustom(customPopup)
                            .show();
                } else {
                    DevicesInProjectActivity.startActivity(activity);
                }
            }
        });

        mRecyclerViewGroup.setAdapter(multiAdapter);
    }

    private void setListener() {
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY <= topBgImageTranslucentScrollDistance) {
                    alpha = (float) scrollY / topBgImageTranslucentScrollDistance;

                    updateSystemBarColor();
                } else {
                    if (alpha < 1) {
                        alpha = 1;

                        updateSystemBarColor();
                    }
                }
            }
        });
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

            ProjectItem projectItem = (ProjectItem) simpleAdapter.getDatas().get(position);
            ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();

            // 只添加Item右侧的菜单。
            {
                SwipeMenuItem addItem = new SwipeMenuItem(getActivity())
                        .setBackground(R.drawable.bg_corner_6dp_blue)
                        .setText(detailInfo.isTop() ? "取消置顶" : "置顶")
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
//                ToastUtils.show("list第" + position + "; 右侧菜单第" + menuPosition);
                ProjectItem projectItem = (ProjectItem) simpleAdapter.getDatas().get(position);
                if (projectItem.getObject() instanceof ProjectDetailInfo) {
                    ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                    updateTopState(detailInfo);
                }
            }
        }
    };

    /**
     * 置顶设置
     *
     * @param detailInfo
     */
    private void updateTopState(ProjectDetailInfo detailInfo) {
        List<Integer> projectIDs = new ArrayList<>();
        projectIDs.add(detailInfo.getProjectID());
        if (detailInfo.isTop()) {
            processUnTopUserProject(projectIDs);
        } else {
            processTopUserProject(projectIDs);
        }
    }

    @OnClick({R.id.search_container, R.id.iv_view_in_map, R.id.iv_filter})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_container:
                ProjectSearchActivity.startActivity(getActivity());
                break;

            case R.id.iv_view_in_map://在地图中浏览项目
                ViewProjectsInMapActivity.startActivity(getActivity());
                break;

            case R.id.iv_filter://推出项目筛选条件抽屉窗口
//                showFilterPopupView(v);
                showFilterDialog();
                break;
        }
    }

    private void showFilterDialog() {
        if (projectDialog == null) {
            projectDialog = new FilterProjectDialog();
            projectDialog.setOnFilterPopupViewListener(new FilterProjectDialog.OnFilterPopupViewListener() {
                @Override
                public void onSearchClick() {
                    ProjectSearchActivity.startActivity(getActivity());
                }

                @Override
                public void onMapClick() {
                    ViewProjectsInMapActivity.startActivity(getActivity());
                }

                @Override
                public void onFilterResult(ProjectGroupViewMode viewMode, ProjectState state) {
                    projectGroupViewMode = viewMode;
                    projectState = state;
                    //列表模式时，直接筛选缓存的tempProjectItems
                    if (viewMode == ProjectGroupViewMode.SIMPLE_LIST) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mRecyclerViewGroup.setVisibility(View.GONE);
                        filterSimpleListProjectsByState();
                    } else {
                        mRecyclerView.setVisibility(View.GONE);
                        mRecyclerViewGroup.setVisibility(View.VISIBLE);
                        //分组展示模式时，需要请求不同的分组接口刷新数据
                        refreshProjects();
                    }
                }
            });
        }

        projectDialog.setLastCheckedItem(projectGroupViewMode, projectState);
        projectDialog.show(getChildFragmentManager(), "dialog");
    }

    private void showFilterPopupView(final View view) {
        if (popupView == null) {
            popupView = (ProjectFilterPopupView) new XPopup.Builder(getContext())
                    .atView(toolBarTopDivider)
                    .setPopupCallback(new SimpleCallback() {
                        @Override
                        public void beforeShow(BasePopupView popupView) {
                            toolBarTopDivider.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void beforeDismiss(BasePopupView popupView) {
                            toolBarTopDivider.setVisibility(View.INVISIBLE);
                        }
                    })
                    .asCustom(new ProjectFilterPopupView(getContext(), new ProjectFilterPopupView.OnFilterPopupViewListener() {
                        @Override
                        public void onSearchClick() {
                            ProjectSearchActivity.startActivity(getActivity());
                        }

                        @Override
                        public void onMapClick() {
                            ViewProjectsInMapActivity.startActivity(getActivity());
                        }

                        @Override
                        public void onFilterResult(ProjectGroupViewMode viewMode, ProjectState state) {
                            projectGroupViewMode = viewMode;
                            projectState = state;
                            //列表模式时，直接筛选缓存的tempProjectItems
                            if (viewMode == ProjectGroupViewMode.SIMPLE_LIST) {
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mRecyclerViewGroup.setVisibility(View.GONE);
                                filterSimpleListProjectsByState();

                            } else {
                                mRecyclerView.setVisibility(View.GONE);
                                mRecyclerViewGroup.setVisibility(View.VISIBLE);
                                //分组展示模式时，需要请求不同的分组接口刷新数据
                                refreshProjects();
                            }
                        }
                    }));
        }

        popupView.show();
    }

    private void refreshProjects() {
        // 进入页面，刷新数据
        swipeRefresh.setRefreshing(true);

        switch (projectGroupViewMode) {
            case SIMPLE_LIST:
                queryUserListProject();
                break;

            case GROUP_BY_LEVEL:
                getLevelProjList();
                break;

            case GROUP_BY_REGION:
                queryUserRegionProject();
                break;

            case GROUP_BY_TYPE:
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
                .subscribe(new BaseObserver<List<IndustryTypeProjectInfo>>() {
                    @Override
                    public void Success(List<IndustryTypeProjectInfo> data, String message) {
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
        multiProjectItems.clear();
        for (CustomLevelProjectInfo info : infos) {
            List<ProjectItem> subProjectItems = filterGroupListProjectsByState(info.getLevelProjs());
            if (!subProjectItems.isEmpty()) {
                multiProjectItems.add(new ProjectItem(info.getLevelName(), ProjectItem.ITEM_TOP));
                multiProjectItems.addAll(subProjectItems);
            }
        }

        multiAdapter.notifyDataSetChanged();
        nestedScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.scrollTo(0, nestedScrollView.getTop());
            }
        }, 500);
    }

    /**
     * 设置按照行政区域分组展示适配器数据
     *
     * @param infos
     */
    private void setRegionModeAdapterData(List<RegionProjectInfo> infos) {
        multiProjectItems.clear();
        for (RegionProjectInfo info : infos) {
            List<ProjectItem> subProjectItems = filterGroupListProjectsByState(info.getProjects());
            if (!subProjectItems.isEmpty()) {
                multiProjectItems.add(new ProjectItem(info.getRegionFullName(), ProjectItem.ITEM_TOP));
                multiProjectItems.addAll(subProjectItems);
            }
        }
        multiAdapter.notifyDataSetChanged();
        nestedScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.scrollTo(0, nestedScrollView.getTop());
            }
        }, 500);
    }

    /**
     * 设置按照项目类型分组展示适配器数据
     *
     * @param infos
     */
    private void setTypeModeAdapterData(List<IndustryTypeProjectInfo> infos) {
        multiProjectItems.clear();
        for (IndustryTypeProjectInfo info : infos) {
            List<ProjectItem> subProjectItems = filterGroupListProjectsByState(info.getProjects());
            if (!subProjectItems.isEmpty()) {
                multiProjectItems.add(new ProjectItem(info.getProjTypeName(), ProjectItem.ITEM_TOP));
                multiProjectItems.addAll(subProjectItems);
            }
        }
        multiAdapter.notifyDataSetChanged();
        nestedScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.scrollTo(0, nestedScrollView.getTop());
            }
        }, 500);
    }

    /**
     * 根据项目状态过滤简单项目列表
     */
    private void filterSimpleListProjectsByState() {
        simpleProjectItems.clear();
        switch (projectState) {
            case ALL:
                simpleProjectItems.addAll(tempProjectItems);
                break;

            case ON_LINE://筛选出在线的项目
                for (ProjectItem item : tempProjectItems) {
                    if (item.getObject() instanceof ProjectDetailInfo) {
                        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                        if (detailInfo.isIsValid()) {
                            simpleProjectItems.add(item);
                        }
                    }
                }
                break;

            case OFF_LINE://筛选出离线的项目
                for (ProjectItem item : tempProjectItems) {
                    if (item.getObject() instanceof ProjectDetailInfo) {
                        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                        if (!detailInfo.isIsValid()) {
                            simpleProjectItems.add(item);
                        }
                    }
                }
                break;

            case OUT_OF_DATE://筛选出过期的项目
                for (ProjectItem item : tempProjectItems) {
                    if (item.getObject() instanceof ProjectDetailInfo) {
                        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                        if (detailInfo.isOutOfDate()) {
                            simpleProjectItems.add(item);
                        }
                    }
                }
                break;
        }

        simpleAdapter.notifyDataSetChanged();
        nestedScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.scrollTo(0, nestedScrollView.getTop());
            }
        }, 500);
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

        if (subProjectItems.size() > 0) {
            subProjectItems.get(subProjectItems.size() - 1).setItemType(ProjectItem.ITEM_BOTTOM);
        }
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


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateSystemBarColor();
        }
    }

    private void updateSystemBarColor() {
        if (alpha < 1) {
            mToolbar.setBackgroundColor(ColorUtils.blendARGB(Color.TRANSPARENT
                    , ContextCompat.getColor(mActivity, R.color.white), alpha));
            searchLayout.setBackgroundResource(R.drawable.bg_search_project_white);
            ivMap.setImageResource(R.drawable.ic_project_map);
            ivFilter.setImageResource(R.drawable.ic_filter_project_normal);

            ImmersionBar.with(ProjectListFragment.this)
                    .statusBarColor(R.color.transparent, alpha)
                    .statusBarDarkFont(false)
                    .navigationBarDarkIcon(true)
                    .navigationBarColor(R.color.white)
                    .init();
        } else {
            mToolbar.setBackgroundColor(ColorUtils.blendARGB(Color.TRANSPARENT
                    , ContextCompat.getColor(mActivity, R.color.white), 1));
            searchLayout.setBackgroundResource(R.drawable.bg_search_project_gray);
            ivMap.setImageResource(R.drawable.ic_project_map_black);
            ivFilter.setImageResource(R.drawable.ic_filter_project_checked);

            ImmersionBar.with(ProjectListFragment.this)
                    .statusBarColor(R.color.white, 1)
                    .statusBarDarkFont(true)
                    .navigationBarDarkIcon(true)
                    .navigationBarColor(R.color.white)
                    .init();
        }
    }
}
