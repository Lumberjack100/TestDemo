package com.shmedo.mcloudapp.common.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.shmedo.lib.core.base.model.ProductInfo
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemProductBinding

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： TODO
 *
 *
 */
class ProductAdapter : BaseQuickAdapter<ProductInfo, BaseDataBindingHolder<ItemProductBinding>>(
    R.layout.item_product
) {
    override fun convert(holder: BaseDataBindingHolder<ItemProductBinding>, item: ProductInfo) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            onBindItem(binding, item)
            binding.executePendingBindings()
        }
    }

    private fun onBindItem(
        binding: ItemProductBinding,
        item: ProductInfo
    ) {
        // 设置数据
        binding.m = item
        if (item.isChecked)
            binding.tvName.setTextAppearance(R.style.Product_Tag_Checked_TitleStyle)
        else
            binding.tvName.setTextAppearance(R.style.Product_Tag_UnChecked_TitleStyle)
    }
}