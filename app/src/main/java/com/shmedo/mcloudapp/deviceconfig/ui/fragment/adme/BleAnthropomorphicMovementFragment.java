package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.R;

public class BleAnthropomorphicMovementFragment extends Fragment {


    public static BleAnthropomorphicMovementFragment newInstance() {
        return new BleAnthropomorphicMovementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.anthropomorphic_movement_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}