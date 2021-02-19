package com.shmedo.mcloudapp.projects.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/20 <br/>
 * 描述：     TODO #gh#
 */
public class ProjectInfoEx {


    /**
     * projInfo : {"id":200,"shortName":"2019贵州","name":"2019贵州地灾项目试点","connectString":"server=rm-bp12hs02k6d560678.mysql.rds.aliyuncs.com;user id=root;password=Mon420529;database=mdm_398_db;port=3306","note":"","centerPoint":"{\"lng\":\"105.314831\",\"lat\":\"27.318888\"}","buildTime":"2020-04-09 09:23:55","registerValidTime":"2021-05-09 09:23:55","location":"","warnStatus":2,"levelID":null,"projTypeID":1,"exValues":null,"companyID":209,"imagePath":null,"createUserID":146,"createTime":"2020-04-09 09:23:55","updateUserID":146,"updateTime":"2020-04-29 12:11:49","isValid":true,"regionID":2532}
     * projTypeAlias : 矿山
     * companyID : 209
     * companyName : 贵州项目
     */

    private ProjInfoBean projInfo;
    private String projTypeAlias;
    private int companyID;
    private String companyName;

    public ProjInfoBean getProjInfo() {
        return projInfo;
    }

    public void setProjInfo(ProjInfoBean projInfo) {
        this.projInfo = projInfo;
    }

    public String getProjTypeAlias() {
        return projTypeAlias;
    }

    public void setProjTypeAlias(String projTypeAlias) {
        this.projTypeAlias = projTypeAlias;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public static class ProjInfoBean {
        /**
         * id : 200
         * shortName : 2019贵州
         * name : 2019贵州地灾项目试点
         * connectString : server=rm-bp12hs02k6d560678.mysql.rds.aliyuncs.com;user id=root;password=Mon420529;database=mdm_398_db;port=3306
         * note :
         * centerPoint : {"lng":"105.314831","lat":"27.318888"}
         * buildTime : 2020-04-09 09:23:55
         * registerValidTime : 2021-05-09 09:23:55
         * location :
         * warnStatus : 2
         * levelID : null
         * projTypeID : 1
         * exValues : null
         * companyID : 209
         * imagePath : null
         * createUserID : 146
         * createTime : 2020-04-09 09:23:55
         * updateUserID : 146
         * updateTime : 2020-04-29 12:11:49
         * isValid : true
         * regionID : 2532
         */

        private int id;
        private String shortName;
        private String name;
        private String connectString;
        private String note;
        private String centerPoint;
        private String buildTime;
        private String registerValidTime;
        private String location;
        private int warnStatus;
        private Object levelID;
        private int projTypeID;
        private Object exValues;
        private int companyID;
        private Object imagePath;
        private int createUserID;
        private String createTime;
        private int updateUserID;
        private String updateTime;
        private boolean isValid;
        private int regionID;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getConnectString() {
            return connectString;
        }

        public void setConnectString(String connectString) {
            this.connectString = connectString;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getCenterPoint() {
            return centerPoint;
        }

        public void setCenterPoint(String centerPoint) {
            this.centerPoint = centerPoint;
        }

        public String getBuildTime() {
            return buildTime;
        }

        public void setBuildTime(String buildTime) {
            this.buildTime = buildTime;
        }

        public String getRegisterValidTime() {
            return registerValidTime;
        }

        public void setRegisterValidTime(String registerValidTime) {
            this.registerValidTime = registerValidTime;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public int getWarnStatus() {
            return warnStatus;
        }

        public void setWarnStatus(int warnStatus) {
            this.warnStatus = warnStatus;
        }

        public Object getLevelID() {
            return levelID;
        }

        public void setLevelID(Object levelID) {
            this.levelID = levelID;
        }

        public int getProjTypeID() {
            return projTypeID;
        }

        public void setProjTypeID(int projTypeID) {
            this.projTypeID = projTypeID;
        }

        public Object getExValues() {
            return exValues;
        }

        public void setExValues(Object exValues) {
            this.exValues = exValues;
        }

        public int getCompanyID() {
            return companyID;
        }

        public void setCompanyID(int companyID) {
            this.companyID = companyID;
        }

        public Object getImagePath() {
            return imagePath;
        }

        public void setImagePath(Object imagePath) {
            this.imagePath = imagePath;
        }

        public int getCreateUserID() {
            return createUserID;
        }

        public void setCreateUserID(int createUserID) {
            this.createUserID = createUserID;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public int getUpdateUserID() {
            return updateUserID;
        }

        public void setUpdateUserID(int updateUserID) {
            this.updateUserID = updateUserID;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public boolean isIsValid() {
            return isValid;
        }

        public void setIsValid(boolean isValid) {
            this.isValid = isValid;
        }

        public int getRegionID() {
            return regionID;
        }

        public void setRegionID(int regionID) {
            this.regionID = regionID;
        }
    }
}
