package com.shmedo.core.commonlib.mmkv

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.model.SensorModel
import com.tencent.mmkv.MMKV

object MmkvCacheUtil {

    /** 获取搜索历史缓存数据 */
    fun getSearchHistoryData(): List<String> {
        val kv = MMKV.defaultMMKV()
        val searchCacheStr = kv.decodeString("device_search_history")
        return if (searchCacheStr.isNullOrEmpty()) arrayListOf()
        else MoshiUtil.fromJson<List<String>>(searchCacheStr) ?: arrayListOf()
    }

    fun setSearchHistoryData(searchResponseStr: String) {
        val kv = MMKV.defaultMMKV()
        kv.encode("device_search_history", searchResponseStr)
    }

    fun getMR702SensorConfigInfo(): List<SensorModel> {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("mr702_sensor_config_info")

        return if (value.isNullOrEmpty()) arrayListOf()
        else MoshiUtil.fromJson<List<SensorModel>>(value) ?: arrayListOf()
    }

    fun setMR702SensorConfigInfo(list: List<SensorModel>?) {
        val kv = MMKV.defaultMMKV()
        list?.let { kv.encode("mr702_sensor_config_info", MoshiUtil.toJson(it)) }
    }
}
