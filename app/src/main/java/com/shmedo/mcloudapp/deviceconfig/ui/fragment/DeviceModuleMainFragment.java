package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.common.ui.fragment.BaseTranslucentFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeEnum;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigE60Activity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.QueryDeviceDataActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.projects.adapter.ProjectPageAdapter;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

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
                } else {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("蓝牙");
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

        if (tab.getPosition() == 0) {
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
                ConfigDASActivity.startActivity(getActivity(), deviceInfo);
                break;

            case "ADME":
                ConfigADMEActivity.startActivity(getActivity(), deviceInfo);
                break;

            case "E60":
                DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
                deviceBasicInfoResult.setDeviceToken(localData[2]);
                deviceBasicInfoResult.setDeviceTypeName(localData[1]);
                ConfigE60Activity.startActivity(getActivity(), deviceBasicInfoResult);
                break;
        }
    }
}
