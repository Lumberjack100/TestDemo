package com.shmedo.mcloudapp.maps.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.BottomPopupView;
import com.shmedo.mcloudapp.R;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/4 <br/>
 * 描述：    Poi 点位底部分享弹框
 */
public class PoiSharePopup extends BottomPopupView implements View.OnClickListener {
    public static final int SHARE_DINGDING = 0x0100;
    public static final int SHARE_WX = 0x0101;
    private int selectItem = -1;

    public PoiSharePopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.poi_share_bottom_layout;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        findViewById(R.id.iv_share_dingding).setOnClickListener(this);
        findViewById(R.id.iv_share_wx).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_share_dingding) {
            selectItem = SHARE_DINGDING;
            dismiss();
        } else if (view.getId() == R.id.iv_share_wx) {
            selectItem = SHARE_WX;
            dismiss();
        }
    }

    public int getSelectedShareItem() {
        return selectItem;
    }
}
