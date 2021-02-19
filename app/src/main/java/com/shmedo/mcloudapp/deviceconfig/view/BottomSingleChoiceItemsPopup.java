package com.shmedo.mcloudapp.deviceconfig.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxj.xpopup.core.BottomPopupView;
import com.shmedo.mcloudapp.R;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/15 <br/>
 * 描述：     TODO #gh#
 */
public class BottomSingleChoiceItemsPopup extends BottomPopupView {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private CommonAdapter adapter;
    private List<String> itemList = new ArrayList<>();

    public BottomSingleChoiceItemsPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.bottom_single_choice_items_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mRecyclerView = findViewById(R.id.recyclerview);
        initAdapter();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        adapter = new CommonAdapter<String>(mContext, R.layout.item_device_firmware_info, itemList) {
            @Override
            protected void convert(CommonViewHolder holder, final String projectItem, final int position) {

            }
        };
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mRecyclerView.setAdapter(adapter);
    }


}
