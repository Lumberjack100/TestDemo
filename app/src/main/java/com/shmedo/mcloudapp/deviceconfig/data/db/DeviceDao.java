package com.shmedo.mcloudapp.deviceconfig.data.db;

import android.text.TextUtils;

import com.shmedo.mcloudapp.deviceconfig.model.DeviceApiKey;
import com.shmedo.mcloudapp.entity.DeviceApiKeyDao;
import com.shmedo.mcloudapp.util.DaoManager;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/25/20 <br/>
 * 描述：     TODO
 */
public class DeviceDao {
    private static final DeviceDao instance = new DeviceDao();

    public static DeviceDao getInstance() {
        return instance;
    }

    /**
     * 根据设备 SN号 返回设备的 apiKey
     *
     * @param sn
     * @return
     */
    public String getCachedDeviceApiKeyBySn(String sn) {
        DeviceApiKey deviceApiKey = DaoManager.getInstance().getDaoSession().getDeviceApiKeyDao()
                .queryBuilder()
                .where(DeviceApiKeyDao.Properties.DeviceToken.eq(sn))
                .unique();

        if (deviceApiKey == null || TextUtils.isEmpty(deviceApiKey.getApiKey()))
            return null;

        return deviceApiKey.getApiKey();
    }

    public void cacheDeviceApiKey(DeviceApiKey deviceApiKey) {
        if (deviceApiKey == null)
            return;

        DaoManager.getInstance().getDaoSession().getDeviceApiKeyDao().insertOrReplaceInTx(deviceApiKey);
    }

    public void deleteCachedDeviceApiKeyBySn(String sn) {
        DeviceApiKey deviceApiKey = DaoManager.getInstance().getDaoSession().getDeviceApiKeyDao()
                .queryBuilder()
                .where(DeviceApiKeyDao.Properties.DeviceToken.eq(sn))
                .unique();

        if (deviceApiKey != null) {
            DaoManager.getInstance().getDaoSession().getDeviceApiKeyDao().delete(deviceApiKey);
        }
    }
}
