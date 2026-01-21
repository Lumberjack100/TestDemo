package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.ml101

import android.os.Bundle
import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：ML101/MS101 电台信息/卫通信息页面（占位实现）
 *
 * 根据产品类型显示不同内容：
 * - ML101 (C_L_1): 显示 LoRa 电台相关信息
 * - MS101 (C_S_2): 显示卫星通信相关信息
 *
 * TODO: 待实现具体的数据查询和展示逻辑
 */
class ML101RadioInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    private val isML101: Boolean
        get() = productType == ProductType.C_L_1

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = if (isML101) "电台信息" else "卫通信息"
    }

    override fun queryStatusInfo() {
        // TODO: 实现电台/卫通信息查询逻辑
        binding.refreshLayout.showContent()

        val groupList = mutableListOf<Any>()
        val groupTitle = if (isML101) "电台信息" else "卫通信息"
        groupList.add(DeviceStatusInfoGroupItem(groupTitle))
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "待实现",
            value = "待开发",
            isBottomItem = true
        )
        binding.recyclerview.models = groupList
    }

    override fun <T> initStatusInfo(content: T) {
        // TODO: 实现状态信息初始化逻辑
    }
}
