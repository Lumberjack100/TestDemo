package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.CollectorModel;

/**
 * Created by adu on 2017/12/28.
 * 设置振弦式传感器修正参数 (模拟量采集器特有参数)
 */
public class VibratingSensorParameterInfo {
    private CollectorModel collectorModel;  //表示模拟量传感器接入的采集器的通道号取值00~07；
    private String correctParame;   //表示修正参数类型取值'A'，'B'，'C'，'K'，'M'：这些值可以为小数;
    private String parametes;       //为长度不确定的参数

    public CollectorModel getCollectorModel() {
        return collectorModel;
    }

    public void setCollectorModel(CollectorModel collectorModel) {
        this.collectorModel = collectorModel;
    }

    public String getCorrectParame() {
        return correctParame;
    }

    public void setCorrectParame(String correctParame) {
        this.correctParame = correctParame;
    }

    public String getParametes() {
        return parametes;
    }

    public void setParametes(String parametes) {
        this.parametes = parametes;
    }

    @Override
    public String toString() {
        return "VibratingSensorParameterInfo{" +
                "collectorModel=" + collectorModel +
                ", correctParame='" + correctParame + '\'' +
                ", parametes='" + parametes + '\'' +
                '}';
    }
}
