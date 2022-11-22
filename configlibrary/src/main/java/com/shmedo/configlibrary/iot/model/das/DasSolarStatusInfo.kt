package com.shmedo.configlibrary.iot.model.das;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/15 <br/>
 * 描述：        DAS 状态页面太阳能控制器状态
 */
public class DasSolarStatusInfo {

    private SolarBean solar;

    public SolarBean getSolar() {
        return solar;
    }

    public void setSolar(SolarBean solar) {
        this.solar = solar;
    }
}
