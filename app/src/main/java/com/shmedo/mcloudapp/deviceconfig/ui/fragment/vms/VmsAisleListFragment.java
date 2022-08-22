package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.RecycleViewDivider;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsAisleAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class VmsAisleListFragment extends BaseFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.recyclerviewAisle)
    RecyclerView mRecyclerViewAisle;

    private VmsAisleAdapter vmsAisleAdapter;
    private List<VmsAisleInfo> vmsAisleInfoList = new ArrayList<>();


    @Override
    protected int getLayoutId() {
        return R.layout.vms_aisle_list_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerViewAisle.setLayoutManager(linearLayoutManager);
        mRecyclerViewAisle.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(15f), com.blankj.utilcode.util.ColorUtils.getColor(R.color.transparent)));
        vmsAisleAdapter = new VmsAisleAdapter(vmsAisleInfoList);
        vmsAisleAdapter.setAnimationEnable(false);
        vmsAisleAdapter.setAnimationFirstOnly(false);
        mRecyclerViewAisle.setAdapter(vmsAisleAdapter);
    }

    public void clearAisleListInfo() {
        int size = vmsAisleInfoList.size();
        vmsAisleInfoList.clear();
        vmsAisleAdapter.notifyItemRangeRemoved(0, size);
    }

    public void updateAisleListInfo(VmsAisleInfo vmsAisleInfo) {
        vmsAisleInfoList.add(vmsAisleInfo);
        vmsAisleAdapter.notifyItemInserted(vmsAisleInfoList.size() - 1);
    }
}