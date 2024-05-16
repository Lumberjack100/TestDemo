package com.shmedo.lib.device.base.iot_cmd.model.lb20s

import com.shmedo.lib.device.base.iot_cmd.model.m20.SensorErrnoBean
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 创建者:   gonghe
 *
 * 创建时间:  1/19/21
 *
 * 描述：   M20设备当前状态
 *
 * {
 *     "000_1": {
 *         "ext_power_volt": 12.20,
 *         "solar_volt": 1.900000,
 *         "battery_volt": 12.400000,
 *         "temp": 0.0,
 *         "humidity": 0.0,
 *         "temp_out": 0.0,
 *         "humidity_out": 0.0,
 *         "4g_signal": -12,
 *         "bd_signal": 99,
 *         "sw_version": "MD-YJGB-119",
 *         "datacenterStatus": "1,1, 0",
 *         "location": "121.604881,31.216244",
 *         "sensor_errno": [
 *             {
 *                 "errno": 0,
 *                 "sensor_id": "000_1"
 *             }
 *         ],
 *         "attach_data": {
 *             "SN": "861039060424641",
 *             "mqtt_id": "258416",
 *             "mqtt_key": "6102203d-2068-420f-92fa-5a0f75dad60d",
 *             "imsi": "460049355910182",
 *             "iccid": "89860473102380042182",
 *             "imei": "861039060424641",
 *             "volumelevel": "3"
 *         }
 *     }
 * }
 *
 *
 */
@JsonClass(generateAdapter = true)
data class LB20SCurrentStateInfo(
    var ext_power_volt: String = IOTConstants.NULL_KEY, //设备输入端电压，单位V
    var temp: String = IOTConstants.NULL_KEY, //设备内部环境温度，单位 ℃
    var humidity: String = IOTConstants.NULL_KEY, //设备内部湿度，单位 RH%
    var temp_out: String = IOTConstants.NULL_KEY, //设备外部环境温度，单位 ℃
    var humidity_out: String = IOTConstants.NULL_KEY, //设备外部环境湿度，单位RH%
    var solar_volt: String = IOTConstants.NULL_KEY,//太阳能板电压,单位V,接了太阳能控制器，就取太阳能控制器给的电压值，没有接就取0值；
    var battery_volt: String = IOTConstants.NULL_KEY,//蓄电池电压，单位V,接了太阳能控制器，就取太阳能控制器给的电压值，没有接就取0值；
    @Json(name = "4g_signal")
    var _4g_signal: Int = 0, //4g信号强度（dBm），dBm=2*CSQ值-113，数值99表示无信号；
    var bd_signal: Int = 0, //北斗信号强度，参数预留，默认99值；数值99表示无信号；
    var sw_version: String = IOTConstants.NULL_KEY, //固件版本
    var location: String = IOTConstants.NULL_KEY, //设备位置-经纬度，经度在前,纬度在后。E表示东经，W表示西经，N表示北纬，S表示南纬。
    var volumelevel: String = IOTConstants.NULL_KEY, //音量强度，分4个等级，无：0、低：1、中：2、高：3
    var datacenterStatus: String = IOTConstants.NULL_KEY, //数据中心转状态显示，[0,1,1]按照左至右的顺序表数据中心1、2、3，0表示未连接，1表示已连接
    var sensor_errno: List<SensorErrnoBean>? = arrayListOf(), //传感器异常信息,
    var attach_data: Map<String, String>? = mapOf()
)
