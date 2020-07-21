package com.shmedo.mcloudapp.projects.adapter.provider;

import android.view.View;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;

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
//        holder.setImageResource(R.id.iv, entity.getImg());
        holder.setText(R.id.tv, entity.getName());
    }

    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data, int position) {
    }
}
