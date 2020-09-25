package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.helper.DispatchCmdHelper;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 通过物联网平台配置采集器
 */
public class NetCollectorSettingFragment extends BaseFragment {
    private static final String COMPANY_ID = "company_id";
    private static final String DEVICE_ID = "device_id";

    @BindView(R.id.collectorAddressET)
    EditText mEtCollectorAddress;

    @BindView(R.id.calculatingTimeET)
    EditText mEtCalculatingTime;

    @BindView(R.id.standbyTimeET)
    EditText mEtStandbyTime;

    @BindView(R.id.collectTimeET)
    EditText mEtCollectTime;

    @BindView(R.id.btn_confirm)
    Button mBtnConfirmComplete;


    private int deviceid;
    private List<String> msgIDList = new ArrayList<>();


    public static NetCollectorSettingFragment newInstance(int deviceid) {
        NetCollectorSettingFragment fragment = new NetCollectorSettingFragment();
        Bundle args = new Bundle();
        args.putInt(DEVICE_ID, deviceid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceid = getArguments().getInt(DEVICE_ID, -1);
        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_collector_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        queryCollectorInfo();
    }

    private void setFilter() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCalculatingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtStandbyTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtCollectTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
        }
    }

    private void queryCollectorInfo() {
        DispatchRawCmdParam param = new DispatchRawCmdParam();
        param.setContent("$cmd=md_getcollctrl");
        param.setCompanyID(MCloudApp.getCompanyID());
        param.setDeviceIDList(Arrays.asList(deviceid));

        showProgressDialog("加载中...");
        DispatchCmdHelper.getInstance().processDispatchRawCmd(param);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(List<DispatchCmdItem> dispatchCmdItemList) {
        dismissProgressDialog();
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            showDispatchFailedDialog();
            return;
        }

        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        showDispatchSuccessDialog();
    }

    private void showDispatchFailedDialog() {
        BaseDispatchCmdDialog newFragment = new DispatchCmdFailedDialog("采集器配置", "下发查询采集器配置信息指令失败");
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    private void showDispatchSuccessDialog() {
        BaseDispatchCmdDialog newFragment = new CommonCmdDialog("采集器配置", "正在重启中...", msgIDList);
        newFragment.show(getChildFragmentManager(), "dialog");
    }
}
