package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 截面形状
 */
enum class FlowSectionShape(val value: Int, val label: String) {
    CUSTOM(0, "自定义"),
    RECTANGLE(1, "矩形"),
    TRAPEZOID(2, "梯形"),
    CIRCLE(3, "圆形"),
    U_SHAPE(4, "U型");

    companion object {
        fun fromValue(value: Int): FlowSectionShape =
            entries.firstOrNull { it.value == value } ?: CUSTOM
    }
}

class UDFlowCalculationViewModel : BaseStateViewModel() {
    val selectedShape = NonNullObservableField(FlowSectionShape.CUSTOM)

    val customize = NonNullObservableField("0")//自定义参数
    val cannalwide = NonNullObservableField("0")//截面宽度
    val initdepth = NonNullObservableField("0")//初始水深
    val initheight = NonNullObservableField("0")//初始空高
    val maxdepth = NonNullObservableField("0")//截面深度
    val bottomwide = NonNullObservableField("0")//截面下宽
    val sloperatio = NonNullObservableField("0")//边坡系数
    val diameter = NonNullObservableField("0")//截面直径
    val hcorvalue = NonNullObservableField("0")//空高修正

    val cannalwideTitle = NonNullObservableField("截面宽度（米）")
    val maxDepthTitle = NonNullObservableField("截面高度（米）")
    val diameterTitle = NonNullObservableField("截面直径（米）")

    val showCustomizeParam = NonNullObservableField(true)
    val showCannalwide = NonNullObservableField(false)
    val showMaxDepth = NonNullObservableField(false)
    val showBottomwide = NonNullObservableField(false)
    val showSlopeRatio = NonNullObservableField(false)
    val showDiameter = NonNullObservableField(false)

    init {
        updateFieldLayout(selectedShape.get())
        registerField()
    }

    fun onShapeChanged(shape: FlowSectionShape) {
        if (selectedShape.get() == shape) return
        selectedShape.set(shape)
    }

    private fun updateFieldLayout(shape: FlowSectionShape) {
        when (shape) {
            FlowSectionShape.CUSTOM -> { //自定义
                showCustomizeParam.set(true)
                showCannalwide.set(false)
                showMaxDepth.set(false)
                showBottomwide.set(false)
                showSlopeRatio.set(false)
                showDiameter.set(false)
            }

            FlowSectionShape.RECTANGLE -> { //矩形
                showCustomizeParam.set(false)
                showCannalwide.set(true)
                showMaxDepth.set(true)
                showBottomwide.set(false)
                showSlopeRatio.set(false)
                showDiameter.set(false)

                cannalwideTitle.set("截面宽度（米）")
                maxDepthTitle.set("截面深度（米）")
            }

            FlowSectionShape.TRAPEZOID -> { //梯形
                showCustomizeParam.set(false)
                showCannalwide.set(true)
                showMaxDepth.set(true)
                showBottomwide.set(true)
                showSlopeRatio.set(true)
                showDiameter.set(false)

                cannalwideTitle.set("截面上宽（米）")
                maxDepthTitle.set("截面深度（米）")
            }

            FlowSectionShape.CIRCLE -> { //圆形
                showCustomizeParam.set(false)
                showCannalwide.set(false)
                showMaxDepth.set(false)
                showBottomwide.set(false)
                showSlopeRatio.set(false)
                showDiameter.set(true)

                diameterTitle.set("截面直径（米）")
            }

            FlowSectionShape.U_SHAPE -> {//U 型
                showCustomizeParam.set(false)
                showCannalwide.set(false)
                showMaxDepth.set(true)
                showBottomwide.set(false)
                showSlopeRatio.set(false)
                showDiameter.set(true)

                maxDepthTitle.set("截面深度（米）")
                diameterTitle.set("U型直径（米）")
            }
        }
    }

    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "shape" to selectedShape.get().value,
            "customize" to customize.get(),
            "cannalwide" to cannalwide.get(),
            "initdepth" to initdepth.get(),
            "initheight" to initheight.get(),
            "maxdepth" to maxdepth.get(),
            "bottomwide" to bottomwide.get(),
            "sloperatio" to sloperatio.get(),
            "diameter" to diameter.get(),
            "hcorvalue" to hcorvalue.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        selectedShape.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateFieldLayout(selectedShape.get())
                updateModificationStatus()
            }
        })

        listOf(
            customize,
            cannalwide,
            initdepth,
            initheight,
            maxdepth,
            bottomwide,
            sloperatio,
            diameter,
            hcorvalue
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "shape" -> value != selectedShape.get().value
                "customize" -> value != customize.get()
                "cannalwide" -> value != cannalwide.get()
                "initdepth" -> value != initdepth.get()
                "initheight" -> value != initheight.get()
                "maxdepth" -> value != maxdepth.get()
                "bottomwide" -> value != bottomwide.get()
                "sloperatio" -> value != sloperatio.get()
                "diameter" -> value != diameter.get()
                "hcorvalue" -> value != hcorvalue.get()
                else -> false
            }
        }
    }
}
