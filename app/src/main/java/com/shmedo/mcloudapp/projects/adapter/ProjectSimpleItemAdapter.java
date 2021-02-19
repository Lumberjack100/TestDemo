package com.shmedo.mcloudapp.projects.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.helper.ProjectImageHelper;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/22 <br/>
 * 描述：     TODO #gh#
 */
public class ProjectSimpleItemAdapter extends BaseQuickAdapter<ProjectItem, BaseViewHolder> {
    public ProjectSimpleItemAdapter(int layoutResId, @Nullable List<ProjectItem> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProjectItem item) {
        ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
//                ImageView mIvThumbnail = holder.getView(R.id.ic_thumbnail);
//                GlideUtils.loadImage(MCloudApp.getContext(), detailInfo.getImagePath(), mIvThumbnail, R.drawable.ic_project_default, R.drawable.ic_project_default);
        holder.setImageResource(R.id.ic_thumbnail, ProjectImageHelper.getSensorResourceID(detailInfo.getProjectTypeID()));
        holder.setText(R.id.tv_project_name, detailInfo.getProjectName());
        holder.setText(R.id.tv_company_name, detailInfo.getCompanyName());
        holder.setText(R.id.tv_create_time, detailInfo.getBuildTime());
    }
}
