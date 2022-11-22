package com.shmedo.configlibrary.iot.cmd.entity.e40

import com.shmedo.configlibrary.ble.interfaces.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2/26/21 <br></br>
 * 描述：     生成 CORS 参数拼接指令
 */
class E40CORSEntity : Validater {
    var sw //0：表示关闭，1：表示打开
            : String? = null
    var addr //服务器地址
            : String? = null
    var port //服务器端口号
            : String? = null
    var user //用户名
            : String? = null
    var pswd //密码
            : String? = null
    var sta //站点名
            : String? = null

    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && value != "NullKey") {
                    stringBuilder.append(f.name)
                    stringBuilder.append("=")
                    stringBuilder.append(value)
                    stringBuilder.append("&")
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        }
        return stringBuilder.toString()
    }
}