package com.shmedo.mcloudapp.projects.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.projects.ui.ViewProjectsInMapActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectListFragment extends BaseFragment {
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_search_hint)
    TextView mTvSearchHint;

    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout mSmartRefreshLayout;

    @BindView(R.id.recycler_project)
    RecyclerView mRecyclerProject;


    @Override
    protected int initContentView() {
        return R.layout.fragment_project_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        initAdapter();
        return view;
    }



    private void initView() {
        mToolbarTitle.setText("项目列表");
    }

    private void initAdapter() {
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
}
