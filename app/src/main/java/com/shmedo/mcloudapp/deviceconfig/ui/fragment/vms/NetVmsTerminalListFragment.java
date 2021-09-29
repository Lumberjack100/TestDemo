package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalExternalSensorHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class NetVmsTerminalListFragment extends BaseFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsTerminalInfoAdapter adapter;
    private List<VmsTerminalInfo> vmsTerminalInfoList = new ArrayList<>();
    private VmsTerminalInfo vmsTerminalInfo;
    public ProjectDeviceInfo projectDeviceInfo;

    private NetVmsHomeFragment vmsHomeFragment;
    private VmsViewModel vmsViewModel;

    public static NetVmsTerminalListFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetVmsTerminalListFragment fragment = new NetVmsTerminalListFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(EXTRA_DEVICE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_list_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        initAdapter();
        vmsHomeFragment = (NetVmsHomeFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        adapter = new VmsTerminalInfoAdapter(vmsTerminalInfoList);
//        adapter.setAnimationEnable(false);
//        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                VmsTerminalHomeActivity.startActivity(mActivity, projectDeviceInfo, vmsTerminalInfo);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                showRemoveTerminalDialog();
                return true;
            }
        });
        adapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                vmsTerminalInfo = vmsTerminalInfoList.get(position);
                VmsTerminalExternalSensorHomeActivity.startActivity(mActivity, projectDeviceInfo, vmsTerminalInfo);
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 删除终端警告
     */
    private void showRemoveTerminalDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content(getWarnMessage())
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        vmsTerminalInfoList.remove(vmsTerminalInfo);
                        vmsViewModel.removeTerminal(vmsTerminalInfo.getSn());
                        adapter.notifyDataSetChanged();
                        vmsHomeFragment.removeTerminal(vmsTerminalInfo.getSn());
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private CharSequence getWarnMessage() {
        SpannableStringBuilder builder = new SpannableStringBuilder(vmsTerminalInfo.getSn());
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue_52B4F8));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "确认移除 ");
        builder.append(" 设备?");

        return builder;
    }

    public void clearTerminalList() {
        vmsTerminalInfoList.clear();
    }

    public void updateTerminalList(List<VmsTerminalInfo> dataList) {
        vmsTerminalInfoList.addAll(dataList);
        adapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

}