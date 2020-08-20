package com.shmedo.mcloudapp.projects.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/24 <br/>
 * 描述：    项目监测点实体类
 */
public  class OpenedMonitorTypeBean {
    /**
     * id : 66
     * sensorType : 0
     * typeName : 地表位移
     * name : 地表位移
     * displayOrder : 0
     * exValues : 你好12
     * iconImage : medo-icon-sensortype-0
     * allowMultiSensorInPoint : false
     * isValid : true
     */

    private int id;
    private int sensorType;
    private String typeName;
    private String name;
    private int displayOrder;
    private String exValues;
    private String iconImage;
    private boolean allowMultiSensorInPoint;
    private boolean isValid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getExValues() {
        return exValues;
    }

    public void setExValues(String exValues) {
        this.exValues = exValues;
    }

    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    public boolean isAllowMultiSensorInPoint() {
        return allowMultiSensorInPoint;
    }

    public void setAllowMultiSensorInPoint(boolean allowMultiSensorInPoint) {
        this.allowMultiSensorInPoint = allowMultiSensorInPoint;
    }

    public boolean isIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
}