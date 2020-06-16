package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.shmedo.mcloudapp.R;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/15 <br/>
 * 描述：    TODO
 */
public class SupendViewContainer extends ConstraintLayout {
    public SupendViewContainer(Context context) {
        this(context, null);
    }

    public SupendViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SupendViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_supend_container, this, true);
    }
}
