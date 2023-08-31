package com.shmedo.lib.device.base.iot_cmd.entity

import android.text.TextUtils
import com.shmedo.lib.device.base.iot_cmd.enums.ServerNumber
import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  11/18/20 <br></br>
 * 描述：     设置数据中心参数
 */
class DataCenterEntity : Validater {
     var serverNumber: ServerNumber? = null
     var protocol //传输协议;TCP-C/TCP-S/MQTT
            : String? = null
     var datatype //数据协议,由设备类型决定
            : String? = null
     var plattype //平台类型
            : String? = null
     var addr //数据中心地址,addr和port设置为空时，关闭该数据中心
            : String? = null
     var port //数据中心端口
            : String? = null

    /**
     * MQTT 协议特有配置参数
     */
     var deviceid //设备id（MQTT参数）,设备id、key设置为空时，设备通过自动注册的方式获取id、key
            : String? = null
     var devicekey //设备key（MQTT参数）
            : String? = null
     var httpaddr //设备注册HTTP地址，域名或者IP（MQTT参数）,MQTT协议下，设备通过自动注册的方式获取到设备id，key
            : String? = null
     var httpport //设备注册HTTP端口（MQTT参数）
            : String? = null
     var projid //产品ID(MQTT参数)
            : String? = null
     var regcode //厂商设备注册码（MQTT参数）
            : String? = null

    /**
     * SL651 水文协议特有配置参数
     */
     var type_code //测站编码
            : String? = null
     var co_address //中心站地址
            : String? = null
     var password //密码
            : String? = null
     var taddress //遥测站地址
            : String? = null
     var hour_report //小时报开启标识  1:开启 0:关闭
            : String? = null
     var data_link //数据链路维持报  0|[10,40]   0:关闭
            : String? = null
    var valid_day //补发数据有效天数
            : String? = null
    var reissue_time //数据补发间隔
            : String? = null

    override fun validate() {}
    
    override fun toString(): String {
        val stringBuilder = StringBuilder().apply {
            append("centerid=" + serverNumber!!.toInt())
            append("&")
            if (protocol != null) {
                append("protocol=" + if (TextUtils.isEmpty(protocol)) "" else protocol)
                append("&")
            }
            if (!TextUtils.isEmpty(datatype)) {
                append("datatype=$datatype")
                append("&")
            }
            if (plattype != null) {
                append("plattype=$plattype")
                append("&")
            }
            if (addr != null) {
                append("addr=$addr")
                append("&")
            }
            if (port != null) {
                append("port=$port")
                append("&")
            }
            if (deviceid != null) {
                append("deviceid=$deviceid")
                append("&")
            }
            if (devicekey != null) {
                append("devicekey=$devicekey")
                append("&")
            }
            if (httpaddr != null) {
                append("httpaddr=$httpaddr")
                append("&")
            }
            if (httpport != null) {
                append("httpport=$httpport")
                append("&")
            }
            if (projid != null) {
                append("projid=$projid")
                append("&")
            }
            if (regcode != null) {
                append("regcode=$regcode")
                append("&")
            }
            if (type_code != null) {
                append("type_code=$type_code")
                append("&")
            }
            if (co_address != null) {
                append("co_address=$co_address")
                append("&")
            }
            if (password != null) {
                append("password=$password")
                append("&")
            }
            if (taddress != null) {
                append("taddress=$taddress")
                append("&")
            }
            if (hour_report != null) {
                append("hour_report=$hour_report")
                append("&")
            }
            if (data_link != null) {
                append("data_link=$data_link")
                append("&")
            }
            if (valid_day != null) {
                append("valid_day=$valid_day")
                append("&")
            }
            if (reissue_time != null) {
                append("reissue_time=$reissue_time")
                append("&")
            }
            if (toString().endsWith("&")) {
                delete(length - 1, length)
            }
        }
        
        return stringBuilder.toString()
    }
}