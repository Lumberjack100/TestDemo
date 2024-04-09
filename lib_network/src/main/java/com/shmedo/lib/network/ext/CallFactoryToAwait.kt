package com.shmedo.lib.network.ext

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import rxhttp.newAwait
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.entity.Progress

/**
 * User: ljx
 * Date: 2021/9/18
 * Time: 17:34
 */


/**
 * @param destPath Local storage path
 * @param append is append download
 * @param capacity capacity of the buffer between coroutines
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toDownloadAwait(
    destPath: String,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<String> = toDownloadFlow(destPath, append, capacity, progress).toAwait()

fun CallFactory.toDownloadAwait(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<Uri> = toDownloadFlow(context, uri, append, capacity, progress).toAwait()

fun <T> CallFactory.toDownloadAwait(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Await<T> = toDownloadFlow(osFactory, append, capacity, progress).toAwait()

private fun <T> Flow<T>.toAwait(): Await<T> = newAwait {
    var t: T? = null
    collect { t = it }
    t!!
}
