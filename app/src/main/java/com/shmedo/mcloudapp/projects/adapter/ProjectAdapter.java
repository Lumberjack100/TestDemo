package com.shmedo.mcloudapp.projects.adapter;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.shmedo.mcloudapp.projects.adapter.provider.ProjectGroupProvider;
import com.shmedo.mcloudapp.projects.adapter.provider.ProjectItemProvider;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/20 <br/>
 * 描述：     TODO
 */
public class ProjectAdapter extends BaseNodeAdapter {
    public ProjectAdapter() {
        super();
        addFullSpanNodeProvider(new ProjectGroupProvider());
        addNodeProvider(new ProjectItemProvider());
    }

    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> data, int position) {
        BaseNode node = data.get(position);
        if (node instanceof ProjectGroup) {
            return 0;
        } else if (node instanceof ProjectDetailInfo) {
            return 1;
        }

        return -1;
    }

}
