package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.WifiBean;
import com.shmedo.mcloudapp.util.AppContants;
import com.shmedo.mcloudapp.util.WifiSupport;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.adapter
 * 文件名:   WifiListAdapter
 * 创建者:   dpc
 * 创建时间:  2019/1/24 11:42
 * 描述：    TODO
 */
public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.MyViewHolder> {

    private Context mContext;
    private List<WifiBean> resultList;
    private onItemClickListener onItemClickListener;

    public void setOnItemClickListener(WifiListAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public WifiListAdapter(Context mContext, List<WifiBean> resultList) {
        this.mContext = mContext;
        this.resultList = resultList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_wifi_list, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final WifiBean bean = resultList.get(position);
        holder.tvItemWifiName.setText(bean.getWifiName());
        holder.tvItemWifiStatus.setText("("+bean.getState()+")");

        //可以传递给adapter的数据都是经过处理的，已连接或者正在连接状态的wifi都是处于集合中的首位，所以可以写出如下判断
        if(position == 0  && (AppContants.WIFI_STATE_ON_CONNECTING.equals(bean.getState()) || AppContants.WIFI_STATE_CONNECT.equals(bean.getState()))){
            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.app_color_blue));
            holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.app_color_blue));
        }else{
            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.gray_909090));
            holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.gray_909090));
        }

        if (WifiSupport.getWifiCipher(bean.getCapabilities()) == WifiSupport.WifiCipherType.WIFICIPHER_NOPASS){
            holder.imgWifi.setBackgroundResource(R.drawable.icon_wifi_unlock);
        }else {
            holder.imgWifi.setBackgroundResource(R.drawable.icon_lock_wifi);
        }

        holder.itemview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(view,position,bean);
            }
        });
    }

    public void replaceAll(List<WifiBean> datas) {
        if (resultList.size() > 0) {
            resultList.clear();
        }
        resultList.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder{

        View itemview;
        TextView tvItemWifiName, tvItemWifiStatus;
        ImageView imgWifi;
        public MyViewHolder(View itemView) {
            super(itemView);
            itemview = itemView;
            tvItemWifiName = itemView.findViewById(R.id.tv_item_wifi_name);
            tvItemWifiStatus =  itemView.findViewById(R.id.tv_item_wifi_status);
            imgWifi = itemView.findViewById(R.id.img_wifi);
        }

    }

    public interface onItemClickListener{
        void onItemClick(View view, int postion, Object o);
    }

}
