package com.shmedo.lib.core.base.viewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/7/26 <br/>
 * 描述：     TODO
 */
class NonNullObservableField<T : Any>(value: T, vararg dependencies: Observable) :
    ObservableField<T>(*dependencies) {
    init {
        set(value)
    }

    override fun get(): T = super.get()!!

    // Only allow non-null `value`.
    override fun set(value: T) = super.set(value)
}