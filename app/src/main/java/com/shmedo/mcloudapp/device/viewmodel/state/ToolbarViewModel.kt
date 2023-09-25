package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/25 <br/>
 * 描述：     TODO
 */
class ToolbarViewModel: ViewModel()  {
    val toolbarIvActionResId = NonNullObservableField(R.drawable.ic_query_device_data)
    val toolbarIvActionVisible = NonNullObservableField(false)
    val toolbarTvActionText = NonNullObservableField("编辑")
    val toolbarTvActionVisible = NonNullObservableField(false)
}