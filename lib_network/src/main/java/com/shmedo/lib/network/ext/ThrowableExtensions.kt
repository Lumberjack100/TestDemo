package com.shmedo.lib.network.ext

import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.JsonSyntaxException
import com.shmedo.lib.network.R
import kotlinx.coroutines.TimeoutCancellationException
import rxhttp.wrapper.exception.HttpStatusCodeException
import rxhttp.wrapper.exception.ParseException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * User: ljx
 * Date: 2020-02-07
 * Time: 21:04
 */
fun Throwable.show() {
    ToastUtils.showShort(errorMsg)
}

val Throwable.errorCode: Int
    get() =
        when (this) {
            is HttpStatusCodeException -> this.statusCode //Http状态码异常
            is ParseException -> this.errorCode.toIntOrNull() ?: -1     //业务code异常
            else -> -1
        }

val Throwable.errorMsg: String
    get() {
        return if (this is UnknownHostException) { //网络异常
            if (!NetworkUtils.isConnected())
                StringUtils.getString(R.string.network_error_tip)
            else
                StringUtils.getString(R.string.notify_no_network)
        } else if (this is SocketTimeoutException  //okhttp全局设置超时
            || this is TimeoutException     //rxjava中的timeout方法超时
            || this is TimeoutCancellationException  //协程超时
        ) {
            StringUtils.getString(R.string.network_connect_timeout)
        } else if (this is ConnectException) {
            StringUtils.getString(R.string.network_connect_error)
        } else if (this is HttpStatusCodeException) {               //请求失败异常
            "Http状态码异常 $message"
        } else if (this is JsonSyntaxException) {  //请求成功，但Json语法异常,导致解析失败
            "数据解析失败,请检查数据是否正确"
        } else if (this is ParseException) {       // ParseException异常表明请求成功，但是数据不正确
            this.message ?: errorCode   //msg为空，显示code
        } else {
            message ?: this.toString()
        }
    }

