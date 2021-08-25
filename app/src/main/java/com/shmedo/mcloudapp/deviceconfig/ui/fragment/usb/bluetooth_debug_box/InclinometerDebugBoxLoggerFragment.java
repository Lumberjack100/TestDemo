package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.BaseUSBSerialCommunicateFragment;

public class InclinometerDebugBoxLoggerFragment extends BaseUSBSerialCommunicateFragment {

    private InclinometerDebugBoxLoggerViewModel mViewModel;

    public static InclinometerDebugBoxLoggerFragment newInstance() {
        return new InclinometerDebugBoxLoggerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.inclinometer_debug_box_logger_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(InclinometerDebugBoxLoggerViewModel.class);

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inclinometer_debug_box, menu);
//        menu.findItem(R.id.hex).setChecked(hexEnabled);
//        menu.findItem(R.id.controlLines).setChecked(controlLinesEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {

            return true;

        }else {
            return super.onOptionsItemSelected(item);
        }
    }

}