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
import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsTerminalInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalExternalSensorHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class NetVmsTerminalListFragment extends BaseFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsTerminalInfoAdapter adapter;
    private List<VmsTerminalInfo> vmsTerminalInfoList = new ArrayList<>();
    public DeviceInfo deviceInfo;

    private NetVmsHomeFragment vmsHomeFragment;
    private VmsViewModel vmsViewModel;

    public static NetVmsTerminalListFragment newInstance(DeviceInfo deviceInfo) {
        NetVmsTerminalListFragment fragment = new NetVmsTerminalListFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceInfo = getArguments().getParcelable(EXTRA_DEVICE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_list_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        initAdapter();
        vmsHomeFragment = (NetVmsHomeFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
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
                VmsTerminalInfo vmsTerminalInfo = vmsTerminalInfoList.get(position);
                if (vmsTerminalInfo.getItemType() == VmsTerminalInfo.SCAN_ADD_DEVICE) {
                    return;
                }
                VmsTerminalHomeActivity.startActivity(mActivity, deviceInfo, vmsTerminalInfo);
            }
        });
        adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                VmsTerminalInfo vmsTerminalInfo = vmsTerminalInfoList.get(position);
                showRemoveTerminalDialog(position, vmsTerminalInfo.getSn());
                return true;
            }
        });
        adapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                VmsTerminalInfo vmsTerminalInfo = vmsTerminalInfoList.get(position);
                VmsTerminalExternalSensorHomeActivity.startActivity(mActivity, deviceInfo, vmsTerminalInfo);
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * 删除终端警告
     */
    private void showRemoveTerminalDialog(final int position, final String sn) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content(getWarnMessage(sn))
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
                        vmsViewModel.removeTerminalFromCacheList(sn);
//                        vmsTerminalInfoList.remove(position);
                        adapter.removeAt(position);
                        vmsHomeFragment.removeTerminal(sn);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private CharSequence getWarnMessage(final String sn) {
        SpannableStringBuilder builder = new SpannableStringBuilder(sn);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "确认移除 ");
        builder.append(" 设备?");

        return builder;
    }

    public void clearTerminalList() {
        vmsTerminalInfoList.clear();
    }

    public void addTerminalList(List<VmsTerminalInfo> dataList) {
        try {
            for (VmsTerminalInfo newTerminalInfo : dataList) {
                boolean isExist = false;
                for (VmsTerminalInfo terminalInfo : vmsTerminalInfoList) {
                    if (newTerminalInfo.getSn().equals(terminalInfo.getSn())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    vmsTerminalInfoList.add(newTerminalInfo);
                }
            }
            adapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getTerminalSize() {
        return vmsTerminalInfoList.size();
    }

}