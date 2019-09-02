package com.shmedo.mcloudapp.entity;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   StatusInfoResult
 * 创建者:   dpc
 * 创建时间:  2019/8/7 17:07
 * 描述：    获取设备列表
 */
@Entity
public class StatusInfoResult implements Serializable {
    private static final long serialVersionUID = -2892275648109778268L;
    /**
     * deviceID : 72
     * deviceToken : 150009K
     * deviceName : DAG
     * deviceTypeID : 5
     * deviceTypeName : null
     * securityNO : 1.2345678E7
     * sensorInfo : null
     * voltage : 12.3
     * gprs : 915143
     * signal : 25
     */
    @Unique
    @Id(autoincrement = false)
    @SerializedName("deviceID")
    private Long id;
    private String deviceToken;
    private String deviceName;
    private int deviceTypeID;
    private String deviceTypeName;
    private String securityNO;
    @ToMany(referencedJoinProperty = "ownerId")
    private List<SensorAndCount> sensorInfo;
    private double voltage;
    private int gprs;
    private int signal;
    private boolean local;  //本地添加数据存储的标记
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 2049396227)
    private transient StatusInfoResultDao myDao;

    @Generated(hash = 821511393)
    public StatusInfoResult(Long id, String deviceToken, String deviceName, int deviceTypeID,
            String deviceTypeName, String securityNO, double voltage, int gprs, int signal,
            boolean local) {
        this.id = id;
        this.deviceToken = deviceToken;
        this.deviceName = deviceName;
        this.deviceTypeID = deviceTypeID;
        this.deviceTypeName = deviceTypeName;
        this.securityNO = securityNO;
        this.voltage = voltage;
        this.gprs = gprs;
        this.signal = signal;
        this.local = local;
    }

    @Generated(hash = 273428391)
    public StatusInfoResult() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceToken() { return deviceToken;}

    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken;}

    public String getDeviceName() { return deviceName;}

    public void setDeviceName(String deviceName) { this.deviceName = deviceName;}

    public int getDeviceTypeID() { return deviceTypeID;}

    public void setDeviceTypeID(int deviceTypeID) { this.deviceTypeID = deviceTypeID;}

    public String getDeviceTypeName() { return deviceTypeName;}

    public void setDeviceTypeName(String deviceTypeName) { this.deviceTypeName = deviceTypeName;}

    public String getSecurityNO() { return securityNO;}

    public void setSecurityNO(String securityNO) { this.securityNO = securityNO;}
    @Keep
    public List<SensorAndCount> getSensorInfo() { return sensorInfo;}
    @Keep
    public void setSensorInfo(List<SensorAndCount> sensorInfo) { this.sensorInfo = sensorInfo;}

    public double getVoltage() { return voltage;}

    public void setVoltage(double voltage) { this.voltage = voltage;}

    public int getGprs() { return gprs;}

    public void setGprs(int gprs) { this.gprs = gprs;}

    public int getSignal() { return signal;}

    public void setSignal(int signal) { this.signal = signal;}


    public boolean isLocal() {
        return local;
    }


    public void setLocal(boolean local) {
        this.local = local;
    }


    @Override public String toString() {
        return "StatusInfoResult{" +
            "id=" + id +
            ", deviceToken='" + deviceToken + '\'' +
            ", deviceName='" + deviceName + '\'' +
            ", deviceTypeID=" + deviceTypeID +
            ", deviceTypeName='" + deviceTypeName + '\'' +
            ", securityNO='" + securityNO + '\'' +
            ", sensorInfo=" + sensorInfo +
            ", voltage=" + voltage +
            ", gprs=" + gprs +
            ", signal=" + signal +
            ", local=" + local +
            '}';
    }

    public boolean getLocal() {
        return this.local;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 463006874)
    public synchronized void resetSensorInfo() {
        sensorInfo = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1758165653)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getStatusInfoResultDao() : null;
    }
}
