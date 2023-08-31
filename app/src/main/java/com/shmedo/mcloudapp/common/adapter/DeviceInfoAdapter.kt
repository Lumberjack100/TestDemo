package com.shmedo.mcloudapp.common.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemDeviceInfoBinding

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： TODO
 *
 *
 */
class DeviceInfoAdapter : BaseQuickAdapter<DeviceInfo, BaseDataBindingHolder<ItemDeviceInfoBinding>>(
    R.layout.item_device_info
) {
    override fun convert(holder: BaseDataBindingHolder<ItemDeviceInfoBinding>, item: DeviceInfo) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            onBindItem(binding, item)
            binding.executePendingBindings()
        }
    }

    private fun onBindItem(
        binding: ItemDeviceInfoBinding,
        item: DeviceInfo
    ) {
        // 设置数据
        binding.deviceInfo = item

    }
}