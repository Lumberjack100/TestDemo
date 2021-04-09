package com.shmedo.configlibrary.iot.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/20/20 <br/>
 * 描述：     米度物联网协议定义下的监测传感器类型工具类
 */
public class IOTSensorUtil {
    private static IOTSensorUtil instance = null;

    private Map<String, String> typeNameMap = new HashMap<>();
    private Map<String, String> errorNoMap = new HashMap<>();


    public static IOTSensorUtil getInstance() {
        if (instance == null) {
            instance = new IOTSensorUtil();
            instance.initTypeData();
            instance.initErrorNOData();
        }

        return instance;
    }

    /**
     * 初始化传感器类型名称 Map
     */
    private void initTypeData() {
        typeNameMap.clear();
        typeNameMap.put("000", "设备状态 ");
//        typeNameMap.put("101", "地表裂缝计");
        typeNameMap.put("102", "裂缝计");
        typeNameMap.put("103", "加速度计");
        typeNameMap.put("104", "泥位计");
        typeNameMap.put("105", "预警喇叭");
        typeNameMap.put("201", "雨量计");
        typeNameMap.put("202", "土壤含水率");
//        typeNameMap.put("203", "裂缝计");
        typeNameMap.put("204", "GNSS结果数据");
        typeNameMap.put("205", "GNSS原始数据");
        typeNameMap.put("206", "倾角计");
        typeNameMap.put("207", "水压力计");
        typeNameMap.put("208", "次声");
        typeNameMap.put("209", "土压力计");
        typeNameMap.put("210", "振弦式应力计");
        typeNameMap.put("211", "地表位移");
        typeNameMap.put("212", "深部位移");
        typeNameMap.put("213", "倾斜仪");
        typeNameMap.put("214", "水位计");
        typeNameMap.put("215", "多点位移");
        typeNameMap.put("216", "渗压计");
        typeNameMap.put("217", "流速仪");
        typeNameMap.put("218", "气温");
        typeNameMap.put("219", "TDR变形计");
        typeNameMap.put("220", "泉水流量");
        typeNameMap.put("221", "沉降仪");
        typeNameMap.put("222", "钻孔测斜仪");
        typeNameMap.put("10000", "泵站数据");
        typeNameMap.put("10001", "图片");
        typeNameMap.put("10002", "干滩");
        typeNameMap.put("10003", "浊度");
        typeNameMap.put("10004", "湿度");
        typeNameMap.put("10005", "轴力计");
        typeNameMap.put("10006", "PH");
        typeNameMap.put("10008", "崩滑仪");
        typeNameMap.put("20001", "设备日志");
        typeNameMap.put("20002", "设备报警");
    }

    /**
     * 初始化错误码信息 Map
     */
    private void initErrorNOData() {
        errorNoMap.clear();
        errorNoMap.put("0", "正常");
        errorNoMap.put("-1", "供电异常");
        errorNoMap.put("-2", "数据异常");
        errorNoMap.put("-3", "未采集到数据");
        errorNoMap.put("-4", "未接入");
        errorNoMap.put("-5", "短路");
        errorNoMap.put("-6", "接触不良");
    }

    /**
     * 根据传感器的编号返回对应的名称
     *
     * @param typeCode
     * @return
     */
    public String getSensorNameByTypeCode(String typeCode) {
        if (typeCode.contains("_")) {
            String[] strs = typeCode.split("_");
            typeCode = strs[0];
        }
        String name = typeNameMap.get(typeCode);
        if (TextUtils.isEmpty(name)) {
            name = "未知类型";
        }

        return name;
    }

    /**
     * 根据传感器的名称返回对应的编号
     * @param sensorName
     * @return
     */
    public String getSensorTypeCodeByName(String sensorName) {
        if (TextUtils.isEmpty(sensorName))
            return null;

        String code = "";
        for (Map.Entry<String, String> entry : typeNameMap.entrySet()) {
            if (entry.getValue().equals(sensorName)) {
                code = entry.getKey();
                break;
            }
        }

        return code;
    }

    /**
     * 根据传感器错误码返回对应的错误信息
     *
     * @param errorNo
     * @return
     */
    public String getErrorMessageByNo(String errorNo) {
        String msg = errorNoMap.get(errorNo);
        if (TextUtils.isEmpty(msg)) {
            msg = "未知错误";
        }

        return msg;
    }

}
