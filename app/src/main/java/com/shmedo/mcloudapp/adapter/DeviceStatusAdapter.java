package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.util.ImageUtil;

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
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        holder.ivState.setImageResource(R.drawable.ic_dot_unassigned);
        if (data.get(position).getLocal()) {
            holder.tv_deviceToken.setText(data.get(position).getDeviceName() + "\n" + "本地设备");
        } else {
            holder.tv_deviceToken.setText(data.get(position).getDeviceName() + "\n" + "云端设备");
        }

        holder.tv_electricity.setText(data.get(position).getVoltage() + "V");
        holder.tv_gprs.setText(StringUtil.getByteSize(data.get(position).getGprs()));
        holder.tv_signal.setText(data.get(position).getSignal() + "");
        holder.ll_SensorType.setTag(position);
        holder.itemView.setTag(position);

        sensorAndCountList = data.get(position).getSensorInfo();
        if (sensorAndCountList == null || sensorAndCountList.size() == 0) {

            holder.tv_sensorType1.setVisibility(View.GONE);
            holder.tv_sensorType2.setVisibility(View.GONE);
            holder.tv_sensorMore.setVisibility(View.GONE);
            return;
        }

        if (sensorAndCountList.size() > 1) {

            holder.tv_sensorType1.setVisibility(View.VISIBLE);
            holder.tv_sensorType2.setVisibility(View.VISIBLE);
            holder.tv_sensorMore.setVisibility(sensorAndCountList.size() > 2 ? View.VISIBLE : View.GONE);

            holder.tv_sensorType1.setCompoundDrawablesWithIntrinsicBounds(ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
            holder.tv_sensorType1.setText("x" + sensorAndCountList.get(0).getSensorCount());
            holder.tv_sensorType2.setCompoundDrawablesWithIntrinsicBounds(ImageUtil.getSensorResourceID(sensorAndCountList.get(1).getSensorType()), 0, 0, 0);
            holder.tv_sensorType2.setText("x" + sensorAndCountList.get(1).getSensorCount());

        } else {
            holder.tv_sensorType1.setVisibility(View.VISIBLE);
            holder.tv_sensorType2.setVisibility(View.GONE);
            holder.tv_sensorMore.setVisibility(View.GONE);

            holder.tv_sensorType1.setCompoundDrawablesWithIntrinsicBounds(ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
            holder.tv_sensorType1.setText("x" + sensorAndCountList.get(0).getSensorCount());
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivState;
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
            ivState = itemView.findViewById(R.id.iv_state);
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
        void onItemClick(View v, ViewName viewName, int position);
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
                    mOnItemClickListener.onItemClick(view, ViewName.PRACTISE, position);
                    break;
                default:
                    mOnItemClickListener.onItemClick(view, ViewName.ITEM, position);
                    break;
            }
        }
    }
}
