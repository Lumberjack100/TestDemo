package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.lxj.xpopup.impl.PartShadowPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemSubProductSeriesBinding
import com.shmedo.mcloudapp.databinding.ProductSelectionPartShadowPopupNewBinding
import com.shmedo.mcloudapp.model.ProductGroupItem
import com.shmedo.mcloudapp.model.ProductSeriesItem
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import timber.log.Timber

/**
 * 产品选择弹窗
 * 
 * 数据结构说明：
 * - 主列表包含 String（组标题）和 ProductGroupItem（子项组）交替出现
 * - 每个 ProductGroupItem 包含多个 ProductSeriesItem
 * - 选中状态管理：维护当前选中的产品系列和对应的全局位置
 */
class NewProductSelectionPartShadowPopupView(context: Context) : PartShadowPopupView(context) {
    
    private lateinit var binding: ProductSelectionPartShadowPopupNewBinding
    private lateinit var data: List<Any>
    private lateinit var selectListener: OnSelectListener
    
    // 选中状态管理
    private var selectedProductSeriesItem: ProductSeriesItem? = null
    private var selectedGlobalPosition = -1
    
    // 存储所有 ProductSeriesItem 的全局位置映射，用于快速查找和状态管理
    private val productSeriesPositionMap = mutableMapOf<String, Int>()

    fun setData(
        list: List<Any>,
        selectedIndex: Int = 0
    ): NewProductSelectionPartShadowPopupView {
        this.data = list.toList()
        this.selectedGlobalPosition = selectedIndex
        initializeSelectedState()
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

    /**
     * 初始化选中状态
     * 根据传入的全局位置索引找到对应的 ProductSeriesItem
     */
    private fun initializeSelectedState() {
        try {
            var currentPosition = 0
            for (item in data) {
                if (item is ProductGroupItem) {
                    for (seriesItem in item.children) {
                        productSeriesPositionMap[seriesItem.name] = currentPosition
                        if (currentPosition == selectedGlobalPosition) {
                            selectedProductSeriesItem = seriesItem
                            seriesItem.refreshChecked(true)
                        } else {
                            seriesItem.refreshChecked(false)
                        }
                        currentPosition++
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "初始化选中状态时出错")
        }
    }

    private fun initAdapter() {
        binding.rv.linear().setup {
            // 添加类型支持
            addType<String>(R.layout.item_product_group_title)
            addType<ProductGroupItem>(R.layout.item_sub_product_series)
            
            // 处理 ViewHolder 创建和绑定
            onCreate {
                when (itemViewType) {
                    R.layout.item_sub_product_series -> {
                        setupSubProductSeriesViewHolder()
                    }
                }
            }
            
            onBind {
                when (itemViewType) {
                    R.layout.item_sub_product_series -> {
                        bindSubProductSeriesData()
                    }
                }
            }
        }

        // 设置数据
        if (data.isNotEmpty()) {
            binding.rv.models = data
        }
    }

    /**
     * 设置子产品系列的 ViewHolder
     */
    private fun BindingViewHolder.setupSubProductSeriesViewHolder() {
        try {
            val itemBinding = getBinding<ItemSubProductSeriesBinding>()
            itemBinding.rvSub.setup { subRv ->
                // 添加间距装饰器
                subRv.addItemDecoration(
                    MyGridSpacingItemDecoration(
                        3,
                        ConvertUtils.dp2px(10f), false
                    )
                )
                // 设置子项类型
                addType<ProductSeriesItem>(R.layout.item_product)
                // 处理子项点击事件
                R.id.item.onClick {
                    handleProductSeriesItemClick()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "设置子产品系列ViewHolder时出错")
        }
    }

    /**
     * 绑定子产品系列数据
     */
    private fun BindingViewHolder.bindSubProductSeriesData() {
        try {
            val productGroupItem = getModel<ProductGroupItem>()
            val itemBinding = getBinding<ItemSubProductSeriesBinding>()
            itemBinding.rvSub.models = productGroupItem.children
        } catch (e: Exception) {
            Timber.e(e, "绑定子产品系列数据时出错")
        }
    }

    /**
     * 处理产品系列项点击事件
     */
    private fun BindingViewHolder.handleProductSeriesItemClick() {
        try {
            val clickedItem = getModel<ProductSeriesItem>()
            // 如果点击的是已选中的项，直接关闭弹窗
            if (clickedItem == selectedProductSeriesItem) {
                dismiss()
                return
            }
            
            // 更新选中状态
            updateSelectedState(clickedItem)
            
            // 获取全局位置并回调
            val globalPosition = productSeriesPositionMap[clickedItem.name] ?: -1
            selectListener.onSelect(clickedItem, globalPosition)
            
            // 关闭弹窗
            dismiss()
            
        } catch (e: Exception) {
            Timber.e(e, "处理产品系列项点击时出错")
        }
    }

    /**
     * 更新选中状态
     * @param newSelectedItem 新选中的项
     */
    private fun updateSelectedState(newSelectedItem: ProductSeriesItem) {
        try {
            // 清除之前的选中状态
            selectedProductSeriesItem?.refreshChecked(false)
            
            // 设置新的选中状态
            newSelectedItem.refreshChecked(true)
        } catch (e: Exception) {
            Timber.e(e, "更新选中状态时出错")
        }
    }

    interface OnSelectListener {
        fun onSelect(selectionItem: ProductSeriesItem, position: Int)
    }

    override fun getMaxHeight(): Int {
        return (ScreenUtils.getAppScreenHeight() * 0.5f).toInt()
    }
}