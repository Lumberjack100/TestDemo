package com.shmedo.lib.tcp

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述： TODO
 */


sealed interface TcpManagerResult<T> {

}

class TcpIdleResult<T> : TcpManagerResult<T>

class TcpSuccessResult<T>(val data: T) : TcpManagerResult<T>

class TcpConnectedResult<T> : TcpManagerResult<T>

class TcpConnectClosed<T> : TcpManagerResult<T>

class TcpConnectError<T> : TcpManagerResult<T>