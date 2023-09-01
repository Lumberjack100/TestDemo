package com.shmedo.lib.core.ext

import androidx.lifecycle.viewModelScope
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * 作者　: hegaojian
 * 时间　: 2020/4/8
 * 描述　:BaseViewModel请求协程封装
 */




/**
 *  调用携程
 * @param block 操作耗时操作任务
 * @param success 成功回调
 * @param error 失败回调 可不给
 */
fun <T> BaseViewModel.launch(
    block: () -> T,
    success: (T) -> Unit,
    error: (Throwable) -> Unit = {}
) {
    viewModelScope.launch {
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                block()
            }
        }.onSuccess {
            success(it)
        }.onFailure {
            error(it)
        }
    }
}
