package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/12
 * 描述　: ViewModel的基类 使用ViewModel类，放弃AndroidViewModel，原因：用处不大 完全有其他方式获取Application上下文
 */
open class BaseDialogViewModel() : ViewModel() {
    val title = NonNullObservableField("")
    val cancelText = NonNullObservableField("取消")
    val sureText = NonNullObservableField("保存")
    val rvVisible = NonNullObservableField(true)

}