package com.shmedo.lib.tcp

/**
 * 创建者：gonghe
 * 创建时间：2024/5/27
 * 描述： TODO
 */


sealed interface TcpManagerResult<T> {

}

class TcpIdleResult<T> : TcpManagerResult<T>

class TcpSuccessDataResult<T>(val data: T) : TcpManagerResult<T>

/**
 * 原始字节数据结果
 */
class TcpSuccessRawDataResult<T>(val data: ByteArray) : TcpManagerResult<T>

class TcpConnectedResult<T> : TcpManagerResult<T>

class TcpConnectClosed<T> : TcpManagerResult<T>

class TcpConnectError<T> : TcpManagerResult<T>
