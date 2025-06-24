package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.flexbox.FlexboxLayoutManager
import com.lxj.xpopup.impl.PartShadowPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemSubProductSeriesBinding
import com.shmedo.mcloudapp.databinding.ProductSelectionPartShadowPopupNewBinding
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.ProductGroupItem
import com.shmedo.mcloudapp.model.ProductSeriesItem
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration

/**
 * 产品选择弹窗
 */
class NewProductSelectionPartShadowPopupView(context: Context) : PartShadowPopupView(context) {
    private lateinit var binding: ProductSelectionPartShadowPopupNewBinding
    private lateinit var data: List<Any>

    private var lastSelectedIndex = -1
    private lateinit var selectListener: OnSelectListener


    fun setData(
        list: List<Any>,
        selectedIndex: Int = 0
    ): NewProductSelectionPartShadowPopupView {
        this.data = list.toList()
        this.lastSelectedIndex = selectedIndex
        return this
    }

    fun setSelectListener(selectListener: OnSelectListener): NewProductSelectionPartShadowPopupView {
        this.selectListener = selectListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.product_selection_part_shadow_popup_new
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        initAdapter()
    }

    private fun initAdapter() {
        binding.rv.setup { rv ->
            rv.layoutManager = FlexboxLayoutManager(context)
            addType<String>(R.layout.item_product_group_title)
            addType<ProductGroupItem>(R.layout.item_sub_product_series)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            onCreate {
                when (itemViewType) {
                    R.layout.item_sub_product_series -> {
                        val itemBinding = getBinding<ItemSubProductSeriesBinding>()
                        itemBinding.rvSub.setup { subRv ->
                            subRv.addItemDecoration(
                                MyGridSpacingItemDecoration(
                                    3,
                                    ConvertUtils.dp2px(10f), false
                                )
                            )
                            addType<ProductSeriesItem>(R.layout.item_product)
                            R.id.item.onClick {
                                val selectionItem = getModel<ProductSeriesItem>()
                                //此处处理选中状态的逻辑有问题
                                if (lastSelectedIndex != -1) {
                                    getModel<ProductSeriesItem>(lastSelectedIndex).refreshChecked(false)
                                }
                                selectionItem.refreshChecked(true)
                                selectListener.onSelect(selectionItem, modelPosition)
                                dismiss()
                            }
                        }
                    }

                    else -> {}
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_sub_product_series -> {
                        val productGroupItem = getModel<ProductGroupItem>()
                        val itemBinding = getBinding<ItemSubProductSeriesBinding>()
                        itemBinding.rvSub.models = productGroupItem.children
                    }

                    else -> {

                    }
                }
            }
        }

        if (data.isNotEmpty())
            binding.rv.models = data
    }

    interface OnSelectListener {
        fun onSelect(selectionItem: ProductSeriesItem, position: Int)
    }

    override fun getMaxHeight(): Int {
        return (ScreenUtils.getAppScreenHeight() * 0.5f).toInt()
    }
}