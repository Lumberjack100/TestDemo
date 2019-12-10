package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.SystemDataInfo;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.adapter
 * 文件名:   SystemAdapter
 * 创建者:   dpc
 * 创建时间:  2019/4/12 10:51
 * 描述：    设备系统适配器
 */
public class SystemAdapter extends RecyclerView.Adapter<SystemAdapter.ViewHolder> {

    private Context mContext;
    private List<SystemDataInfo> data;
    private OnItemClickListener onItemClickListener;
    private OnItemMapClickListener onItemMapClickListener;

    public SystemAdapter(Context mContext, List<SystemDataInfo> data) {
        this.mContext = mContext;
        this.data = data;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_system,viewGroup,false);
        return new ViewHolder(view);
    }


    @Override public void onBindViewHolder(@NonNull final ViewHolder holder, int i) {
        holder.projectName.setText(data.get(i).getProjName());

        if (onItemClickListener != null){
            holder.itemSystemItem.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView,pos);
                }
            });
        }
       if (onItemMapClickListener != null){
            holder.imgMap.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    int pos = holder.getLayoutPosition();
                    onItemMapClickListener.onItemMapClick(holder.itemView,pos);
                }
            });
       }
    }

    public List<SystemDataInfo> getDataList(){
        return data;
    }

    @Override public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout itemSystemItem;
        private TextView projectName;
        private LinearLayout imgMap;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemSystemItem = itemView.findViewById(R.id.item_system_id);
            projectName = itemView.findViewById(R.id.projectName);
            imgMap = itemView.findViewById(R.id.img_map);

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    public void setOnItemClickLitener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public interface OnItemMapClickListener {
        void onItemMapClick(View view, int position);
    }
    public void setOnItemMapClickListener(OnItemMapClickListener onItemMapClickListener) {
        this.onItemMapClickListener = onItemMapClickListener;
    }
}
