package com.shmedo.lib.core.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.core.ext.getLogItem
import kotlinx.coroutines.launch

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/12
 * 描述　: ViewModel的基类 使用ViewModel类，放弃AndroidViewModel，原因：用处不大 完全有其他方式获取Application上下文
 */
open class BaseRequestViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) : ViewModel() {

    fun addLogItem(sessionId: String, priority: Int, data: String) = viewModelScope.launch {
        loggerRepositoryImp.insertLog(
            getLogItem(
                sessionId = sessionId,
                priority = priority,
                data = data
            )
        )
    }

}