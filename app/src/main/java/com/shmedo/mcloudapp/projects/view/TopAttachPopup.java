package com.shmedo.mcloudapp.projects.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.AttachPopupView;
import com.shmedo.mcloudapp.R;

/**
 * Description: 自定义背景的Attach弹窗
 * Create by lxj, at 2019/3/13
 */
public class TopAttachPopup extends AttachPopupView {
    private boolean top;//项目是否置顶
    private OnClickListener clickListener;

    public TopAttachPopup(@NonNull Context context) {
        super(context);
    }

    public TopAttachPopup(@NonNull Context context, boolean top, OnClickListener clickListener) {
        super(context);
        this.top = top;
        this.clickListener = clickListener;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.top_attach_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        final TextView tvTop = findViewById(R.id.tv_top);
        tvTop.setText(top ? "取消置顶" : "置顶");

        tvTop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null)
                    clickListener.onClick(view);
                dismiss();
            }
        });
    }
}
