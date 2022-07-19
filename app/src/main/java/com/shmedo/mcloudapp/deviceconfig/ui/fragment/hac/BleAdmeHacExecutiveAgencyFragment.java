package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.R;

public class BleAdmeHacExecutiveAgencyFragment extends Fragment {


    public static BleAdmeHacExecutiveAgencyFragment newInstance() {
        return new BleAdmeHacExecutiveAgencyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ble_adme_hac_executive_agency, container, false);
    }



}