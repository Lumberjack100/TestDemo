package com.shmedo.mcloudapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.adapter
 * 文件名:   InfoWinAdapter
 * 创建者:   dpc
 * 创建时间:  2019/1/17 13:46
 * 描述：    TODO
 */
public class InfoWinAdapter implements AMap.InfoWindowAdapter {
    private Context mContext = MCloudApp.getContext();
    private LatLng latLng;
    private TextView deviceTypeName;
    private TextView deviceName;
    private String snippet;
    private String agentName;

    /*@Override public void onClick(View view) {
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
    }*/


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

        deviceTypeName = (TextView) view.findViewById(R.id.deviceTypeName);
        deviceName = (TextView) view.findViewById(R.id.deviceName);

        deviceTypeName.setText(agentName);
        deviceName.setText(String.format(mContext.getString(R.string.agent_addr),snippet));

        //navigation.setOnClickListener(this);
        //call.setOnClickListener(this);
        return view;
    }
    @Override public View getInfoContents(Marker marker) {
        return null;
    }
}
