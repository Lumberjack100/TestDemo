package com.shmedo.mcloudapp.projects.adapter.provider;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.util.GlideUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/21 <br/>
 * 描述：     TODO
 */
public class ProjectItemProvider extends BaseNodeProvider {
    @Override
    public int getItemViewType() {
        return 1;
    }

    @Override
    public int getLayoutId() {
        return R.layout.listitem_project_baseinfo;
    }

    @Override
    public void convert(@NotNull BaseViewHolder holder, @Nullable BaseNode data) {
        if (data == null) {
            return;
        }

        ProjectDetailInfo entity = (ProjectDetailInfo) data;

        ImageView mIvThumbnail = holder.getView(R.id.ic_thumbnail);
        GlideUtils.loadImage(MCloudApp.getContext(), entity.getImagePath(), mIvThumbnail, R.drawable.ic_project_default, R.drawable.ic_photo);
        holder.setText(R.id.tv_project_name, entity.getProjectName());
        holder.setText(R.id.tv_create_time, entity.getBuildTime());
        holder.setText(R.id.tv_company_name, entity.getCompanyName());
    }

    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data, int position) {
    }
}
