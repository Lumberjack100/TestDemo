package com.shmedo.mcloudapp.projects.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/21 <br/>
 * 描述：     TODO
 */
public class SlidingConflictRecyclerView extends RecyclerView {

    public SlidingConflictRecyclerView(@NonNull Context context) {
        super(context);
    }

    public SlidingConflictRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingConflictRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean canScrollHorizontally = canScrollHorizontally(-1) || canScrollHorizontally(1);
        boolean canScrollVertically = canScrollVertically(-1) || canScrollVertically(1);
        if (canScrollHorizontally) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        } else if (canScrollVertically) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(false);
            }
        }
        return super.dispatchTouchEvent(event);
    }

}

