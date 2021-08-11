package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shmedo.mcloudapp.R;

public class BluetoothDebugBoxHomeFragment extends Fragment {

    private BluetoothDebugBoxHomeViewModel mViewModel;

    public static BluetoothDebugBoxHomeFragment newInstance() {
        return new BluetoothDebugBoxHomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bluetooth_debug_box_home_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BluetoothDebugBoxHomeViewModel.class);
        // TODO: Use the ViewModel
    }

}