package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/29 <br/>
 * 描述：    TODO
 */
public class DistanceToolbarView extends FrameLayout {
    private Context mContext;

    @BindView(R.id.recyclerViewLayer)
    RecyclerView mRecyclerView;

    public DistanceToolbarView(@NonNull Context context) {
        this(context, null);
    }

    public DistanceToolbarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DistanceToolbarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.map_distance_toolbar, this, true);
        ButterKnife.bind(this);
        mContext = context;
    }
}
