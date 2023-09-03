package com.shmedo.mcloudapp.device.ext

import androidx.lifecycle.lifecycleScope
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.data.repository.remote.NetDataRepository
import com.shmedo.mcloudapp.device.BaseNetIotCommunicateFragment
import com.shmedo.mcloudapp.device.model.DispatchCmdItem
import com.shmedo.mcloudapp.device.model.DispatchRawCmdParam
import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/3
 *
 * 描述：指令下发扩展
 *
 *
 */


/**
 * 批量透明指令下发(限定同一产品)
 */
fun BaseNetIotCommunicateFragment.batchDispatchRawCmd(
    content: String,
    deviceTokenList: List<String>
) {
    lifecycleScope.launch {
        val rawCmdParam = DispatchRawCmdParam(content, deviceTokenList)
        val jsonParam = MoshiUtil.toJson(rawCmdParam)
        val data: List<DispatchCmdItem> =
            NetDataRepository.instance.batchDispatchRawCmd(jsonParam) { error: Throwable ->
                doDispatchFailed(content, error.errorMsg)
            } ?: return@launch

        msgIDList.clear()
        msgIDList.addAll(data.map { it.msgID })
        doDispatchSuccess(content)
    }
}
