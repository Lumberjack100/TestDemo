package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shmedo.mcloudapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DispatchCmdFailedDialog extends Fragment {

    public DispatchCmdFailedDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dispatch_cmd_failed_dialog, container, false);
    }
}
