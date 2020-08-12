package com.shmedo.mcloudapp.projects.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.EmptyDataView;
import com.shmedo.mcloudapp.entity.ProjectDetailInfoDao;
import com.shmedo.mcloudapp.projects.adapter.ProjectMultipleItemAdapter;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectItem;
import com.shmedo.mcloudapp.util.DaoManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ProjectSearchActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_data)
    EmptyDataView mEmptyData;

    private ProjectMultipleItemAdapter groupAdapter;
    private List<ProjectItem> projectItems = new ArrayList<>();


    private int userId;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProjectSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_project_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("项目查询");
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            userId = user.getId();
        }
        initAdapter();
        initData();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new ProjectMultipleItemAdapter(projectItems);
        groupAdapter.setAnimationEnable(true);
        groupAdapter.setAnimationFirstOnly(false);
        groupAdapter.setOnItemClickListener(new com.chad.library.adapter.base.listener.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {

            }
        });

        mRecyclerView.setAdapter(groupAdapter);
    }

    private void initData() {
        List<ProjectDetailInfo> resultList = DaoManager.getInstance().getDaoSession().getProjectDetailInfoDao().queryBuilder()
                .where(ProjectDetailInfoDao.Properties.UserId.eq(userId))
                .list();

        if (resultList != null && resultList.size() > 0) {
            projectItems.clear();
            for (int i = 0; i < resultList.size(); i++) {
                if (i % 5 == 0) {
                    ProjectItem item = new ProjectItem("分组：" + i, ProjectItem.ITEM_TOP);
                    projectItems.add(item);
                }
                projectItems.add(new ProjectItem(resultList.get(i), ProjectItem.ITEM_MIDDLE));
            }
            groupAdapter.notifyDataSetChanged();
        }
    }
}
