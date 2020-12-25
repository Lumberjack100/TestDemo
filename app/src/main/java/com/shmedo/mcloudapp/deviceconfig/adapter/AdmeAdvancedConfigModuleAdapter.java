package com.shmedo.mcloudapp.deviceconfig.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/25/20 <br/>
 * 描述：     ADME设备高级配置功能模块适配器
 */
public class AdmeAdvancedConfigModuleAdapter extends BaseQuickAdapter<ConfigModule, BaseViewHolder> {
    public AdmeAdvancedConfigModuleAdapter(@Nullable List<ConfigModule> data) {
        super(R.layout.item_adme_advanced_config_module, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ConfigModule configModule) {
        holder.setImageResource(R.id.iv_config_logo, configModule.getIconResId());
        holder.setText(R.id.tv_config_name, configModule.getName());
        holder.setText(R.id.tv_config_desc, configModule.getDesc());
    }
}
