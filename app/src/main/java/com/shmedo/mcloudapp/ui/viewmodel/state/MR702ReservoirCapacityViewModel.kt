package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702ReservoirCapacityViewModel : BaseStateViewModel() {
    companion object {
        const val MAX_POINT_COUNT = 50
    }

    val isOpened = NonNullObservableField(true)

    val pointCount = NonNullObservableField("0")//坐标点数量

    private val xPoints: List<NonNullObservableField<String>> =
        List(MAX_POINT_COUNT) { NonNullObservableField("") }
    private val yPoints: List<NonNullObservableField<String>> =
        List(MAX_POINT_COUNT) { NonNullObservableField("") }

    // 保留旧字段名以兼容现有 DataBinding 与业务逻辑
    val x1: NonNullObservableField<String> get() = xPoints[0]
    val x2: NonNullObservableField<String> get() = xPoints[1]
    val x3: NonNullObservableField<String> get() = xPoints[2]
    val x4: NonNullObservableField<String> get() = xPoints[3]
    val x5: NonNullObservableField<String> get() = xPoints[4]
    val x6: NonNullObservableField<String> get() = xPoints[5]
    val x7: NonNullObservableField<String> get() = xPoints[6]
    val x8: NonNullObservableField<String> get() = xPoints[7]
    val x9: NonNullObservableField<String> get() = xPoints[8]
    val x10: NonNullObservableField<String> get() = xPoints[9]
    val x11: NonNullObservableField<String> get() = xPoints[10]
    val x12: NonNullObservableField<String> get() = xPoints[11]
    val x13: NonNullObservableField<String> get() = xPoints[12]
    val x14: NonNullObservableField<String> get() = xPoints[13]
    val x15: NonNullObservableField<String> get() = xPoints[14]
    val x16: NonNullObservableField<String> get() = xPoints[15]
    val x17: NonNullObservableField<String> get() = xPoints[16]
    val x18: NonNullObservableField<String> get() = xPoints[17]
    val x19: NonNullObservableField<String> get() = xPoints[18]
    val x20: NonNullObservableField<String> get() = xPoints[19]
    val x21: NonNullObservableField<String> get() = xPoints[20]
    val x22: NonNullObservableField<String> get() = xPoints[21]
    val x23: NonNullObservableField<String> get() = xPoints[22]
    val x24: NonNullObservableField<String> get() = xPoints[23]
    val x25: NonNullObservableField<String> get() = xPoints[24]
    val x26: NonNullObservableField<String> get() = xPoints[25]
    val x27: NonNullObservableField<String> get() = xPoints[26]
    val x28: NonNullObservableField<String> get() = xPoints[27]
    val x29: NonNullObservableField<String> get() = xPoints[28]
    val x30: NonNullObservableField<String> get() = xPoints[29]
    val x31: NonNullObservableField<String> get() = xPoints[30]
    val x32: NonNullObservableField<String> get() = xPoints[31]
    val x33: NonNullObservableField<String> get() = xPoints[32]
    val x34: NonNullObservableField<String> get() = xPoints[33]
    val x35: NonNullObservableField<String> get() = xPoints[34]
    val x36: NonNullObservableField<String> get() = xPoints[35]
    val x37: NonNullObservableField<String> get() = xPoints[36]
    val x38: NonNullObservableField<String> get() = xPoints[37]
    val x39: NonNullObservableField<String> get() = xPoints[38]
    val x40: NonNullObservableField<String> get() = xPoints[39]
    val x41: NonNullObservableField<String> get() = xPoints[40]
    val x42: NonNullObservableField<String> get() = xPoints[41]
    val x43: NonNullObservableField<String> get() = xPoints[42]
    val x44: NonNullObservableField<String> get() = xPoints[43]
    val x45: NonNullObservableField<String> get() = xPoints[44]
    val x46: NonNullObservableField<String> get() = xPoints[45]
    val x47: NonNullObservableField<String> get() = xPoints[46]
    val x48: NonNullObservableField<String> get() = xPoints[47]
    val x49: NonNullObservableField<String> get() = xPoints[48]
    val x50: NonNullObservableField<String> get() = xPoints[49]

    val y1: NonNullObservableField<String> get() = yPoints[0]
    val y2: NonNullObservableField<String> get() = yPoints[1]
    val y3: NonNullObservableField<String> get() = yPoints[2]
    val y4: NonNullObservableField<String> get() = yPoints[3]
    val y5: NonNullObservableField<String> get() = yPoints[4]
    val y6: NonNullObservableField<String> get() = yPoints[5]
    val y7: NonNullObservableField<String> get() = yPoints[6]
    val y8: NonNullObservableField<String> get() = yPoints[7]
    val y9: NonNullObservableField<String> get() = yPoints[8]
    val y10: NonNullObservableField<String> get() = yPoints[9]
    val y11: NonNullObservableField<String> get() = yPoints[10]
    val y12: NonNullObservableField<String> get() = yPoints[11]
    val y13: NonNullObservableField<String> get() = yPoints[12]
    val y14: NonNullObservableField<String> get() = yPoints[13]
    val y15: NonNullObservableField<String> get() = yPoints[14]
    val y16: NonNullObservableField<String> get() = yPoints[15]
    val y17: NonNullObservableField<String> get() = yPoints[16]
    val y18: NonNullObservableField<String> get() = yPoints[17]
    val y19: NonNullObservableField<String> get() = yPoints[18]
    val y20: NonNullObservableField<String> get() = yPoints[19]
    val y21: NonNullObservableField<String> get() = yPoints[20]
    val y22: NonNullObservableField<String> get() = yPoints[21]
    val y23: NonNullObservableField<String> get() = yPoints[22]
    val y24: NonNullObservableField<String> get() = yPoints[23]
    val y25: NonNullObservableField<String> get() = yPoints[24]
    val y26: NonNullObservableField<String> get() = yPoints[25]
    val y27: NonNullObservableField<String> get() = yPoints[26]
    val y28: NonNullObservableField<String> get() = yPoints[27]
    val y29: NonNullObservableField<String> get() = yPoints[28]
    val y30: NonNullObservableField<String> get() = yPoints[29]
    val y31: NonNullObservableField<String> get() = yPoints[30]
    val y32: NonNullObservableField<String> get() = yPoints[31]
    val y33: NonNullObservableField<String> get() = yPoints[32]
    val y34: NonNullObservableField<String> get() = yPoints[33]
    val y35: NonNullObservableField<String> get() = yPoints[34]
    val y36: NonNullObservableField<String> get() = yPoints[35]
    val y37: NonNullObservableField<String> get() = yPoints[36]
    val y38: NonNullObservableField<String> get() = yPoints[37]
    val y39: NonNullObservableField<String> get() = yPoints[38]
    val y40: NonNullObservableField<String> get() = yPoints[39]
    val y41: NonNullObservableField<String> get() = yPoints[40]
    val y42: NonNullObservableField<String> get() = yPoints[41]
    val y43: NonNullObservableField<String> get() = yPoints[42]
    val y44: NonNullObservableField<String> get() = yPoints[43]
    val y45: NonNullObservableField<String> get() = yPoints[44]
    val y46: NonNullObservableField<String> get() = yPoints[45]
    val y47: NonNullObservableField<String> get() = yPoints[46]
    val y48: NonNullObservableField<String> get() = yPoints[47]
    val y49: NonNullObservableField<String> get() = yPoints[48]
    val y50: NonNullObservableField<String> get() = yPoints[49]

    fun getXPoint(index: Int): NonNullObservableField<String> {
        require(index in 1..MAX_POINT_COUNT) { "x 坐标索引越界: $index" }
        return xPoints[index - 1]
    }

    fun getYPoint(index: Int): NonNullObservableField<String> {
        require(index in 1..MAX_POINT_COUNT) { "y 坐标索引越界: $index" }
        return yPoints[index - 1]
    }

    fun resetAllPoints() {
        xPoints.forEach { it.set("") }
        yPoints.forEach { it.set("") }
    }

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true

        val stateMap = mutableMapOf<String, Any>(
            "isOpened" to isOpened.get(),
            "pointCount" to pointCount.get()
        )

        xPoints.forEachIndexed { index, observableField ->
            stateMap["x${index + 1}"] = observableField.get()
        }

        yPoints.forEachIndexed { index, observableField ->
            stateMap["y${index + 1}"] = observableField.get()
        }

        initialState = stateMap
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        // 为 isOpened 添加监听器
        isOpened.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateModificationStatus()
            }
        })

        // 为 pointCount 添加监听器
        pointCount.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateModificationStatus()
            }
        })

        xPoints.forEach { observableField ->
            observableField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }

        yPoints.forEach { observableField ->
            observableField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    override fun updateModificationStatus() {
        if (isInitializing) return

        var modified = isOpened.get() != initialState["isOpened"] ||
            pointCount.get() != initialState["pointCount"]

        if (!modified) {
            modified = hasPointChanged(xPoints, "x")
        }

        if (!modified) {
            modified = hasPointChanged(yPoints, "y")
        }

        isDataModified.value = modified
    }

    private fun hasPointChanged(
        points: List<NonNullObservableField<String>>,
        prefix: String
    ): Boolean {
        points.forEachIndexed { index, observableField ->
            val initialValue = initialState["$prefix${index + 1}"] as? String ?: ""
            if (observableField.get() != initialValue) {
                return true
            }
        }
        return false
    }
}
