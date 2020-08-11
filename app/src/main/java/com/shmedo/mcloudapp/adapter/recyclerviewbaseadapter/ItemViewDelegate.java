package com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter;

/**
 * Created by GH on 2016/8/15.
 */
public interface ItemViewDelegate<T>
{

    int getItemViewLayoutId();

    boolean isForViewType(T item, int position);

    void convert(CommonViewHolder holder, T t, int position);

}
