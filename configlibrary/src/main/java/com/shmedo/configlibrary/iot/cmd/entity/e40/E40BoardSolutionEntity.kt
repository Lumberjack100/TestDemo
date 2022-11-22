package com.shmedo.configlibrary.iot.cmd.entity.e40;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：     生成板卡解算参数拼接指令
 */
public class E40BoardSolutionEntity implements Validater {
    private String inittime;//初始化时间
    private String calcgap;//解算时间
    private String smoothlevel;//平滑等级
    private String reinit;//重新初始化
    private String rtkdynamicmode;//RTK动态模式
    private String corrval;//形变修正值

    public void setInittime(String inittime) {
        this.inittime = inittime;
    }

    public void setCalcgap(String calcgap) {
        this.calcgap = calcgap;
    }

    public void setSmoothlevel(String smoothlevel) {
        this.smoothlevel = smoothlevel;
    }

    public void setReinit(String reinit) {
        this.reinit = reinit;
    }

    public void setRtkdynamicmode(String rtkdynamicmode) {
        this.rtkdynamicmode = rtkdynamicmode;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !value.equals("NullKey")) {
                    stringBuilder.append(f.getName());
                    stringBuilder.append("=");
                    stringBuilder.append(value);
                    stringBuilder.append("&");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
