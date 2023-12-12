package com.shmedo.mcloudapp.device.ui

import android.content.Context
import android.graphics.Color
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.flexbox.FlexboxLayoutManager
import com.lxj.xpopup.impl.PartShadowPopupView
import com.shmedo.lib.core.base.model.ProductInfo
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemProductBinding
import com.shmedo.mcloudapp.databinding.ProductSelectionPartShadowPopupBinding

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/30 <br/>
 * 描述：     TODO
 */
class ProductSelectionPartShadowPopupView(context: Context) : PartShadowPopupView(context) {
    private lateinit var binding: ProductSelectionPartShadowPopupBinding
    private lateinit var data: List<ProductInfo>

    private var selectedIndex: Int = 0
    private lateinit var selectListener: OnSelectListener
    private val activeColor: Int = ColorUtils.getColor(R.color.white)
    private val normalColor: Int = Color.parseColor("#65A2CD")

    fun setData(
        list: List<ProductInfo>,
        selectedIndex: Int = 0
    ): ProductSelectionPartShadowPopupView {
        this.data = list.toList()
        this.selectedIndex = selectedIndex
        return this
    }

    fun setSelectListener(selectListener: OnSelectListener): ProductSelectionPartShadowPopupView {
        this.selectListener = selectListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.product_selection_part_shadow_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        initAdapter()
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter() {
        binding.rv.setup { rv ->
            rv.layoutManager = FlexboxLayoutManager(context)
            addType<ProductInfo>(R.layout.item_product)
            onBind {
                if (selectedIndex == modelPosition) {
                    getBinding<ItemProductBinding>().item.setBackgroundResource(R.drawable.bg_mr702_port_tab_checked)
                    getBinding<ItemProductBinding>().tabText.setTextColor(activeColor)
                } else {
                    getBinding<ItemProductBinding>().item.setBackgroundResource(R.drawable.bg_mr702_port_tab_normal)
                    getBinding<ItemProductBinding>().tabText.setTextColor(normalColor)
                }
            }
            R.id.item.onClick {
                val productInfo = getModel<ProductInfo>()
                selectListener.onSelect(productInfo, modelPosition)
                dismiss()
            }
        }
        if (data.isNotEmpty())
            binding.rv.models = data
    }

    interface OnSelectListener {
        fun onSelect(productInfo: ProductInfo, position: Int)
    }

    override fun getMaxHeight(): Int {
        return (ScreenUtils.getAppScreenHeight() * 0.5f).toInt()
    }
}