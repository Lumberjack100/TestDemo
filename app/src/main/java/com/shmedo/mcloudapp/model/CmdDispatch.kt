package com.shmedo.mcloudapp.model

import com.shmedo.core.model.QueryCmdResult

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/4
 *
 * 描述： 通过物联网平台透传指令结果
 *
 *
 */
sealed class CmdDispatch

data class Loading(val isLoading: Boolean) : CmdDispatch()

data class DispatchFailed(val cmdStr: String, val errorMsg: String) : CmdDispatch()
data class DispatchSuccess(val cmdStr: String) : CmdDispatch()
data class CmdResponseResultError(val cmdStr: String, val errorMsg: String) : CmdDispatch()
data class CmdResponseResultTimeOut(val cmdStr: String, val errorMsg: String = "") : CmdDispatch()
data class CmdResponseResultSuccess(val cmdResult: QueryCmdResult) : CmdDispatch()

