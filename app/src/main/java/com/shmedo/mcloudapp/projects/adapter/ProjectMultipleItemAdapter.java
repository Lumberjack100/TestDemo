package com.shmedo.mcloudapp.projects.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
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
 * 创建时间:  2020/8/12 <br/>
 * 描述：     TODO
 */
public class ProjectMultipleItemAdapter extends BaseMultiItemQuickAdapter<ProjectItem, BaseViewHolder> {
    public ProjectMultipleItemAdapter(@Nullable List<ProjectItem> data) {
        super(data);
        addItemType(ProjectItem.ITEM_TOP, R.layout.item_project_info_top);
        addItemType(ProjectItem.ITEM_MIDDLE, R.layout.item_project_info_middle);
        addItemType(ProjectItem.ITEM_BOTTOM, R.layout.item_project_info_bottom);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProjectItem item) {
        switch (holder.getItemViewType()) {
            case ProjectItem.ITEM_TOP:
                if (item.getObject() instanceof String) {
                    holder.setText(R.id.tv_project_group_name, (String) item.getObject());
                }
                break;
            case ProjectItem.ITEM_MIDDLE:

            case ProjectItem.ITEM_BOTTOM: {
                ProjectDetailInfo detailInfo = (ProjectDetailInfo) item.getObject();
                ImageView mIvThumbnail = holder.getView(R.id.ic_thumbnail);
                GlideUtils.loadImage(MCloudApp.getContext(), detailInfo.getImagePath(), mIvThumbnail, R.drawable.ic_project_default, R.drawable.ic_photo);
                holder.setText(R.id.tv_project_name, detailInfo.getProjectName());
                holder.setText(R.id.tv_create_time, detailInfo.getBuildTime());
                holder.setText(R.id.tv_company_name, detailInfo.getCompanyName());
            }
            break;
        }
    }
}
