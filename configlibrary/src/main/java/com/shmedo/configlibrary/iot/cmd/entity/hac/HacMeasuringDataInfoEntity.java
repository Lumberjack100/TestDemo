package com.shmedo.configlibrary.iot.cmd.entity.hac;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/11 <br/>
 * 描述：     TODO
 */
public class HacMeasuringDataInfoEntity implements Validater {
    private String equipmodel;//电机工作标识 0：停止 1：开始测量 2: 异常
    private String address;//MAC 地址
    private String downwaitetime;//下放等待时间
    private String datatype;//数据解算方式（0:顶部固定法，1底部固定法）
    private String onewaytest;//单向测量 0 :关闭 1:开启
    private String holeno;//孔号
    private String areano;//区号
    private String holedepth;//测斜管孔深
    private String checkreverse;//反转自检


    public void setEquipmodel(String equipmodel) {
        this.equipmodel = equipmodel;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDownwaitetime(String downwaitetime) {
        this.downwaitetime = downwaitetime;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public void setOnewaytest(String onewaytest) {
        this.onewaytest = onewaytest;
    }

    public void setHoleno(String holeno) {
        this.holeno = holeno;
    }

    public void setAreano(String areano) {
        this.areano = areano;
    }

    public void setHoledepth(String holedepth) {
        this.holedepth = holedepth;
    }

    public void setCheckreverse(String checkreverse) {
        this.checkreverse = checkreverse;
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
