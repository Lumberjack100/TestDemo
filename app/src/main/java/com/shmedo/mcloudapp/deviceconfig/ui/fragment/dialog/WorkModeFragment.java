package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkModeFragment extends BaseDialogFragment {

    public WorkModeFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_work_mode;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
