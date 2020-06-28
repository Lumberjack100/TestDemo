package com.shmedo.mcloudapp.maps.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/28 <br/>
 * 描述：    地图图层信息实体
 */
public class MapLayerInfo {
    private String layerName;//图层名称
    private int layerThumbnail;//图层缩略图
    private MapType mapType;//地图类型
    private boolean isChecked = false;

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public int getLayerThumbnail() {
        return layerThumbnail;
    }

    public void setLayerThumbnail(int layerThumbnail) {
        this.layerThumbnail = layerThumbnail;
    }

    public MapType getMapType() {
        return mapType;
    }

    public void setMapType(MapType mapType) {
        this.mapType = mapType;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
