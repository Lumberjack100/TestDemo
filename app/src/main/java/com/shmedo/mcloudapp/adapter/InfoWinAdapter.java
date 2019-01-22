package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.shmedo.mcloudapp.App;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.util.NavigationUtils;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.adapter
 * 文件名:   InfoWinAdapter
 * 创建者:   dpc
 * 创建时间:  2019/1/17 13:46
 * 描述：    TODO
 */
public class InfoWinAdapter implements AMap.InfoWindowAdapter, View.OnClickListener {
    private Context mContext = App.getContext();
    private LatLng latLng;
    private LinearLayout call;
    private LinearLayout navigation;
    private TextView nameTV;
    private String agentName;
    private TextView addrTV;
    private String snippet;

    @Override public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.navigation_LL:  //点击导航
                NavigationUtils.Navigation(latLng);
                break;
            case R.id.call_LL:  //点击打电话
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                Uri uri = Uri.parse("tel:33963627");   //设置要操作界面的具体内容  拨打电话固定格式： tel：
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
        }
    }


    @Override public View getInfoWindow(Marker marker) {
        initData(marker);
        View view = initView();
        return view;
    }
    private void initData(Marker marker) {
        latLng = marker.getPosition();
        snippet = marker.getSnippet();
        agentName = marker.getTitle();
    }
    @NonNull
    private View initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_infowindow, null);
        navigation = (LinearLayout) view.findViewById(R.id.navigation_LL);
        call = (LinearLayout) view.findViewById(R.id.call_LL);
        nameTV = (TextView) view.findViewById(R.id.name);
        addrTV = (TextView) view.findViewById(R.id.addr);

        nameTV.setText(agentName);
        addrTV.setText(String.format(mContext.getString(R.string.agent_addr),snippet));

        navigation.setOnClickListener(this);
        call.setOnClickListener(this);
        return view;
    }
    @Override public View getInfoContents(Marker marker) {
        return null;
    }
}
