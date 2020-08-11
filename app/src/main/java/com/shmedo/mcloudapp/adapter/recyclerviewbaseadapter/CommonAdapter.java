package com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.List;

/**
 * Created by zhy on 16/4/9.
 */
public abstract class CommonAdapter<T> extends MultiItemTypeAdapter<T>
{
    protected Context mContext;
    protected int mLayoutId;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;

    public CommonAdapter(final Context context, final int layoutId, List<T> datas)
    {
        super(context, datas);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutId = layoutId;
        mDatas = datas;

        addItemViewDelegate(new ItemViewDelegate<T>()
        {
            @Override
            public int getItemViewLayoutId()
            {
                return layoutId;
            }

            @Override
            public boolean isForViewType(T item, int position)
            {
                return true;
            }

            @Override
            public void convert(CommonViewHolder holder, T t, int position)
            {
                CommonAdapter.this.convert(holder, t, position);
            }
        });
    }

    protected abstract void convert(CommonViewHolder holder, T t, int position);

    public void removeItem(int position)
    {
        mDatas.remove(position);
        notifyItemRemoved(position);
        if (position != mDatas.size())
        {
            notifyItemRangeChanged(position, mDatas.size() - position);
        }
    }

    public void addItem(T bean)
    {
        mDatas.add(bean);
        notifyItemInserted(mDatas.size() - 1);
    }

    public void addItem(T bean, int position)
    {
        mDatas.add(position, bean);
        notifyItemInserted(position);
        if (position != mDatas.size() - 1)
        {
            notifyItemRangeChanged(position, mDatas.size() - position);
        }
    }
}
