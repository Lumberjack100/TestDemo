package com.shmedo.core.commonlib.mmkv

import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.model.ProductGroupConfig
import com.shmedo.core.model.SensorModel
import com.shmedo.core.model.UserPermissionInfo
import com.tencent.mmkv.MMKV

object MmkvCacheUtil {

    fun setUserPermissionList(list: List<UserPermissionInfo>?) {
        val kv = MMKV.defaultMMKV()
        list?.let { kv.encode("user_permission_list", MoshiUtil.toJson(it)) }
    }

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

    fun getProductGroupConfig(): List<ProductGroupConfig>  {
        val kv = MMKV.defaultMMKV()
        val value = kv.decodeString("product_group_config")

        return if (value.isNullOrEmpty()) arrayListOf()
        else MoshiUtil.fromJson<List<ProductGroupConfig>>(value) ?: arrayListOf()
    }

    fun setProductGroupConfig(list: List<ProductGroupConfig>?) {
        val kv = MMKV.defaultMMKV()
        list?.let { kv.encode("product_group_config", MoshiUtil.toJson(it)) }
    }

}
