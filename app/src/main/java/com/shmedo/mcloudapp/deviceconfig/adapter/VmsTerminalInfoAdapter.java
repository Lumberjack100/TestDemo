package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.text.TextUtils;
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
import com.shmedo.mcloudapp.util.DateUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：       Vms 终端信息适配器
 */
public class VmsTerminalInfoAdapter extends BaseQuickAdapter<VmsTerminalInfo, BaseViewHolder> {

    public VmsTerminalInfoAdapter(@Nullable List<VmsTerminalInfo> data) {
        super(R.layout.item_vms_terminal_info, data);
        addChildClickViewIds(R.id.tv_look_sensors);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, VmsTerminalInfo vmsTerminalInfo) {
        holder.setText(R.id.tv_terminal_sn, String.valueOf(vmsTerminalInfo.getSn()));
        holder.setText(R.id.tv_asile_number, String.valueOf(vmsTerminalInfo.getAsileNumber()));
        holder.setText(R.id.tv_address, String.valueOf(vmsTerminalInfo.getAddr()));
        holder.setText(R.id.tv_signal_strength, vmsTerminalInfo.getUprssi() + "dBm");
        holder.setText(R.id.tv_last_data_time, vmsTerminalInfo.getLastpackagetime());

        DecimalFormat df = new DecimalFormat("#");//格式化小数
        String rate = df.format(vmsTerminalInfo.getVolt()) + "%";
        holder.setText(R.id.tv_battery_value, rate);

        if (vmsTerminalInfo.getStatus() == 1) {//在线
            holder.setTextColorRes(R.id.tv_terminal_sn, R.color.title_text_color);
//            holder.setImageResource(R.id.iv_signal, R.drawable.ic_device_signal_three);
            holder.setImageResource(R.id.iv_battery, R.drawable.ic_battery_full_online);
            holder.setTextColorRes(R.id.tv_terminal_state, R.color.text_color_54DA99);
            holder.setText(R.id.tv_terminal_state, "在线");

        } else {
//            holder.setImageResource(R.id.iv_signal, R.drawable.ic_device_signal_offline);
            holder.setText(R.id.tv_terminal_state, processTerminalState(vmsTerminalInfo.getLastpackagetime(), vmsTerminalInfo.getLogintime()));
            if (vmsTerminalInfo.getLastpackagetime().equals("离线")) {
                holder.setTextColorRes(R.id.tv_terminal_sn, R.color.text_color_b3b3b3);
                holder.setImageResource(R.id.iv_battery, R.drawable.ic_battery_full_offline);
                holder.setTextColorRes(R.id.tv_terminal_state, R.color.text_color_b3b3b3);
            } else {
                holder.setTextColorRes(R.id.tv_terminal_sn, R.color.title_text_color);
                holder.setImageResource(R.id.iv_battery, R.drawable.ic_battery_full_online);
                holder.setTextColorRes(R.id.tv_terminal_state, R.color.title_text_color);
            }
        }
        processSensorInsertState(holder, vmsTerminalInfo);
    }

    private String processTerminalState(String lastTime, String logintime) {
        if (TextUtils.isEmpty(lastTime)) {
            return "离线";
        }
        long lastPackageTime = DateUtil.stringToLong(lastTime, "yyyy/MM/dd HH:mm:ss");
        long loginTime = DateUtil.stringToLong(logintime, "yyyy/MM/dd HH:mm:ss");
        long phoneTime = new Date().getTime();
        if (((phoneTime - lastPackageTime) > 24 * 3600 * 1000) || (Math.abs(loginTime - lastPackageTime) < 2 * 1000)) {
            return "离线";
        } else {
            return "待机";
        }
    }

    private void processSensorInsertState(BaseViewHolder holder, VmsTerminalInfo vmsTerminalInfo) {
        List<SensorErrnoInfo> errnoInfoList = vmsTerminalInfo.getSensor();
        if (errnoInfoList == null || errnoInfoList.size() == 0) {
            holder.setGone(R.id.ll_sensor_container, true);
            return;
        }
        holder.setGone(R.id.ll_sensor_container, false);

        LayoutInflater inflater = LayoutInflater.from(MCloudApp.getContext());
        LinearLayout sensorContainer = holder.getView(R.id.ll_sensor_container);
        sensorContainer.removeAllViews();

        //终端接入的是雨量计时，只显示一个传感器
        if (vmsTerminalInfo.getSn().toUpperCase().endsWith("Y")) {
            SensorErrnoInfo errnoInfo = errnoInfoList.get(0);
            addSensorView(inflater, sensorContainer, errnoInfo);

        } else {
            for (SensorErrnoInfo errnoInfo : errnoInfoList) {
                addSensorView(inflater, sensorContainer, errnoInfo);
            }
        }
    }

    private void addSensorView(LayoutInflater inflater, LinearLayout sensorContainer, SensorErrnoInfo errnoInfo) {
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
