package com.shmedo.mcloudapp.projects.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.shmedo.mcloudapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConstructionFragment extends Fragment {

    public ConstructionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_construction, container, false);
    }
}
