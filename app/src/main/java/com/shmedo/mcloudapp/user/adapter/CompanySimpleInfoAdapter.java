package com.shmedo.mcloudapp.user.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.user.model.BasicCompanyInfo;

import org.jetbrains.annotations.NotNull;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/19 <br/>
 * 描述：    公司简单信息适配器
 */
public class CompanySimpleInfoAdapter extends BaseQuickAdapter<BasicCompanyInfo, BaseViewHolder> implements LoadMoreModule {
    public CompanySimpleInfoAdapter() {
        super(R.layout.item_company_simple_info);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, BasicCompanyInfo basicCompanyInfo) {
        holder.setText(R.id.tv_name, basicCompanyInfo.getCompanyName());

        if (basicCompanyInfo.isChecked()) {
            holder.setTextColorRes(R.id.tv_name, R.color.text_color_343434);
            holder.setGone(R.id.iv_checked_flag, false);
        } else {
            holder.setTextColorRes(R.id.tv_name, R.color.text_color_666666);
            holder.setGone(R.id.iv_checked_flag, true);
        }
    }
}
