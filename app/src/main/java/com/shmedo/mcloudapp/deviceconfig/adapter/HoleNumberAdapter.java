package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.model.hac.HacHoleAreaDepthInfo;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/25 <br/>
 * 描述：     测斜管对应孔号列表
 */
public class HoleNumberAdapter extends ArrayAdapter<HacHoleAreaDepthInfo> {
    private Context mContext;
    private List<HacHoleAreaDepthInfo> holelist;
    private int resoureId;

    private OnHoleChosenListener mListener;

    public HoleNumberAdapter(@NonNull Context context, int resource, @NonNull List<HacHoleAreaDepthInfo> objects, OnHoleChosenListener l) {
        super(context, resource, objects);
        this.mContext = context;
        this.resoureId = resource;
        this.holelist = objects;
        this.mListener = l;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(resoureId, parent, false);
            viewHolder.content = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        HacHoleAreaDepthInfo areaDepthInfo = holelist.get(position);
        if (null != areaDepthInfo) {
            viewHolder.content.setText(areaDepthInfo.getHoleno());
        }

        View.OnClickListener choose = v -> {
            if (mListener != null) {
                mListener.onHoleChosen(areaDepthInfo);
            }
        };
        viewHolder.content.setOnClickListener(choose);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    class ViewHolder {
        TextView content;
    }

    public interface OnHoleChosenListener {
        void onHoleChosen(HacHoleAreaDepthInfo areaDepthInfo);
    }
}
