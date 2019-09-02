package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class DeviceStatusAdapter extends RecyclerView.Adapter<DeviceStatusAdapter.MyViewHolder> implements View.OnClickListener{

    private Context mContext;
    private List<StatusInfoResult> data;
    private List<SensorAndCount> countList = new ArrayList<>();

    public DeviceStatusAdapter(Context mContext, List<StatusInfoResult> data) {
        this.mContext = mContext;
        this.data = data;
    }


    @NonNull @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_all_device,viewGroup,false);
        return new MyViewHolder(view);
    }


    @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        holder.tv_deviceStatus.setText(data.get(i).getDeviceName());
        if (data.get(i).getLocal()){
            holder.tv_deviceToken.setText(data.get(i).getDeviceToken()+"\r\n"+"本地设备");
        }else {
            holder.tv_deviceToken.setText(data.get(i).getDeviceToken()+"\r\n"+"云端设备");

        }
        countList = data.get(i).getSensorInfo();
        if (countList != null){
            for (int j = 0; j < data.get(i).getSensorInfo().size() ; j++) {
                String sensorType = StringUtil.formatStringTwo(String.valueOf(countList.get(j).getSensorType()));
                switch (sensorType){
                    case "02":
                        holder.imgSensorType1.setImageResource(R.drawable.medo_icon_sensortype_2);
                        break;

                }
            }
        }

        holder.tv_electricity.setText("电量");
        holder.tv_gprs.setText(StringUtil.setSize(data.get(i).getGprs()));
        holder.tv_signal.setText(data.get(i).getSignal()+"");
        holder.ll_SensorType.setTag(i);
        holder.itemView.setTag(i);
    }


    @Override public int getItemCount() {
        return data.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{


        private TextView tv_deviceStatus;
        private TextView tv_deviceToken;
        private LinearLayout ll_SensorType;
        private ImageView imgSensorType1;
        private ImageView imgSensorType2;
        private ImageView imgSensorType3;
        private TextView tv_electricity;
        private TextView tv_gprs;
        private TextView tv_signal;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_deviceStatus = itemView.findViewById(R.id.tv_deviceStatus);
            tv_deviceToken = itemView.findViewById(R.id.tv_deviceToken);
            ll_SensorType = itemView.findViewById(R.id.ll_SensorType);
            imgSensorType1 = itemView.findViewById(R.id.img_sensor_type_1);
            imgSensorType2 = itemView.findViewById(R.id.img_sensor_type_2);
            imgSensorType3 = itemView.findViewById(R.id.img_sensor_type_3);
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
    public interface OnItemClickListener  {
        void onItemClick(View v, ViewName viewName, int position, List<SensorAndCount> list);
    }

    //定义方法并传给外面的使用者
    public void setOnItemClickListener(OnItemClickListener  listener) {
        this.mOnItemClickListener  = listener;
    }

    @Override public void onClick(View view) {
        int position = (int) view.getTag();
        if (mOnItemClickListener != null) {
            switch (view.getId()){
                case R.id.recycler_device:
                    mOnItemClickListener.onItemClick(view, ViewName.PRACTISE, position,countList);
                    break;
                    default:
                        mOnItemClickListener.onItemClick(view, ViewName.ITEM, position,countList);
                        break;
            }
        }
    }
}
