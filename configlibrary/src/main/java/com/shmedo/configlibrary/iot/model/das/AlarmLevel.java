package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：     声光报警器级别
 */
public class AlarmLevel {
    private String type;//传感器类型  1:雨量计,2:倾角计,3:主传感器
    private String level1;//无报警
    private String level2;//蓝色一级
    private String level3;//黄色二级
    private String level4;//橙色三级
    private String level5;//红色四级

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel1() {
        return TextUtils.isEmpty(level1) ? "" : level1;
    }

    public void setLevel1(String level1) {
        this.level1 = level1;
    }

    public String getLevel2() {
        return TextUtils.isEmpty(level2) ? "" : level2;
    }

    public void setLevel2(String level2) {
        this.level2 = level2;
    }

    public String getLevel3() {
        return TextUtils.isEmpty(level3) ? "" : level3;
    }

    public void setLevel3(String level3) {
        this.level3 = level3;
    }

    public String getLevel4() {
        return TextUtils.isEmpty(level4) ? "" : level4;
    }

    public void setLevel4(String level4) {
        this.level4 = level4;
    }

    public String getLevel5() {
        return TextUtils.isEmpty(level5) ? "" : level5;
    }

    public void setLevel5(String level5) {
        this.level5 = level5;
    }
}
