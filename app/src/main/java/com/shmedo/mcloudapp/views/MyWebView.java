package com.shmedo.mcloudapp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 文件名:   MyWebView
 * 创建者:   dpc
 * 创建时间:  2019/3/6 10:18
 * 描述：    重写webview
 */
public class MyWebView extends WebView {
    private ViewGroup viewGroup;
    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyWebView(Context context) {
        super(context);
    }


    public ViewGroup getViewGroup() {
        return viewGroup;
    }


    public void setViewGroup(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (this.getScrollY() <= 0) {
                    this.scrollTo(0,1);
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

}
