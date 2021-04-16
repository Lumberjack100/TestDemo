package com.shmedo.configlibrary.iot.model.das;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/15 <br/>
 * 描述：       DAS 状态页面温湿度状态
 */
public class DasTemperatureAndHumidityStatusinfo {

    private InthBean inth;
    private OutthBean outth;

    public InthBean getInth() {
        return inth;
    }

    public void setInth(InthBean inth) {
        this.inth = inth;
    }

    public OutthBean getOutth() {
        return outth;
    }

    public void setOutth(OutthBean outth) {
        this.outth = outth;
    }
}
