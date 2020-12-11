package com.shmedo.mcloudapp.deviceconfig.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.configlibrary.iot.model.VmsAisleInfo;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/11 <br/>
 * 描述：     Vms 网关通道信息 item_vms_aisle_info
 */
public class VmsAisleAdapter extends BaseQuickAdapter<VmsAisleInfo, BaseViewHolder> {

    public VmsAisleAdapter(@Nullable List<VmsAisleInfo> data) {
        super(R.layout.item_vms_aisle_info, data);
        addChildClickViewIds(R.id.tv_terminal_equipment_count);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, VmsAisleInfo vmsAisleInfo) {
        if (vmsAisleInfo.getChannel() == 1) {
            holder.setText(R.id.tv_aisle_name, "数据通道1");
        } else if (vmsAisleInfo.getChannel() == 2) {
            holder.setText(R.id.tv_aisle_name, "数据通道2");
        }

        String terminalCount = String.format("设备(%s)", vmsAisleInfo.getTerminalnum());
        holder.setText(R.id.tv_terminal_equipment_count, terminalCount);

        holder.setText(R.id.tv_network_number, String.valueOf(vmsAisleInfo.getNetid()));
        holder.setText(R.id.tv_address, String.valueOf(vmsAisleInfo.getAddr()));
        holder.setText(R.id.tv_communication_chl, String.valueOf(vmsAisleInfo.getChl()));
        holder.setText(R.id.tv_signal_strength, vmsAisleInfo.getRssi() + "dBm");
    }
}
