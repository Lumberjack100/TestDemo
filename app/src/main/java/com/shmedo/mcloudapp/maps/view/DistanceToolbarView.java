package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/29 <br/>
 * 描述：    TODO #gh#
 */
public class DistanceToolbarView extends FrameLayout {
    private Context mContext;

    @BindView(R.id.tv_distance)
    public TextView mTvDistance;

    @BindView(R.id.iv_remove_marker)
    public ImageView mIvRemoveMarker;

    @BindView(R.id.iv_clear_markers)
    public ImageView mIvClearMarkers;

    private OnDistanceToolbarViewClickListener mListener;


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

    @OnClick({R.id.back, R.id.iv_remove_marker, R.id.iv_clear_markers})
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.back:
                mListener.onCancelDistanceClick();
                break;

            case R.id.iv_remove_marker:
                mListener.onRemoveMarkerClick();
                break;

            case R.id.iv_clear_markers:
                mListener.onClearMarkersClick();
                break;
        }
    }

    public void setOnDistanceToolbarViewClickListener(OnDistanceToolbarViewClickListener listener) {
        this.mListener = listener;
    }


    public interface OnDistanceToolbarViewClickListener {
        /**
         * 取消测距
         */
        void onCancelDistanceClick();

        void onRemoveMarkerClick();

        void onClearMarkersClick();

    }
}
