package com.shmedo.mcloudapp.ui.page.base.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.data.extensions.getLogItem
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.network.ext.errorCode
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource

import kotlinx.coroutines.launch

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/12
 * 描述　: ViewModel的基类 使用ViewModel类，放弃AndroidViewModel，原因：用处不大 完全有其他方式获取Application上下文
 */
open class BaseRequestViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) :
    ViewModel() {

    protected fun <T> handleError(
        result: MutableResult<DataResult<T>>,
        error: Throwable,
        methodUrl: String
    ): ResponseStatus {
        error.printStackTrace()
        val msg = "$methodUrl  error: ${error.stackTraceToString()}"
        addLogItem(
            sessionId = CommonMMKVOwner.appLogSessionId,
            priority = Log.ERROR,
            data = msg
        )
        //非法的标记或者标记已经过期,清空token
        if (error.errorCode == 11)
            AuthMMKVOwner.token = ""

        val responseStatus = ResponseStatus().apply {
            isSuccess = false
            responseCode = error.errorCode.toString()
            errorMessage = if (error.errorCode == 11) "登录已过期，请重新登录" else error.errorMsg
            source = ResultSource.NETWORK
        }
        result.postValue(DataResult(responseStatus = responseStatus))

        return responseStatus
    }

    fun addLogItem(sessionId: String, priority: Int, data: String) =
        viewModelScope.launch {
            loggerRepositoryImp.insertLogItem(
                getLogItem(
                    sessionId = sessionId,
                    priority = priority,
                    data = data
                )
            )
        }

}