package com.shmedo.mcloudapp.projects.ui.fragment;

import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.gyf.immersionbar.ImmersionBar;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.dialog.BaseTranslucentDialogFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.projects.adapter.FilterItemAdapter;
import com.shmedo.mcloudapp.projects.model.FilterItem;
import com.shmedo.mcloudapp.projects.model.enums.ProjectState;
import com.shmedo.mcloudapp.projects.model.enums.ProjectGroupViewMode;
import com.shmedo.mcloudapp.projects.model.StateFilterItem;
import com.shmedo.mcloudapp.projects.model.TypeFilterItem;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/16 <br/>
 * 描述：     TODO #gh#
 */
public class FilterProjectDialog extends BaseTranslucentDialogFragment {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.search_container)
    ViewGroup searchLayout;

    @BindView(R.id.iv_search_icon)
    ImageView mIvSearchIcon;

    @BindView(R.id.tv_search_hint)
    TextView mTvSearchHint;

    @BindView(R.id.iv_view_in_map)
    ImageView ivMap;

    @BindView(R.id.iv_filter)
    ImageView ivFilter;

    @BindView(R.id.recyclerView_project_type)
    RecyclerView mRecyclerViewProType;

    @BindView(R.id.recyclerView_project_state)
    RecyclerView mRecyclerViewProState;

    private FilterItemAdapter projectTypeAdapter, projectStateAdapter;

    private ProjectGroupViewMode projectGroupViewMode = ProjectGroupViewMode.SIMPLE_LIST;
    private ProjectState filterScope = ProjectState.ALL;

    private OnFilterPopupViewListener mListener;

    @Override
    public void onStart() {
        super.onStart();
        mWindow.setGravity(Gravity.TOP);
        mWindow.setWindowAnimations(R.style.TopAnimation);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    protected int setLayoutId() {
        return R.layout.project_filter_popup;
    }

    @Override
    protected void initImmersionBar() {
        ImmersionBar.with(this)
                .titleBar(toolbar)
                .statusBarColor(R.color.gray_909090)
                .statusBarDarkFont(false)
                .navigationBarWithKitkatEnable(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                .init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImmersionBar.with(this)
                .navigationBarWithKitkatEnable(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                .init();
    }


    @Override
    protected void initView() {
        toolbar.setBackgroundResource(R.color.white);
        searchLayout.setBackgroundResource(R.drawable.bg_search_project_gray);
        mIvSearchIcon.setImageResource(R.drawable.ic_search_project);
        mTvSearchHint.setTextColor(GlobalUtil.getColor(R.color.text_color_b3b3b3));
        ivMap.setImageResource(R.drawable.ic_project_map_dark);
        ivFilter.setImageResource(R.drawable.ic_filter_project_checked);
        setUpProjectTypeRecyclerView();
        setUpProjectStateRecyclerView();
    }

    @Override
    protected void initData() {
        initProjectTypeData();
        initProjectStateData();
    }

    private void setUpProjectTypeRecyclerView() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(getActivity(), 7);//每一个矩形的间距
        mRecyclerViewProType.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
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

                projectGroupViewMode = filterItem.getProjectGroupViewMode();
            }
        });
    }

    private void setUpProjectStateRecyclerView() {
        int spanCount = 4;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(getActivity(), 7);//每一个矩形的间距
        mRecyclerViewProState.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
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

    @OnClick({R.id.search_container, R.id.iv_view_in_map, R.id.iv_filter, R.id.ll_reset, R.id.tv_confirm})
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
                mListener.onFilterResult(ProjectGroupViewMode.SIMPLE_LIST, ProjectState.ALL);
                break;

            case R.id.tv_confirm:
                mListener.onFilterResult(projectGroupViewMode, filterScope);
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
        TypeFilterItem filterItem = new TypeFilterItem("列表", ProjectGroupViewMode.SIMPLE_LIST);
        projectTypeAdapter.addData(filterItem);

        filterItem = new TypeFilterItem("分级", ProjectGroupViewMode.GROUP_BY_LEVEL);
        projectTypeAdapter.addData(filterItem);

        filterItem = new TypeFilterItem("行政区域", ProjectGroupViewMode.GROUP_BY_REGION);
        projectTypeAdapter.addData(filterItem);

        filterItem = new TypeFilterItem("项目类型", ProjectGroupViewMode.GROUP_BY_TYPE);
        projectTypeAdapter.addData(filterItem);

        for (FilterItem item : projectTypeAdapter.getData()) {
            item.setChecked(false);
            TypeFilterItem typeFilterItem = (TypeFilterItem) item;
            if (typeFilterItem.getProjectGroupViewMode() == projectGroupViewMode) {
                typeFilterItem.setChecked(true);
            }
        }
        projectTypeAdapter.notifyDataSetChanged();
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

        for (FilterItem item : projectStateAdapter.getData()) {
            item.setChecked(false);
            StateFilterItem stateFilterItem = (StateFilterItem) item;
            if (stateFilterItem.getFilterScope() == filterScope) {
                stateFilterItem.setChecked(true);
            }
        }

        projectStateAdapter.notifyDataSetChanged();
    }

    public void setLastCheckedItem(ProjectGroupViewMode projectGroupViewMode, ProjectState filterScope) {
        this.projectGroupViewMode = projectGroupViewMode;
        this.filterScope = filterScope;
    }

    public void setOnFilterPopupViewListener(OnFilterPopupViewListener listener) {
        this.mListener = listener;
    }

    public interface OnFilterPopupViewListener {
        void onSearchClick();

        void onMapClick();

        void onFilterResult(ProjectGroupViewMode projectGroupViewMode, ProjectState filterScope);
    }
}
