package com.shmedo.mcloudapp.maps.util;

import android.text.TextUtils;

import java.text.DecimalFormat;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/1 <br/>
 * 描述：    经纬度格式转换工具类.
 */
public class CoordinateFormatUtils {

    /**
     * 功能：         度-->度分秒
     *
     * @param d 传入待转化格式的经度或者纬度
     */
    public static String DDtoDMS(double d) {
        String[] array = Double.toString(d).split("[.]");
        String degrees = array[0];//得到度

        double m = Double.parseDouble("0." + array[1]) * 60;
        String[] array1 = Double.toString(m).split("[.]");
        String minutes = array1[0];//得到分

        double s = Double.parseDouble("0." + array1[1]) * 60;
//        String[] array2 = Double.toString(s).split("[.]");
//        String seconds = array2[0];//得到秒
        DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String seconds = decimalFormat.format(s);//format 返回的是字符串

        return degrees + "°" + minutes + "′" + seconds + "″";
    }

    /**
     * 经纬度转化，度分秒转度.
     * 如：108°13′21″= 108.2225
     *
     * @param jwd
     * @return
     */
    public static String DmsTurnDD(String jwd) {
        if (!TextUtils.isEmpty(jwd) && (jwd.contains("°"))) {//如果不为空并且存在度单位
            //计算前进行数据处理
            jwd = jwd.replace("E", "").replace("N", "").replace(":", "").replace("：", "");
            double d = 0, m = 0, s = 0;
            d = Double.parseDouble(jwd.split("°")[0]);
            //不同单位的分，可扩展
            if (jwd.contains("′")) {//正常的′
                m = Double.parseDouble(jwd.split("°")[1].split("′")[0]);
            } else if (jwd.contains("'")) {//特殊的'
                m = Double.parseDouble(jwd.split("°")[1].split("'")[0]);
            }
            //不同单位的秒，可扩展
            if (jwd.contains("″")) {//正常的″
                //有时候没有分 如：112°10.25″
                s = jwd.contains("′") ? Double.parseDouble(jwd.split("′")[1].split("″")[0]) : Double.parseDouble(jwd.split("°")[1].split("″")[0]);
            } else if (jwd.contains("''")) {//特殊的''
                //有时候没有分 如：112°10.25''
                s = jwd.contains("'") ? Double.parseDouble(jwd.split("'")[1].split("''")[0]) : Double.parseDouble(jwd.split("°")[1].split("''")[0]);
            }
            jwd = String.valueOf(d + m / 60 + s / 60 / 60);//计算并转换为string

        }
        return jwd;
    }

    /**
     * 经纬度转换，度分转度度.
     * 如：112°30.4128 = 112.50688
     */
    public static String DmTurnDD(String jwd) {
        jwd = jwd.replace("′", "");
        if (!TextUtils.isEmpty(jwd) && (jwd.contains("°"))) {//如果不为空并且存在度单位
            double d = 0, m = 0;
            d = Double.parseDouble(jwd.split("°")[0]);
            m = Double.parseDouble(jwd.split("°")[1]) / 60;
            jwd = String.valueOf(d + m);
        }
        return jwd;
    }
}