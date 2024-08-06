package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
class AdmeHacAlarmSettingViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val firstLevelXAxisMin = NonNullObservableField("0")//X轴相对位移量最小值
    val firstLevelXAxisMax = NonNullObservableField("0")//X轴相对位移量最大值
    val firstLevelYAxisMin = NonNullObservableField("0")//Y轴相对位移量最小值
    val firstLevelYAxisMax = NonNullObservableField("0")//Y轴相对位移量最大值

    val secondLevelXAxisMin = NonNullObservableField("0")//X轴绝对位移量最小值
    val secondLevelXAxisMax = NonNullObservableField("0")//X轴绝对位移量最大值
    val secondLevelYAxisMin = NonNullObservableField("0")//Y轴绝对位移量最小值
    val secondLevelYAxisMax = NonNullObservableField("0")//Y轴绝对位移量最大值

    val thirdLevelXAxisMin = NonNullObservableField("0")//X轴绝对位移量最小值
    val thirdLevelXAxisMax = NonNullObservableField("0")//X轴绝对位移量最大值
    val thirdLevelYAxisMin = NonNullObservableField("0")//Y轴绝对位移量最小值
    val thirdLevelYAxisMax = NonNullObservableField("0")//Y轴绝对位移量最大值
}