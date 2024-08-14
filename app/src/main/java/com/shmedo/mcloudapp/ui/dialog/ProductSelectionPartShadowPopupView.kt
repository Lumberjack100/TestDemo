package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ScreenUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.flexbox.FlexboxLayoutManager
import com.lxj.xpopup.impl.PartShadowPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ProductSelectionPartShadowPopupBinding
import com.shmedo.mcloudapp.model.SingleSelectionItem

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/30 <br/>
 * 描述：     TODO
 */
class ProductSelectionPartShadowPopupView(context: Context) : PartShadowPopupView(context) {
    private lateinit var binding: ProductSelectionPartShadowPopupBinding
    private lateinit var data: List<SingleSelectionItem>

    private var lastSelectedIndex = -1
    private lateinit var selectListener: OnSelectListener
   

    fun setData(
        list: List<SingleSelectionItem>,
        selectedIndex: Int = 0
    ): ProductSelectionPartShadowPopupView {
        this.data = list.toList()
        this.lastSelectedIndex = selectedIndex
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
            addType<SingleSelectionItem>(R.layout.item_product)
            R.id.item.onClick {
                val selectionItem = getModel<SingleSelectionItem>()
                if (lastSelectedIndex != -1) {
                    getModel<SingleSelectionItem>(lastSelectedIndex).refreshChecked(false)
                }
                selectionItem.refreshChecked(true)
                selectListener.onSelect(selectionItem, modelPosition)
                dismiss()
            }
        }
        if (data.isNotEmpty())
            binding.rv.models = data
    }

    interface OnSelectListener {
        fun onSelect(selectionItem: SingleSelectionItem, position: Int)
    }

    override fun getMaxHeight(): Int {
        return (ScreenUtils.getAppScreenHeight() * 0.5f).toInt()
    }
}