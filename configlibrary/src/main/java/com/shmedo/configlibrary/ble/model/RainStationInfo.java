package com.shmedo.configlibrary.ble.model;


import com.shmedo.configlibrary.ble.enums.RainStation;

/**
 * Created by adu on 2017/12/15.
 * 雨量站的实体类
 */
public class RainStationInfo {
    private RainStation rainStation;

    public RainStation getRainStation() {
        return rainStation;
    }

    public void setRainStation(RainStation rainStation) {
        this.rainStation = rainStation;
    }

    @Override
    public String toString() {
        return "RainStationInfo{" +
                "rainStation=" + rainStation +
                '}';
    }
}
