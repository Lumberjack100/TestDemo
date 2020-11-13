package com.shmedo.mcloudapp.deviceconfig.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.configlibrary.iot.model.VmsAisleTerminalInfo;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/11 <br/>
 * 描述：     Vms 网关通道信息 item_vms_aisle_info
 */
public class VmsAisleAdapter extends BaseQuickAdapter<VmsAisleTerminalInfo, BaseViewHolder> {

    public VmsAisleAdapter(@Nullable List<VmsAisleTerminalInfo> data) {
        super(R.layout.item_vms_aisle_info, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, VmsAisleTerminalInfo vmsAisleTerminalInfo) {
        if (vmsAisleTerminalInfo.getChannel() == 0) {
            holder.setText(R.id.tv_aisle_name, "通道01");
            holder.setGone(R.id.tv_terminal_equipment_count, true);
        } else if (vmsAisleTerminalInfo.getChannel() == 1) {
            holder.setText(R.id.tv_aisle_name, "通道02");
        } else if (vmsAisleTerminalInfo.getChannel() == 2) {
            holder.setText(R.id.tv_aisle_name, "通道03");
        }

        String terminalCount = String.format("设备(%s)", vmsAisleTerminalInfo.getTerminalnum());
        holder.setText(R.id.tv_terminal_equipment_count, terminalCount);

        holder.setText(R.id.tv_network_number, String.valueOf(vmsAisleTerminalInfo.getNetid()));
        holder.setText(R.id.tv_address, String.valueOf(vmsAisleTerminalInfo.getAddr()));
        holder.setText(R.id.tv_communication_channel, String.valueOf(vmsAisleTerminalInfo.getChannel()));
        holder.setText(R.id.tv_signal_strength, String.valueOf(vmsAisleTerminalInfo.getNetid()));
//        holder.setImageResource(R.id.iv_config_logo, configModule.getIconResId());

    }
}
