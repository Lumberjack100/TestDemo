package com.shmedo.mcloudapp.maps.model;

import java.io.Serializable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/23 <br/>
 * 描述：    网络质量
 */
public class NetWorkQuality  implements Serializable {
    private String delay;
    private String downloadSpeed;
    private String uploadSpeed;

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public String getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(String uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }
}
