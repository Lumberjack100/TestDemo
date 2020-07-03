package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;

/**
 * 蓝牙设备列表页面
 */
public class BluetoothDeviceListFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;



    public static BluetoothDeviceListFragment newInstance(String param1, String param2) {
        BluetoothDeviceListFragment fragment = new BluetoothDeviceListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_bluetooth_device_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initView();
        initAdapter();
        return view;
    }

    private void initView() {

    }

    private void initAdapter() {

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


    @Override
    public boolean onBackPressed() {
        return false;
    }

}
