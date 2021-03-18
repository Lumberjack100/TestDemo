package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.configlibrary.iot.model.SensorErrnoInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：       Vms 终端信息适配器
 */
public class VmsTerminalInfoAdapter extends BaseQuickAdapter<VmsTerminalInfo, BaseViewHolder> {

    public VmsTerminalInfoAdapter(@Nullable List<VmsTerminalInfo> data) {
        super(R.layout.item_vms_terminal_info, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, VmsTerminalInfo vmsTerminalInfo) {
        holder.setText(R.id.tv_terminal_sn, vmsTerminalInfo.getSn());
        holder.setText(R.id.tv_signal_strength, vmsTerminalInfo.getUprssi() + "dBm");
        holder.setText(R.id.tv_last_data_time, vmsTerminalInfo.getLastpackagetime());

        DecimalFormat df = new DecimalFormat("#");//格式化小数
        String rate = df.format(vmsTerminalInfo.getVolt()) + "%";
        holder.setText(R.id.tv_battery_value, rate);

        if (vmsTerminalInfo.getStatus() == 1) {//在线
            holder.setTextColorRes(R.id.tv_terminal_sn, R.color.title_text_color);
//            holder.setImageResource(R.id.iv_signal, R.drawable.ic_device_signal_three);
            holder.setImageResource(R.id.iv_battery, R.drawable.ic_battery_full_online);
            holder.setText(R.id.tv_terminal_state, "在线");
            holder.setTextColorRes(R.id.tv_terminal_state, R.color.text_color_54DA99);
        } else {
            holder.setTextColorRes(R.id.tv_terminal_sn, R.color.text_color_b3b3b3);
//            holder.setImageResource(R.id.iv_signal, R.drawable.ic_device_signal_offline);
            holder.setImageResource(R.id.iv_battery, R.drawable.ic_battery_full_offline);
            holder.setText(R.id.tv_terminal_state, "离线");
            holder.setTextColorRes(R.id.tv_terminal_state, R.color.text_color_b3b3b3);
        }

        processSensorInsertState(holder, vmsTerminalInfo.getSensor());
    }

    private void processSensorInsertState(BaseViewHolder holder, List<SensorErrnoInfo> errnoInfoList) {
        if (errnoInfoList == null || errnoInfoList.size() == 0) {
            holder.setGone(R.id.ll_sensor_container, true);
            return;
        }
        holder.setGone(R.id.ll_sensor_container, false);

        LayoutInflater inflater = LayoutInflater.from(MCloudApp.getContext());
        LinearLayout sensorContainer = holder.getView(R.id.ll_sensor_container);
        sensorContainer.removeAllViews();
        for (SensorErrnoInfo errnoInfo : errnoInfoList) {
            TextView tvSensor = (TextView) inflater.inflate(R.layout.item_sensor_insert_state, null);
            tvSensor.setText(errnoInfo.getNum().equals("0") ? "" : errnoInfo.getNum());
            tvSensor.setBackgroundResource(errnoInfo.getIn().equals("0") ? R.drawable.bg_sensor_uninsert : R.drawable.bg_sensor_insert);
            // 定义LayoutParam
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.Dp2Px(MCloudApp.getContext(), 28), ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = sensorContainer.getChildCount() > 0 ? DensityUtil.Dp2Px(MCloudApp.getContext(), 5) : 0;
            tvSensor.setLayoutParams(params);

            sensorContainer.addView(tvSensor);
        }
    }
}
