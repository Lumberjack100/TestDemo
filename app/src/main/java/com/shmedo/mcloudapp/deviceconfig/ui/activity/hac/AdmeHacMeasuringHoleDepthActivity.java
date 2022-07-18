package com.shmedo.mcloudapp.deviceconfig.ui.activity.hac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac.BleAdmeHacMeasuringHoleDepthFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/11<br/>
 * 描述：     AC10 测孔深参数配置页面
 */
public class AdmeHacMeasuringHoleDepthActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, AdmeHacMeasuringHoleDepthActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("孔深测量");
        //TODO #gh#  打开注释，可以在编辑/浏览模式间切换
//        mTvAction.setVisibility(View.VISIBLE);
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            fragment = BleAdmeHacMeasuringHoleDepthFragment.newInstance();
        }

        return fragment;
    }
}