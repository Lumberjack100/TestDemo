package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.shmedo.mcloudapp.R;


public class SupendPartitionView extends ConstraintLayout {
    public SupendPartitionView(Context context) {
        this(context, null);
    }

    public SupendPartitionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SupendPartitionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_supend_partition, this, true);
    }
}
