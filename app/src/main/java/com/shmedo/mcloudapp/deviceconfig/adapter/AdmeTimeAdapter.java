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
import com.shmedo.mcloudapp.deviceconfig.model.AdmeTimeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/5/31 <br/>
 * 描述：     TODO
 */
public class AdmeTimeAdapter extends RecyclerView.Adapter<AdmeTimeAdapter.ViewHolder> {
    public static final int TYPE_ADD = 1;
    public static final int TYPE_ITEM = 2;
    private final LayoutInflater mInflater;
    private final ArrayList<AdmeTimeItem> list = new ArrayList<>();
    private int itemMax = 8;

    public AdmeTimeAdapter(Context context, List<AdmeTimeItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.list.addAll(data);
    }

    public int getItemMax() {
        return itemMax;
    }

    public void setItemMax(int itemMax) {
        this.itemMax = itemMax;
    }

    public ArrayList<AdmeTimeItem> getData() {
        return list;
    }

    public void remove(int position) {
        if (position < list.size()) {
            list.remove(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        ImageView mImg;

        public ViewHolder(View view) {
            super(view);
            tvTime = view.findViewById(R.id.tv_time);
            mImg = view.findViewById(R.id.iv_add_time);
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
    public AdmeTimeAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.item_time, viewGroup, false);
        return new AdmeTimeAdapter.ViewHolder(view);
    }
    /**
     * 设置值
     */
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //少于MaxSize，显示继续添加的图标
        if (getItemViewType(position) == TYPE_ADD) {
            viewHolder.mImg.setVisibility(View.VISIBLE);
            viewHolder.tvTime.setVisibility(View.GONE);
            viewHolder.mImg.setImageResource(R.drawable.ic_add_sensor);
            viewHolder.mImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.addItem();
                    }
                }
            });

        } else {
            viewHolder.tvTime.setVisibility(View.VISIBLE);
            viewHolder.mImg.setVisibility(View.GONE);
            AdmeTimeItem admeTimeItem = list.get(position);
            if (!TextUtils.isEmpty(admeTimeItem.getTime())) {
                viewHolder.tvTime.setText(admeTimeItem.getTime());
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
