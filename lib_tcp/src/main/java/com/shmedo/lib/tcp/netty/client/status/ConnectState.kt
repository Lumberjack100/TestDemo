package com.shmedo.lib.tcp.netty.client.status

/**
 * @author Created by LittleGreens on 2019/7/30
 */
object ConnectState {
    const val STATUS_CONNECT_SUCCESS: Int = 1

    const val STATUS_CONNECT_CLOSED: Int = 0

    const val STATUS_CONNECT_ERROR: Int = -1
}
