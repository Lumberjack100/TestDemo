package com.shmedo.lib.core.base.viewmodel

import android.text.InputFilter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/26 <br/>
 * 描述：     TODO
 */
open class BaseStateViewModel : ViewModel() {
    val length15Filter: MutableLiveData<Array<InputFilter>> by lazy {
        MutableLiveData<Array<InputFilter>>(arrayOf<InputFilter>(InputFilter.LengthFilter(15)))
    }


}