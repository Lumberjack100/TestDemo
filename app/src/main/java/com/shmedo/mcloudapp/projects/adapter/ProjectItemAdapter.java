package com.shmedo.mcloudapp.projects.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectItem;
import com.shmedo.mcloudapp.util.GlideUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：     TODO
 */
public class ProjectItemAdapter extends BaseSectionQuickAdapter<ProjectItem, BaseViewHolder> {
    public ProjectItemAdapter(int sectionHeadResId, int layoutResId, @Nullable List<ProjectItem> data) {
        super(sectionHeadResId, layoutResId, data);
        addChildLongClickViewIds(R.id.card_view);
    }


    @Override
    protected void convertHeader(@NotNull BaseViewHolder holder, @NotNull ProjectItem item) {
        if (item.getObject() instanceof String) {
            holder.setText(R.id.tv_project_group_name, (String) item.getObject());
        }
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProjectItem item) {
        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
        ImageView mIvThumbnail = holder.getView(R.id.ic_thumbnail);
        GlideUtils.loadImage(MCloudApp.getContext(), detailInfo.getImagePath(), mIvThumbnail, R.drawable.ic_project_default, R.drawable.ic_photo);
        holder.setText(R.id.tv_project_name, detailInfo.getProjectName());
        holder.setText(R.id.tv_create_time, detailInfo.getBuildTime());
        holder.setText(R.id.tv_company_name, detailInfo.getCompanyName());

        if (detailInfo.isOutOfDate()) {
            holder.setVisible(R.id.tv_project_state, true);
            holder.setText(R.id.tv_project_state, "已过期");
        } else {
            holder.setVisible(R.id.tv_project_state, false);
        }

        holder.setGone(R.id.iv_top, !detailInfo.isTop());
    }
}
