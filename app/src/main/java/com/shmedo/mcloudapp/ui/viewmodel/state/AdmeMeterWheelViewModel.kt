package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AdmeMeterWheelViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val encoderLineNumber = NonNullObservableField("0")
    val outerDiameter = NonNullObservableField("0")
    val upCorrectionParametersOne = NonNullObservableField("0")
    val upCorrectionParametersTwo = NonNullObservableField("0")
    val upConstant = NonNullObservableField("0")
    val upFilterCoefficient = NonNullObservableField("0")
    val downCorrectionParametersOne = NonNullObservableField("0")
    val downCorrectionParametersTwo = NonNullObservableField("0")
    val downConstant = NonNullObservableField("0")
    val downFilterCoefficient = NonNullObservableField("0")

}