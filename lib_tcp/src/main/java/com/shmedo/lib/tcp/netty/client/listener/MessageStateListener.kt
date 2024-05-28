package com.shmedo.lib.tcp.netty.client.listener


/**
 * @author Created by LittleGreens on 2019/7/30
 *
 * 发送状态监听
 */
interface MessageStateListener {
    fun isSendSuccess(isSuccess: Boolean)
}