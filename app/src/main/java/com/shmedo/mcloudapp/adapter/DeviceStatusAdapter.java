package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.adapter
 * 文件名:   AllDeviceAdapter
 * 创建者:   dpc
 * 创建时间:  2019/4/12 11:17
 * 描述：    设备管理中设备的适配器
 */
public class DeviceStatusAdapter extends RecyclerView.Adapter<DeviceStatusAdapter.MyViewHolder> implements View.OnClickListener {

    private Context mContext;
    private List<StatusInfoResult> data;
    private List<SensorAndCount> sensorAndCountList = new ArrayList<>();

    public DeviceStatusAdapter(Context mContext, List<StatusInfoResult> data) {
        this.mContext = mContext;
        this.data = data;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_all_device, viewGroup, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

        holder.tv_deviceStatus.setText(data.get(i).getDeviceName());
        if (data.get(i).getLocal()) {
            holder.tv_deviceToken.setText(data.get(i).getDeviceToken() + "\n" + "本地设备");
        } else {
            holder.tv_deviceToken.setText(data.get(i).getDeviceToken() + "\n" + "云端设备");
        }

        holder.tv_electricity.setText(data.get(i).getVoltage() + "V");
        holder.tv_gprs.setText(StringUtil.setSize(data.get(i).getGprs()));
        holder.tv_signal.setText(data.get(i).getSignal() + "");
        holder.ll_SensorType.setTag(i);
        holder.itemView.setTag(i);

        sensorAndCountList = data.get(i).getSensorInfo();
        if (sensorAndCountList == null || sensorAndCountList.size() == 0) {

            holder.tv_sensorType1.setVisibility(View.GONE);
            holder.tv_sensorType2.setVisibility(View.GONE);
            holder.tv_sensorMore.setVisibility(View.GONE);
            holder.ll_SensorType.setClickable(false);
            return;
        }

        if (data.get(i).getSensorInfo().size() > 2) {

            holder.tv_sensorType1.setVisibility(View.VISIBLE);
            holder.tv_sensorType2.setVisibility(View.VISIBLE);
            holder.tv_sensorMore.setVisibility(View.VISIBLE);
            holder.ll_SensorType.setClickable(true);

            holder.tv_sensorType1.setCompoundDrawablesWithIntrinsicBounds(sensorAndCountList.get(0).getSensorType(), 0, 0, 0);
            holder.tv_sensorType1.setText("x" + sensorAndCountList.get(0).getSensorCount());

            holder.tv_sensorType2.setCompoundDrawablesWithIntrinsicBounds(sensorAndCountList.get(1).getSensorType(), 0, 0, 0);
            holder.tv_sensorType2.setText("x" + sensorAndCountList.get(1).getSensorCount());

        } else if (data.get(i).getSensorInfo().size() > 1) {

            holder.tv_sensorType1.setVisibility(View.VISIBLE);
            holder.tv_sensorType2.setVisibility(View.VISIBLE);
            holder.tv_sensorMore.setVisibility(View.GONE);
            holder.ll_SensorType.setClickable(false);

            holder.tv_sensorType1.setCompoundDrawablesWithIntrinsicBounds(sensorAndCountList.get(0).getSensorType(), 0, 0, 0);
            holder.tv_sensorType1.setText("x" + sensorAndCountList.get(0).getSensorCount());

            holder.tv_sensorType2.setCompoundDrawablesWithIntrinsicBounds(sensorAndCountList.get(1).getSensorType(), 0, 0, 0);
            holder.tv_sensorType2.setText("x" + sensorAndCountList.get(1).getSensorCount());

        } else {

            holder.tv_sensorType1.setVisibility(View.VISIBLE);
            holder.tv_sensorType2.setVisibility(View.GONE);
            holder.tv_sensorMore.setVisibility(View.GONE);
            holder.ll_SensorType.setClickable(false);

            holder.tv_sensorType1.setCompoundDrawablesWithIntrinsicBounds(sensorAndCountList.get(0).getSensorType(), 0, 0, 0);
            holder.tv_sensorType1.setText("x" + sensorAndCountList.get(0).getSensorCount());
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_deviceStatus;
        private TextView tv_deviceToken;
        private LinearLayout ll_SensorType;
        private TextView tv_sensorType1;
        private TextView tv_sensorType2;
        private TextView tv_sensorMore;
        private TextView tv_electricity;
        private TextView tv_gprs;
        private TextView tv_signal;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_deviceStatus = itemView.findViewById(R.id.tv_deviceStatus);
            tv_deviceToken = itemView.findViewById(R.id.tv_deviceToken);
            ll_SensorType = itemView.findViewById(R.id.ll_SensorType);
            tv_sensorType1 = itemView.findViewById(R.id.tv_sensor_type_1);
            tv_sensorType2 = itemView.findViewById(R.id.tv_sensor_type_2);
            tv_sensorMore = itemView.findViewById(R.id.tv_sensor_more);
            tv_electricity = itemView.findViewById(R.id.tv_electricity);//电量
            tv_gprs = itemView.findViewById(R.id.tv_gprs);
            tv_signal = itemView.findViewById(R.id.tv_signal);

            itemView.setOnClickListener(DeviceStatusAdapter.this);
            ll_SensorType.setOnClickListener(DeviceStatusAdapter.this);
        }
    }

    //item里面有多个控件可以点击（item+item内部控件）
    public enum ViewName {
        ITEM,
        PRACTISE
    }


    private OnItemClickListener mOnItemClickListener;//声明自定义的接口

    //自定义一个回调接口来实现Click和LongClick事件
    public interface OnItemClickListener {
        void onItemClick(View v, ViewName viewName, int position, List<SensorAndCount> list);
    }

    //定义方法并传给外面的使用者
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        if (mOnItemClickListener != null) {
            switch (view.getId()) {
                case R.id.recycler_device:
                    mOnItemClickListener.onItemClick(view, ViewName.PRACTISE, position, sensorAndCountList);
                    break;
                default:
                    mOnItemClickListener.onItemClick(view, ViewName.ITEM, position, sensorAndCountList);
                    break;
            }
        }
    }
}
