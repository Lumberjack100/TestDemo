package com.shmedo.mcloudapp.projects.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.lxj.xpopup.impl.PartShadowPopupView;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.projects.adapter.FilterItemAdapter;
import com.shmedo.mcloudapp.projects.model.FilterItem;
import com.shmedo.mcloudapp.projects.model.ProjectState;
import com.shmedo.mcloudapp.projects.model.ProjectViewMode;
import com.shmedo.mcloudapp.projects.model.StateFilterItem;
import com.shmedo.mcloudapp.projects.model.TypeFilterItem;

import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/12 <br/>
 * 描述：    项目筛选阴影弹窗
 */
public class ProjectFilterPopupView extends PartShadowPopupView implements View.OnClickListener {
    private ViewGroup searchLayout;
    private ImageView ivMap;
    private ImageView ivFilter;
    private RecyclerView mRecyclerViewProType;
    private RecyclerView mRecyclerViewProState;

    private Context mContext;
    private FilterItemAdapter projectTypeAdapter, projectStateAdapter;

    private ProjectViewMode projectViewMode = ProjectViewMode.VIEW_SIMPLE;
    private ProjectState filterScope = ProjectState.ALL;

    private OnFilterPopupViewListener mListener;


    public ProjectFilterPopupView(@NonNull Context context, OnFilterPopupViewListener listener) {
        super(context);
        ButterKnife.bind(this);
        mContext = context;
        mListener = listener;
    }


    @Override
    protected int getImplLayoutId() {
        return R.layout.project_filter_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        initView();
        setUpProjectTypeRecyclerView();
        setUpProjectStateRecyclerView();
        initProjectTypeData();
        initProjectStateData();
    }

    private void initView() {
        searchLayout = findViewById(R.id.search_container);
        ivMap = findViewById(R.id.iv_view_in_map);
        ivFilter = findViewById(R.id.iv_filter);
        mRecyclerViewProType = findViewById(R.id.recyclerView_project_type);
        mRecyclerViewProState = findViewById(R.id.recyclerView_project_state);

        searchLayout.setBackgroundResource(R.drawable.bg_search_project_gray);
        ivMap.setImageResource(R.drawable.ic_project_map_black);
        ivFilter.setImageResource(R.drawable.ic_filter_project_checked);

        searchLayout.setOnClickListener(this);
        ivMap.setOnClickListener(this);
        ivFilter.setOnClickListener(this);
        findViewById(R.id.ll_reset).setOnClickListener(this);
        findViewById(R.id.tv_confirm).setOnClickListener(this);
    }

    private void setUpProjectTypeRecyclerView() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mContext, 7);//每一个矩形的间距
        mRecyclerViewProType.setLayoutManager(new GridLayoutManager(mContext, spanCount));
        //设置每个item间距
        mRecyclerViewProType.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        projectTypeAdapter = new FilterItemAdapter();
        mRecyclerViewProType.setAdapter(projectTypeAdapter);
        projectTypeAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                TypeFilterItem filterItem = (TypeFilterItem) projectTypeAdapter.getItem(position);
                if (filterItem.isChecked())
                    return;

                for (FilterItem item : projectTypeAdapter.getData()) {
                    item.setChecked(false);
                }
                filterItem.setChecked(true);
                projectTypeAdapter.notifyDataSetChanged();

                projectViewMode = filterItem.getProjectViewMode();
            }
        });
    }

    private void setUpProjectStateRecyclerView() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mContext, 7);//每一个矩形的间距
        mRecyclerViewProState.setLayoutManager(new GridLayoutManager(mContext, spanCount));
        //设置每个item间距
        mRecyclerViewProState.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        projectStateAdapter = new FilterItemAdapter();
        mRecyclerViewProState.setAdapter(projectStateAdapter);
        projectStateAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                StateFilterItem filterItem = (StateFilterItem) projectStateAdapter.getItem(position);
                if (filterItem.isChecked())
                    return;

                for (FilterItem item : projectStateAdapter.getData()) {
                    item.setChecked(false);
                }
                filterItem.setChecked(true);
                projectStateAdapter.notifyDataSetChanged();

                filterScope = filterItem.getFilterScope();
            }
        });
    }

    public void onClick(View view) {
        if (mListener == null)
            return;

        switch (view.getId()) {
            case R.id.search_container:
                mListener.onSearchClick();
                break;

            case R.id.iv_view_in_map:
                mListener.onMapClick();
                break;

            case R.id.iv_filter:

                break;

            case R.id.ll_reset:
                resetRecyclerViewItemState();
                mListener.onFilterResult(ProjectViewMode.VIEW_SIMPLE, ProjectState.ALL);
                break;

            case R.id.tv_confirm:
                mListener.onFilterResult(projectViewMode, filterScope);
                break;
        }

        dismiss();
    }

    private void resetRecyclerViewItemState() {
        for (FilterItem item : projectTypeAdapter.getData()) {
            item.setChecked(false);
        }
        projectTypeAdapter.getData().get(0).setChecked(true);
        projectTypeAdapter.notifyDataSetChanged();

        for (FilterItem item : projectStateAdapter.getData()) {
            item.setChecked(false);
        }
        projectStateAdapter.getData().get(0).setChecked(true);
        projectStateAdapter.notifyDataSetChanged();
    }

    private void initProjectTypeData() {
        TypeFilterItem filterItem = new TypeFilterItem("列表", ProjectViewMode.VIEW_SIMPLE);
        filterItem.setChecked(true);
        projectTypeAdapter.addData(filterItem);

        filterItem = new TypeFilterItem("分级", ProjectViewMode.VIEW_GROUP_BY_LEVEL);
        projectTypeAdapter.addData(filterItem);

        filterItem = new TypeFilterItem("行政区域", ProjectViewMode.VIEW_GROUP_BY_REGION);
        projectTypeAdapter.addData(filterItem);

        filterItem = new TypeFilterItem("项目类型", ProjectViewMode.VIEW_GROUP_BY_TYPE);
        projectTypeAdapter.addData(filterItem);
    }

    private void initProjectStateData() {
        StateFilterItem filterItem = new StateFilterItem("全部", ProjectState.ALL);
        filterItem.setChecked(true);
        projectStateAdapter.addData(filterItem);

        filterItem = new StateFilterItem("在线", ProjectState.ON_LINE);
        projectStateAdapter.addData(filterItem);

        filterItem = new StateFilterItem("离线", ProjectState.OFF_LINE);
        projectStateAdapter.addData(filterItem);

        filterItem = new StateFilterItem("过期", ProjectState.OUT_OF_DATE);
        projectStateAdapter.addData(filterItem);
    }

    public interface OnFilterPopupViewListener {
        void onSearchClick();

        void onMapClick();

        void onFilterResult(ProjectViewMode projectViewMode, ProjectState filterScope);
    }
}
