package com.shmedo.mcloudapp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 文件名:   VerticalSwipeRefreshLayout
 * 创建者:   dpc
 * 创建时间:  2019/4/18 15:39
 * 描述：    自定义SwipeRefreshLayout
 */
public class VerticalSwipeRefreshLayout extends SwipeRefreshLayout {

    //实际需要滑动的child view
    private View mScrollUpChild;

    public VerticalSwipeRefreshLayout(Context context) {
        super(context);
    }

    public VerticalSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollUpChild(View view) {
        mScrollUpChild = view;
    }

    @Override
    public boolean canChildScrollUp() {
        if (mScrollUpChild != null) {
            if (android.os.Build.VERSION.SDK_INT < 14) {
                if (mScrollUpChild instanceof AbsListView) {
                    final AbsListView absListView = (AbsListView) mScrollUpChild;
                    return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
                } else {
                    return ViewCompat.canScrollVertically(mScrollUpChild, -1) || mScrollUpChild.getScrollY() > 0;
                }
            } else {
                return ViewCompat.canScrollVertically(mScrollUpChild, -1);
            }
        }
        return super.canChildScrollUp();
    }
}
