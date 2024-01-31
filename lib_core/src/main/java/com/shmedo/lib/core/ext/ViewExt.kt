package com.shmedo.lib.core.ext

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * 创建者：gonghe
 * 创建时间：2024/1/31
 * 描述： TODO
 */

/**
 * 判断是否为空 并传入相关操作
 */
inline fun <reified T> T?.notNull(notNullAction: (T) -> Unit, nullAction: () -> Unit = {}) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction.invoke()
    }
}


// 构建输入框文字变化流
fun EditText.textChangeFlow(): Flow<CharSequence> = callbackFlow {
    // 构建输入框监听器
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        // 在文本变化后向流发射数据
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let { text ->
                trySend(text).isSuccess
            }
        }
    }
    addTextChangedListener(watcher) // 设置输入框监听器
    awaitClose { removeTextChangedListener(watcher) } // 阻塞以保证流一直运行
}