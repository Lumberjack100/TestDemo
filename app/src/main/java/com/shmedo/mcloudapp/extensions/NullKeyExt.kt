package com.shmedo.mcloudapp.extensions

import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants

/**
 * 创建者：gonghe
 * 创建时间：2024/5/12
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

inline fun <reified T> T.notNullKey(action: (T) -> Unit) {
    if (this != IOTConstants.NULL_KEY) {
        action.invoke(this)
    }
}

inline fun <reified T> T.notNullKeyEmpty(action: (T) -> Unit) {
    if (this != IOTConstants.NULL_KEY && this != "") {
        action.invoke(this)
    }
}


