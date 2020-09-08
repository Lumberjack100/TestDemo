package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.projects.ui.activity.DeviceSearchActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleDeviceListFragment extends BaseFragment {

    @BindView(R.id.tv_ble_device_count)
    TextView mTvDeviceCount;

    @BindView(R.id.iv_ble_scan_refresh)
    ImageView mIvBleScanRefresh;

    @BindView(R.id.tv_ble_scan_state)
    TextView mTvScanState;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_device_list;
    }


    @OnClick({R.id.search_container, R.id.ll_scan_refresh})
    public void onClick(View v) {
        if (v.getId() == R.id.search_container) {
            DeviceSearchActivity.startActivity(getActivity());
        } else if (v.getId() == R.id.ll_scan_refresh) {
            DeviceSearchActivity.startActivity(getActivity());
        }
    }


}
