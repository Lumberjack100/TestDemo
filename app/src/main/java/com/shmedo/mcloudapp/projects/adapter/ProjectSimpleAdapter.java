package com.shmedo.mcloudapp.projects.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.util.GlideUtils;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     项目普通列表展示适配器
 */
public class ProjectSimpleAdapter extends BaseQuickAdapter<ProjectDetailInfo, BaseViewHolder> {

    public ProjectSimpleAdapter() {
        super(R.layout.listitem_project_baseinfo);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProjectDetailInfo entity) {
        ImageView mIvThumbnail = holder.getView(R.id.ic_thumbnail);
        GlideUtils.loadImage(MCloudApp.getContext(), entity.getImagePath(), mIvThumbnail, R.drawable.ic_project_default, R.drawable.ic_photo);
        holder.setText(R.id.tv_project_name, entity.getProjectName());
        holder.setText(R.id.tv_create_time, entity.getBuildTime());
        holder.setText(R.id.tv_company_name, entity.getCompanyName());
    }
}
