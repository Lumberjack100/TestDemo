package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DasTerminalParameterViewModel : BaseStateViewModel() {
    val reportMethod = NonNullObservableField("定时定点上报")
    val interval = NonNullObservableField("")
    val reportStartTimeHour = NonNullObservableField("")
    val reportStartTimeMinute = NonNullObservableField("")


    init {
        registerField()
    }

    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "reportMethod" to reportMethod.get(),
            "interval" to interval.get(),
            "reportStartTimeHour" to reportStartTimeHour.get(),
            "reportStartTimeMinute" to reportStartTimeMinute.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            reportMethod,
            interval,
            reportStartTimeHour,
            reportStartTimeMinute
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
                "reportMethod" -> reportMethod.get() != value
                "interval" -> interval.get() != value
                "reportStartTimeHour" -> reportStartTimeHour.get() != value
                "reportStartTimeMinute" -> reportStartTimeMinute.get() != value
                else -> false
            }
        }
    }
}