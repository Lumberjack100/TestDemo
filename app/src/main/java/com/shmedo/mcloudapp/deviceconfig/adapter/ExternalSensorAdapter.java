package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ExternalSensorItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/1/6 <br/>
 * 描述：     单个通道下接入的物模型列表
 */
public class ExternalSensorAdapter extends RecyclerView.Adapter<ExternalSensorAdapter.ViewHolder> {
    public static final int TYPE_ADD = 1;
    public static final int TYPE_ITEM = 2;
    private final LayoutInflater mInflater;
    private final ArrayList<ExternalSensorItem> list = new ArrayList<>();
    private int itemMax = 16;


    public ExternalSensorAdapter(Context context, List<ExternalSensorItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.list.addAll(data);
    }

    public int getItemMax() {
        return itemMax;
    }

    public void setItemMax(int itemMax) {
        this.itemMax = itemMax;
    }

    public ArrayList<ExternalSensorItem> getData() {
        return list;
    }

    public void remove(int position) {
        if (position < list.size()) {
            list.remove(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImg;
        ImageView mIvDel;
        TextView tvAddr;

        public ViewHolder(View view) {
            super(view);
            mImg = view.findViewById(R.id.iv_sensor);
            mIvDel = view.findViewById(R.id.iv_del);
            tvAddr = view.findViewById(R.id.tv_address);
        }
    }

    @Override
    public int getItemCount() {
        if (list.size() < itemMax) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    private boolean isShowAddItem(int position) {
        int size = list.size();
        return position == size;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowAddItem(position)) {
            return TYPE_ADD;
        } else {
            return TYPE_ITEM;
        }
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.item_external_sensor, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * 设置值
     */
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //少于MaxSize，显示继续添加的图标
        if (getItemViewType(position) == TYPE_ADD) {
            viewHolder.mImg.setImageResource(R.drawable.ic_add_sensor);
            viewHolder.mImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.addItem();
                    }
                }
            });
            viewHolder.mIvDel.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.tvAddr.setVisibility(View.VISIBLE);
            viewHolder.mIvDel.setVisibility(View.VISIBLE);
            viewHolder.mIvDel.setOnClickListener(view -> {
                int index = viewHolder.getAbsoluteAdapterPosition();
                if (index != RecyclerView.NO_POSITION && list.size() > index) {
                    if (mItemClickListener != null) {
                        mItemClickListener.deleteItem(index);
                    }
                }
            });
            ExternalSensorItem item = list.get(position);
            viewHolder.mImg.setImageResource(item.getResId() == -1 ? R.drawable.ic_sensor_holder_bright : item.getResId());
            if (!TextUtils.isEmpty(item.getSensorAddress())) {
                try {
                    String sensorAisle = item.isVibratingWireSensor() ? String.valueOf(Integer.parseInt(item.getSensorAddress()) + 1) : item.getSensorAddress();
                    viewHolder.tvAddr.setText(sensorAisle);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            //itemView 的点击事件
            if (mItemClickListener != null) {
                viewHolder.itemView.setOnClickListener(v -> {
                    int adapterPosition = viewHolder.getAbsoluteAdapterPosition();
                    mItemClickListener.onItemClick(v, adapterPosition);
                });
            }
            if (mItemLongClickListener != null) {
                viewHolder.itemView.setOnLongClickListener(v -> {
                    int adapterPosition = viewHolder.getAbsoluteAdapterPosition();
                    mItemLongClickListener.onItemLongClick(viewHolder, adapterPosition, v);
                    return true;
                });
            }
        }
    }

    public interface OnItemClickListener {
        /**
         * Item click event
         *
         * @param v
         * @param position
         */
        void onItemClick(View v, int position);

        void addItem();

        void deleteItem(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(RecyclerView.ViewHolder holder, int position, View v);
    }

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    public void setItemLongClickListener(OnItemLongClickListener l) {
        this.mItemLongClickListener = l;
    }

}
