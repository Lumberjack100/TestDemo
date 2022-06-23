package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.shmedo.mcloudapp.util.LocationUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/16/20 <br/>
 * 描述：     定位相关
 */
public class LocationViewModel extends ViewModel {

    public final LocationUtils locationUtils = new LocationUtils();

}
