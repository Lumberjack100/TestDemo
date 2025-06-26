package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702ReservoirCapacityViewModel : BaseStateViewModel() {
    val isOpened = NonNullObservableField(true)

    val pointCount = NonNullObservableField("0")//坐标点数量
    
    // X坐标值数组，1-50
    val x1 = NonNullObservableField("")
    val x2 = NonNullObservableField("")
    val x3 = NonNullObservableField("")
    val x4 = NonNullObservableField("")
    val x5 = NonNullObservableField("")
    val x6 = NonNullObservableField("")
    val x7 = NonNullObservableField("")
    val x8 = NonNullObservableField("")
    val x9 = NonNullObservableField("")
    val x10 = NonNullObservableField("")
    val x11 = NonNullObservableField("")
    val x12 = NonNullObservableField("")
    val x13 = NonNullObservableField("")
    val x14 = NonNullObservableField("")
    val x15 = NonNullObservableField("")
    val x16 = NonNullObservableField("")
    val x17 = NonNullObservableField("")
    val x18 = NonNullObservableField("")
    val x19 = NonNullObservableField("")
    val x20 = NonNullObservableField("")
    val x21 = NonNullObservableField("")
    val x22 = NonNullObservableField("")
    val x23 = NonNullObservableField("")
    val x24 = NonNullObservableField("")
    val x25 = NonNullObservableField("")
    val x26 = NonNullObservableField("")
    val x27 = NonNullObservableField("")
    val x28 = NonNullObservableField("")
    val x29 = NonNullObservableField("")
    val x30 = NonNullObservableField("")
    val x31 = NonNullObservableField("")
    val x32 = NonNullObservableField("")
    val x33 = NonNullObservableField("")
    val x34 = NonNullObservableField("")
    val x35 = NonNullObservableField("")
    val x36 = NonNullObservableField("")
    val x37 = NonNullObservableField("")
    val x38 = NonNullObservableField("")
    val x39 = NonNullObservableField("")
    val x40 = NonNullObservableField("")
    val x41 = NonNullObservableField("")
    val x42 = NonNullObservableField("")
    val x43 = NonNullObservableField("")
    val x44 = NonNullObservableField("")
    val x45 = NonNullObservableField("")
    val x46 = NonNullObservableField("")
    val x47 = NonNullObservableField("")
    val x48 = NonNullObservableField("")
    val x49 = NonNullObservableField("")
    val x50 = NonNullObservableField("")

    // Y坐标值数组，1-50
    val y1 = NonNullObservableField("")
    val y2 = NonNullObservableField("")
    val y3 = NonNullObservableField("")
    val y4 = NonNullObservableField("")
    val y5 = NonNullObservableField("")
    val y6 = NonNullObservableField("")
    val y7 = NonNullObservableField("")
    val y8 = NonNullObservableField("")
    val y9 = NonNullObservableField("")
    val y10 = NonNullObservableField("")
    val y11 = NonNullObservableField("")
    val y12 = NonNullObservableField("")
    val y13 = NonNullObservableField("")
    val y14 = NonNullObservableField("")
    val y15 = NonNullObservableField("")
    val y16 = NonNullObservableField("")
    val y17 = NonNullObservableField("")
    val y18 = NonNullObservableField("")
    val y19 = NonNullObservableField("")
    val y20 = NonNullObservableField("")
    val y21 = NonNullObservableField("")
    val y22 = NonNullObservableField("")
    val y23 = NonNullObservableField("")
    val y24 = NonNullObservableField("")
    val y25 = NonNullObservableField("")
    val y26 = NonNullObservableField("")
    val y27 = NonNullObservableField("")
    val y28 = NonNullObservableField("")
    val y29 = NonNullObservableField("")
    val y30 = NonNullObservableField("")
    val y31 = NonNullObservableField("")
    val y32 = NonNullObservableField("")
    val y33 = NonNullObservableField("")
    val y34 = NonNullObservableField("")
    val y35 = NonNullObservableField("")
    val y36 = NonNullObservableField("")
    val y37 = NonNullObservableField("")
    val y38 = NonNullObservableField("")
    val y39 = NonNullObservableField("")
    val y40 = NonNullObservableField("")
    val y41 = NonNullObservableField("")
    val y42 = NonNullObservableField("")
    val y43 = NonNullObservableField("")
    val y44 = NonNullObservableField("")
    val y45 = NonNullObservableField("")
    val y46 = NonNullObservableField("")
    val y47 = NonNullObservableField("")
    val y48 = NonNullObservableField("")
    val y49 = NonNullObservableField("")
    val y50 = NonNullObservableField("")

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        
        // 创建一个包含所有字段的初始状态映射
        val stateMap = mutableMapOf<String, Any>(
            "isOpened" to isOpened.get(),
            "pointCount" to pointCount.get()
        )
        
        // 添加所有 X 坐标值
        for (i in 1..50) {
            val field = this::class.java.getDeclaredField("x$i")
            field.isAccessible = true
            val value = (field.get(this) as NonNullObservableField<String>).get()
            stateMap["x$i"] = value
        }
        
        // 添加所有 Y 坐标值
        for (i in 1..50) {
            val field = this::class.java.getDeclaredField("y$i")
            field.isAccessible = true
            val value = (field.get(this) as NonNullObservableField<String>).get()
            stateMap["y$i"] = value
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
        
        // 为所有 X 坐标值添加监听器
        for (i in 1..50) {
            try {
                val field = this::class.java.getDeclaredField("x$i")
                field.isAccessible = true
                val observableField = field.get(this) as NonNullObservableField<String>
                observableField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                        updateModificationStatus()
                    }
                })
            } catch (e: Exception) {
                // 处理异常
            }
        }
        
        // 为所有 Y 坐标值添加监听器
        for (i in 1..50) {
            try {
                val field = this::class.java.getDeclaredField("y$i")
                field.isAccessible = true
                val observableField = field.get(this) as NonNullObservableField<String>
                observableField.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                        updateModificationStatus()
                    }
                })
            } catch (e: Exception) {
                // 处理异常
            }
        }
    }

    override fun updateModificationStatus() {
        if (isInitializing) return
        
        // 检查 isOpened 和 pointCount 是否已修改
        var modified = isOpened.get() != initialState["isOpened"] ||
                      pointCount.get() != initialState["pointCount"]
        
        // 如果尚未检测到修改，检查所有 X 坐标值
        if (!modified) {
            for (i in 1..50) {
                try {
                    val field = this::class.java.getDeclaredField("x$i")
                    field.isAccessible = true
                    val observableField = field.get(this) as NonNullObservableField<String>
                    if (observableField.get() != initialState["x$i"]) {
                        modified = true
                        break
                    }
                } catch (e: Exception) {
                    // 处理异常
                }
            }
        }
        
        // 如果尚未检测到修改，检查所有 Y 坐标值
        if (!modified) {
            for (i in 1..50) {
                try {
                    val field = this::class.java.getDeclaredField("y$i")
                    field.isAccessible = true
                    val observableField = field.get(this) as NonNullObservableField<String>
                    if (observableField.get() != initialState["y$i"]) {
                        modified = true
                        break
                    }
                } catch (e: Exception) {
                    // 处理异常
                }
            }
        }
        
        isDataModified.value = modified
    }
}