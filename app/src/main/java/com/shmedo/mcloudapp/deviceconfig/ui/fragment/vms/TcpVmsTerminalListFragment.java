package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.shmedo.configlibrary.iot.model.TerminalBean;
import com.shmedo.configlibrary.iot.model.VmsAisleTerminalInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseBottomSheetDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TcpVmsTerminalListFragment extends BaseBottomSheetDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsTerminalInfoAdapter adapter;

    private List<TerminalBean> terminalBeanList = new ArrayList<>();

    private VmsAisleTerminalInfo vmsAisleTerminalInfo;

    private VmsViewModel vmsViewModel;


    public TcpVmsTerminalListFragment(VmsAisleTerminalInfo vmsAisleTerminalInfo) {
        this.vmsAisleTerminalInfo = vmsAisleTerminalInfo;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_terminal_list_fragment;
    }

    /**
     * 设置弹窗高度，默认为屏幕高度的四分之三
     */
    @Override
    protected int getPeekHeight() {
        int peekHeight = getResources().getDisplayMetrics().heightPixels;
        //设置弹窗高度为屏幕高度的4/5
        return peekHeight - peekHeight / 5;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        initAdapter();
        initView();
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapter = new VmsTerminalInfoAdapter(terminalBeanList);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                TerminalBean terminalBean = terminalBeanList.get(position);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {

                return true;
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private void initView() {
        if (vmsAisleTerminalInfo == null)
            return;

        String title;
        if (vmsAisleTerminalInfo.getChannel() == 0) {
            title = "通道01-设备(";
        } else if (vmsAisleTerminalInfo.getChannel() == 1) {
            title = "通道02-设备(";
        } else {
            title = "通道03-设备(";
        }
        title += vmsAisleTerminalInfo.getTerminal().size() + ")";
        mTvTitle.setText(title);

        if (vmsAisleTerminalInfo.getTerminal().size() == 0) {
            adapter.setEmptyView(R.layout.empty_view);
            return;
        }
        terminalBeanList.clear();
        terminalBeanList.addAll(vmsAisleTerminalInfo.getTerminal());
//        terminalBeanList.addAll(vmsAisleTerminalInfo.getTerminal());
        adapter.notifyDataSetChanged();
    }

    @OnClick({R.id.iv_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

}