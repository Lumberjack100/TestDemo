package com.shmedo.mcloudapp.projects.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.projects.adapter.FilterItemAdapter;
import com.shmedo.mcloudapp.projects.model.FilterItem;
import com.shmedo.mcloudapp.projects.model.ProjectState;
import com.shmedo.mcloudapp.projects.model.ProjectViewMode;
import com.shmedo.mcloudapp.projects.model.StateFilterItem;
import com.shmedo.mcloudapp.projects.model.TypeFilterItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     项目筛选设置抽屉布局
 */
public class ProjectFilterDrawerView extends LinearLayout {
    private Context mContext;

    @BindView(R.id.recyclerView_project_type)
    RecyclerView mRecyclerViewProType;

    @BindView(R.id.recyclerView_project_state)
    RecyclerView mRecyclerViewProState;

    private FilterItemAdapter adapterProjectType, adapterProjectState;

    private ProjectViewMode projectViewMode = ProjectViewMode.VIEW_SIMPLE;
    private ProjectState filterScope = ProjectState.ALL;

    private OnFilterResultListener mListener;


    public ProjectFilterDrawerView(Context context) {
        this(context, null);
    }

    public ProjectFilterDrawerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProjectFilterDrawerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.project_filter_set_layout, this, true);
        ButterKnife.bind(this);
        mContext = context;
        setUpProjectTypeRecyclerView();
        setUpProjectStateRecyclerView();
        initProjectTypeData();
        initProjectStateData();
    }


    private void setUpProjectTypeRecyclerView() {
        mRecyclerViewProType.setLayoutManager(new GridLayoutManager(mContext, 2));
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mContext, 15);//每一个矩形的间距
        //设置每个item间距
        mRecyclerViewProType.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapterProjectType = new FilterItemAdapter();
        mRecyclerViewProType.setAdapter(adapterProjectType);
        adapterProjectType.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                TypeFilterItem filterItem = (TypeFilterItem) adapterProjectType.getItem(position);
                if (filterItem.isChecked())
                    return;

                for (FilterItem item : adapterProjectType.getData()) {
                    item.setChecked(false);
                }
                filterItem.setChecked(true);
                adapterProjectType.notifyDataSetChanged();

                projectViewMode = filterItem.getProjectViewMode();
            }
        });
    }

    private void setUpProjectStateRecyclerView() {
        mRecyclerViewProState.setLayoutManager(new GridLayoutManager(mContext, 2));
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mContext, 15);//每一个矩形的间距
        //设置每个item间距
        mRecyclerViewProState.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapterProjectState = new FilterItemAdapter();
        mRecyclerViewProState.setAdapter(adapterProjectState);
        adapterProjectState.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                StateFilterItem filterItem = (StateFilterItem) adapterProjectState.getItem(position);
                if (filterItem.isChecked())
                    return;

                for (FilterItem item : adapterProjectState.getData()) {
                    item.setChecked(false);
                }
                filterItem.setChecked(true);
                adapterProjectState.notifyDataSetChanged();

                filterScope = filterItem.getFilterScope();
            }
        });
    }


    @OnClick({R.id.tv_reset, R.id.tv_confirm})
    public void onClick(View view) {
        if (mListener == null)
            return;

        switch (view.getId()) {
            case R.id.tv_reset:
                resetRecyclerViewItemState();
                mListener.onFilterResult(ProjectViewMode.VIEW_SIMPLE, ProjectState.ALL);
                break;

            case R.id.tv_confirm:
                mListener.onFilterResult(projectViewMode, filterScope);
                break;
        }
    }

    private void resetRecyclerViewItemState() {
        for (FilterItem item : adapterProjectType.getData()) {
            item.setChecked(false);
        }
        adapterProjectType.getData().get(0).setChecked(true);
        adapterProjectType.notifyDataSetChanged();

        for (FilterItem item : adapterProjectState.getData()) {
            item.setChecked(false);
        }
        adapterProjectState.getData().get(0).setChecked(true);
        adapterProjectState.notifyDataSetChanged();
    }


    private void initProjectTypeData() {
        TypeFilterItem filterItem = new TypeFilterItem("列表", ProjectViewMode.VIEW_SIMPLE);
        filterItem.setChecked(true);
        adapterProjectType.addData(filterItem);

        filterItem = new TypeFilterItem("分级", ProjectViewMode.VIEW_GROUP_BY_LEVEL);
        adapterProjectType.addData(filterItem);

        filterItem = new TypeFilterItem("行政区域", ProjectViewMode.VIEW_GROUP_BY_REGION);
        adapterProjectType.addData(filterItem);

        filterItem = new TypeFilterItem("项目类型", ProjectViewMode.VIEW_GROUP_BY_TYPE);
        adapterProjectType.addData(filterItem);
    }

    private void initProjectStateData() {
        StateFilterItem filterItem = new StateFilterItem("全部", ProjectState.ALL);
        filterItem.setChecked(true);
        adapterProjectState.addData(filterItem);

        filterItem = new StateFilterItem("在线", ProjectState.ON_LINE);
        adapterProjectState.addData(filterItem);

        filterItem = new StateFilterItem("离线", ProjectState.OFF_LINE);
        adapterProjectState.addData(filterItem);

        filterItem = new StateFilterItem("过期", ProjectState.OUT_OF_DATE);
        adapterProjectState.addData(filterItem);
    }

    public void setOnFilterSetListener(OnFilterResultListener listener) {
        this.mListener = listener;
    }

    public interface OnFilterResultListener {
        void onFilterResult(ProjectViewMode projectViewMode, ProjectState filterScope);
    }
}
