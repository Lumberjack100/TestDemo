package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * GT600 姿态监测页面 ViewModel
 * 
 * @author: AI Agent
 * @time: 2026/1/5
 * @desc: 管理姿态监测页面的所有状态数据
 */
class Gt600LocationInfoViewModel : ViewModel() {
    // 基准站状态（true=正常/绿色，false=异常/红色）
    val isBaseStationNormal = NonNullObservableField(false)
    val baseStationStatusText = NonNullObservableField("异常")
    
    // 移动站状态
    val isRoverStationNormal = NonNullObservableField(false)
    val roverStationStatusText = NonNullObservableField("异常")
    
    // 倾角计状态
    val isInclinometerNormal = NonNullObservableField(false)
    val inclinometerStatusText = NonNullObservableField("异常")
    
    // 时间
    val displayTime = NonNullObservableField("--")
    
    // 位置
    val displayLocation = NonNullObservableField("--")
    
    // 方位角
    val heading = NonNullObservableField("--")
    
    // 仰角 (x_ang)
    val elevation = NonNullObservableField("--")
    
    // 经纬度原始值（用于地图打点）
    val latitude = NonNullObservableField(0.0)
    val longitude = NonNullObservableField(0.0)
}

