package com.shmedo.mcloudapp.deviceconfig.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/11 <br/>
 * 描述：     Vms 网关通道信息 item_vms_aisle_info
 */
public class VmsAisleAdapter extends BaseQuickAdapter<ConfigModule, BaseViewHolder> {

    public VmsAisleAdapter(@Nullable List<ConfigModule> data) {
        super(R.layout.item_vms_aisle_info, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ConfigModule configModule) {
        holder.setText(R.id.tv_aisle_name, "通道01");
        holder.setText(R.id.tv_terminal_equipment_count, "设备(14)");

        holder.setText(R.id.tv_network_number, configModule.getName());
        holder.setText(R.id.tv_address, configModule.getDesc());
        holder.setText(R.id.tv_communication_channel, configModule.getDesc());
        holder.setText(R.id.tv_signal_strength, configModule.getDesc());
//        holder.setImageResource(R.id.iv_config_logo, configModule.getIconResId());

    }
}
