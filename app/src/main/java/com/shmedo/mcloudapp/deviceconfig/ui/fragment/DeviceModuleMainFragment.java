package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.DeviceModuleSwitchTabEvent;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.common.ui.fragment.BaseTranslucentFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeEnum;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.QueryDeviceDataActivity;
import com.shmedo.mcloudapp.projects.adapter.ProjectPageAdapter;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceModuleMainFragment extends BaseTranslucentFragment implements TabLayout.OnTabSelectedListener {
    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager2 viewPager;

    @BindView(R.id.iv_scan_device_code)
    ImageView ivScanDeviceCode;

    private FragmentStateAdapter pagerAdapter;
    private TabLayoutMediator tabLayoutMediator;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_device_module_main;
    }

    @Override
    protected void initView() {
        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(new NetDeviceListFragment());
        mFragments.add(new BleDeviceListFragment());
        mFragments.add(new WiFiDeviceListFragment());
        pagerAdapter = new ProjectPageAdapter((FragmentActivity) mActivity, mFragments);
        viewPager.setAdapter(pagerAdapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("网络");
                    textView.setTextColor(ContextCompat.getColor(mActivity, R.color.title_text_color));
                    textView.setTextSize(18);
                    tab.setCustomView(textView);
                } else if (position == 1) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("蓝牙");
                    textView.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                    textView.setTextSize(17);
                    tab.setCustomView(textView);
                } else if (position == 2) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("WIFI");
                    textView.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                    textView.setTextSize(17);
                    tab.setCustomView(textView);
                }
            }
        });
        tabLayoutMediator.attach();
        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        TextView textView = (TextView) tab.getCustomView();
        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.title_text_color));
        textView.setTextSize(18);

        if (tab.getPosition() == 0 || tab.getPosition() == 2) {
            ivScanDeviceCode.setVisibility(View.INVISIBLE);
        } else {
            ivScanDeviceCode.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        TextView textView = (TextView) tab.getCustomView();
        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
        textView.setTextSize(17);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        if (messageEvent instanceof DeviceModuleSwitchTabEvent) {
            DeviceModuleSwitchTabEvent switchTabEvent = (DeviceModuleSwitchTabEvent) messageEvent;
            viewPager.setCurrentItem(switchTabEvent.getTabPosition());
        }
    }

    @OnClick({R.id.iv_query_data, R.id.iv_scan_device_code})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_query_data:
                QueryDeviceDataActivity.startActivity(mActivity, "");
                break;

            case R.id.iv_scan_device_code:
                PermissionHelper.requestScanPermissions(DeviceModuleMainFragment.this);
                break;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case XPermissionUtils.REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                        Timber.d("扫描结果为：%s", content);
                        scanResult(content);
                    }
                }
                break;
        }
    }

    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (result.contains("=")) {
            String results = result.substring(result.indexOf("=") + 1);
            scan(results);
        } else {
            scan(result);
        }
    }

    /**
     * 处理扫描结果，例如：MEDO,189150L,DAS
     */
    private void scan(String deviceInfo) {
        if (!deviceInfo.startsWith("MEDO")) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        String[] localData = deviceInfo.split(",");
        if (localData.length != 3) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (TextUtils.isEmpty(localData[0]) || TextUtils.isEmpty(localData[1]) || TextUtils.isEmpty(localData[2])) {
            showTipDialog("二维码信息不能为空");
            return;
        }

        if (localData[1].length() != 7) {
            showTipDialog("设备标识有误,请扫码正确的设备二维码");
            return;
        }

        if (!DeviceTypeEnum.value(localData[2])) {
            showTipDialog("此设备类型暂时不支持");
            return;
        }

        MCloudApp.setCurDeviceToken(localData[1]);
        MCloudApp.setCurDeviceMacAddr(null);

        switch (localData[2]) {
            case "DAS":
                DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.BLE_CONNECT, deviceInfo);
                break;

            case "ADME":

                break;

        }
    }
}
