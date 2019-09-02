package com.shmedo.mcloudapp.entity.ble;


/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.ble
 * 文件名:   RainStationSub
 * 创建者:   dpc
 * 创建时间:  2019/4/17 09:21
 * 描述：      雨量站开关
 */
public class RainStationSub {
    private String rainStation;

    public String getRainStation() {
        return rainStation;
    }

    public void setRainStation(String rainStation) {
        this.rainStation = rainStation;
    }

    @Override
    public String toString() {
        return "RainStationInfo{" +
            "rainStation=" + rainStation +
            '}';
    }
}
