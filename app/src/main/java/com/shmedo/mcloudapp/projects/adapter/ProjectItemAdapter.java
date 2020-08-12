package com.shmedo.mcloudapp.projects.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.projects.model.ProjectItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：     TODO
 */
public class ProjectItemAdapter extends BaseMultiItemQuickAdapter<ProjectItem, BaseViewHolder> {
    public ProjectItemAdapter(int sectionHeadResId, int layoutResId, @Nullable List<ProjectItem> data) {
//        super(sectionHeadResId, layoutResId, data);
//        addChildLongClickViewIds(R.id.card_view);
    }


//    @Override
//    protected void convertHeader(@NotNull BaseViewHolder holder, @NotNull ProjectItem item) {
//        if (item.getObject() instanceof String) {
//            holder.setText(R.id.tv_project_group_name, (String) item.getObject());
//        }
//    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProjectItem item) {

    }
}
