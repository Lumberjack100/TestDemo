package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shmedo.mcloudapp.R;

public class TcpE40HomeFragment extends Fragment {

    private TcpE40HomeViewModel mViewModel;

    public static TcpE40HomeFragment newInstance() {
        return new TcpE40HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tcp_e40_home_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TcpE40HomeViewModel.class);

    }

}