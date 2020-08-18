package com.shmedo.mcloudapp.projects.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.projects.model.ProjectSearchKeyWord;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/13 <br/>
 * 描述：     TODO
 */
public class ProjectSearchKeyWordsAdapter extends BaseQuickAdapter<ProjectSearchKeyWord, BaseViewHolder> {

    public ProjectSearchKeyWordsAdapter(@Nullable List<ProjectSearchKeyWord> data) {
        super(R.layout.item_project_search_keyword, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProjectSearchKeyWord keyWord) {
        holder.setText(R.id.tv_name, keyWord.getKeyWord());
    }
}
