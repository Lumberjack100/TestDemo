package com.shmedo.mcloudapp.common.ext

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/4
 *
 * 描述： TODO
 *
 *
 */


inline fun Fragment.launchAndRepeatWithViewLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
            block()
        }
    }


inline fun Fragment.launchWithViewLifecycle(
    crossinline block: suspend CoroutineScope.() -> Unit
): Job = viewLifecycleOwner.lifecycleScope.launch {
    block()
}
