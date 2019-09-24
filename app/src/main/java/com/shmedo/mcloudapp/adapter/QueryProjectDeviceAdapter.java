package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.GlideUtils;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.adapter
 * 文件名:   AllDeviceAdapter
 * 创建者:   dpc
 * 创建时间:  2019/4/12 11:17
 * 描述：    设备管理中设备的适配器
 */
public class QueryProjectDeviceAdapter extends RecyclerView.Adapter<QueryProjectDeviceAdapter.MyViewHolder> {

    private Context mContext;
    private List<ProjectDeviceInfo> data;

    public QueryProjectDeviceAdapter(Context mContext, List<ProjectDeviceInfo>  data) {
        this.mContext = mContext;
        this.data = data;
    }


    @NonNull @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_project_device,viewGroup,false);
        return new MyViewHolder(view);
    }


    @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

                holder.tv_name.setText(data.get(i).getDeviceName());
                holder.tv_deviceTypeName.setText(data.get(i).getDeviceTypeName());
                holder.tv_createTime.setText(data.get(i).getCreateTime());
                holder.device_status.setText(data.get(i).getDeviceStatus());
                switch (data.get(i).getDeviceTypeName()){
                    case "DAS":
                            GlideUtils.loadResImage(mContext, R.drawable.das, holder.device_photo);
                        break;
                    case "DAG":
                            GlideUtils.loadResImage(mContext, R.drawable.dag, holder.device_photo);
                        break;
                    case "TPS":
                            GlideUtils.loadResImage(mContext, R.drawable.icon_other, holder.device_photo);
                        break;
                        default:
                            GlideUtils.loadResImage(mContext, R.drawable.icon_other, holder.device_photo);
                            break;
                }
    }


    @Override public int getItemCount() {
        return data.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{


        //private TextView tv_deviceStatus;

        private TextView tv_name;
        private TextView tv_deviceTypeName;
        private TextView tv_createTime;
        private TextView device_status;
        private ImageView device_photo;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            device_photo = itemView.findViewById(R.id.device_photo);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_deviceTypeName = itemView.findViewById(R.id.tv_deviceTypeName);
            tv_createTime = itemView.findViewById(R.id.tv_createTime);
            device_status = itemView.findViewById(R.id.device_status);

        }
    }
}
