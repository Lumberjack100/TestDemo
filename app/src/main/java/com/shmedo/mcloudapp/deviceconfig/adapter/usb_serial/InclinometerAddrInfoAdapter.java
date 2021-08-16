package com.shmedo.mcloudapp.deviceconfig.adapter.usb_serial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.InclinometerMacInfo;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/13 <br/>
 * 描述：     蓝牙测斜仪扫描地址信息适配器
 */
public class InclinometerAddrInfoAdapter extends BaseQuickAdapter<InclinometerMacInfo, BaseViewHolder> {

    public InclinometerAddrInfoAdapter(@Nullable List<InclinometerMacInfo> data) {
        super(R.layout.item_inclinometer_mac, data);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder holder, InclinometerMacInfo inclinometerMacInfo) {
        String info = String.format("No:%s Addr:%s RSSI:%s", inclinometerMacInfo.getNo(), inclinometerMacInfo.getAddr(), inclinometerMacInfo.getRssi());
        holder.setText(R.id.tv_info, info);

        if (inclinometerMacInfo.isChecked()) {
            holder.setGone(R.id.iv_checked_flag, false);

        } else {
            holder.setGone(R.id.iv_checked_flag, true);
        }
    }
}
