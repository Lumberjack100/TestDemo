package com.shmedo.mcloudapp.deviceconfig.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.configlibrary.iot.model.TerminalBean;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：     TODO
 */
public class VmsTerminalInfoAdapter extends BaseQuickAdapter<TerminalBean, BaseViewHolder> {

    public VmsTerminalInfoAdapter(@Nullable List<TerminalBean> data) {
        super(R.layout.item_vms_terminal_info, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, TerminalBean terminalBean) {
        holder.setText(R.id.tv_terminal_sn, terminalBean.getSn());
        holder.setText(R.id.tv_battery_value, String.valueOf(terminalBean.getVolt()));
        holder.setText(R.id.tv_terminal_state, "正常");
        holder.setText(R.id.tv_last_data_time, terminalBean.getLastpackagetime());

        if (terminalBean.getStatus() == 1) {//在线
            holder.setTextColorRes(R.id.tv_terminal_sn, R.color.title_text_color);
            holder.setImageResource(R.id.iv_signal, R.drawable.ic_device_signal_three);
            holder.setImageResource(R.id.iv_battery, R.drawable.ic_battery_full_online);
        } else {
            holder.setTextColorRes(R.id.tv_terminal_sn, R.color.text_color_b3b3b3);
            holder.setImageResource(R.id.iv_signal, R.drawable.ic_device_signal_offline);
            holder.setImageResource(R.id.iv_battery, R.drawable.ic_battery_full_offline);
        }

    }
}
