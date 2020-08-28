package com.shmedo.mcloudapp.projects.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：     项目设备信息
 */
public class ProjectDeviceInfo implements Parcelable {

    /**
     * id : 181
     * token : 180706K
     * name : 180706K
     * deviceTypeID : 5
     * deviceTypeName : DAG
     * gpsLocation :
     * installLocation :
     * deviceStatus : 启用
     * desc :
     * projectID : 380
     * projectName : 米度ADME测试项目
     * online : false
     * registerCompanyID : 1
     * registerCompanyName : 上海米度测控科技有限公司
     * deviceTag : 测试
     * lastActiveTime : 2020-01-02 15:49:02
     * firmwareVersion : null
     * deviceSimList : [{"simID":29,"ccid":"89860445101970723691","simNO":"1440456841134","simIsp":"中国移动","vendor":"浙江企朋"}]
     */

    private int id;
    private String token;
    private String name;
    private int deviceTypeID;
    private String deviceTypeName;
    private String gpsLocation;
    private String installLocation;
    private String deviceStatus;
    private String desc;
    private int projectID;
    private String projectName;
    private boolean online;
    private int registerCompanyID;
    private String registerCompanyName;
    private String deviceTag;
    private String lastActiveTime;
    private Object firmwareVersion;
    private List<DeviceSimListBean> deviceSimList = new ArrayList<>();

    protected ProjectDeviceInfo(Parcel in) {
        id = in.readInt();
        token = in.readString();
        name = in.readString();
        deviceTypeID = in.readInt();
        deviceTypeName = in.readString();
        gpsLocation = in.readString();
        installLocation = in.readString();
        deviceStatus = in.readString();
        desc = in.readString();
        projectID = in.readInt();
        projectName = in.readString();
        online = in.readByte() != 0;
        registerCompanyID = in.readInt();
        registerCompanyName = in.readString();
        deviceTag = in.readString();
        lastActiveTime = in.readString();
        deviceSimList = in.createTypedArrayList(DeviceSimListBean.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(token);
        dest.writeString(name);
        dest.writeInt(deviceTypeID);
        dest.writeString(deviceTypeName);
        dest.writeString(gpsLocation);
        dest.writeString(installLocation);
        dest.writeString(deviceStatus);
        dest.writeString(desc);
        dest.writeInt(projectID);
        dest.writeString(projectName);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeInt(registerCompanyID);
        dest.writeString(registerCompanyName);
        dest.writeString(deviceTag);
        dest.writeString(lastActiveTime);
        dest.writeTypedList(deviceSimList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProjectDeviceInfo> CREATOR = new Creator<ProjectDeviceInfo>() {
        @Override
        public ProjectDeviceInfo createFromParcel(Parcel in) {
            return new ProjectDeviceInfo(in);
        }

        @Override
        public ProjectDeviceInfo[] newArray(int size) {
            return new ProjectDeviceInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceTypeID() {
        return deviceTypeID;
    }

    public void setDeviceTypeID(int deviceTypeID) {
        this.deviceTypeID = deviceTypeID;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getRegisterCompanyID() {
        return registerCompanyID;
    }

    public void setRegisterCompanyID(int registerCompanyID) {
        this.registerCompanyID = registerCompanyID;
    }

    public String getRegisterCompanyName() {
        return registerCompanyName;
    }

    public void setRegisterCompanyName(String registerCompanyName) {
        this.registerCompanyName = registerCompanyName;
    }

    public String getDeviceTag() {
        return deviceTag;
    }

    public void setDeviceTag(String deviceTag) {
        this.deviceTag = deviceTag;
    }

    public String getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(String lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public Object getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(Object firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public List<DeviceSimListBean> getDeviceSimList() {
        return deviceSimList;
    }

    public void setDeviceSimList(List<DeviceSimListBean> deviceSimList) {
        this.deviceSimList = deviceSimList;
    }

    public static class DeviceSimListBean implements Parcelable {
        /**
         * simID : 29
         * ccid : 89860445101970723691
         * simNO : 1440456841134
         * simIsp : 中国移动
         * vendor : 浙江企朋
         */

        private int simID;
        private String ccid;
        private String simNO;
        private String simIsp;
        private String vendor;

        protected DeviceSimListBean(Parcel in) {
            simID = in.readInt();
            ccid = in.readString();
            simNO = in.readString();
            simIsp = in.readString();
            vendor = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(simID);
            dest.writeString(ccid);
            dest.writeString(simNO);
            dest.writeString(simIsp);
            dest.writeString(vendor);
        }

        public static final Creator<DeviceSimListBean> CREATOR = new Creator<DeviceSimListBean>() {
            @Override
            public DeviceSimListBean createFromParcel(Parcel in) {
                return new DeviceSimListBean(in);
            }

            @Override
            public DeviceSimListBean[] newArray(int size) {
                return new DeviceSimListBean[size];
            }
        };

        public int getSimID() {
            return simID;
        }

        public void setSimID(int simID) {
            this.simID = simID;
        }

        public String getCcid() {
            return ccid;
        }

        public void setCcid(String ccid) {
            this.ccid = ccid;
        }

        public String getSimNO() {
            return simNO;
        }

        public void setSimNO(String simNO) {
            this.simNO = simNO;
        }

        public String getSimIsp() {
            return simIsp;
        }

        public void setSimIsp(String simIsp) {
            this.simIsp = simIsp;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }


    }
}
