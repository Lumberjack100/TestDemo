package com.shmedo.mcloudapp.util;

import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.entity.DaoMaster;
import com.shmedo.mcloudapp.entity.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * 项目名：  eMeasApp
 * 包名：    com.shmedo.emeas.util
 * 文件名:   DaoManager
 * 创建者:   dpc
 * 创建时间:  2017/9/25 18:46
 *
 */

public class DaoManager {

    private volatile static DaoManager daoManager=new DaoManager();
    private DaoMaster mDaoMaster;
    private DaoMaster.DevOpenHelper mHelper;
    private DaoSession mDaoSession;


    public static DaoManager getInstance() {
        return daoManager;
    }


    private DaoMaster getDaoMaster() {
        if (mDaoMaster == null) {
            mHelper = new DaoMaster.DevOpenHelper(MCloudApp.getContext(), "project.db", null);
            mDaoMaster = new DaoMaster(mHelper.getWritableDatabase());
        }
        return mDaoMaster;
    }


    public DaoSession getDaoSession() {
        if (mDaoSession == null) {
            if (mDaoMaster == null) {
                mDaoMaster = getDaoMaster();
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }


    public void setDebug() {
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }


    public void closeConnection() {
        closeHelper();
        closeDaoSession();
    }


    private void closeDaoSession() {
        if (mDaoSession != null) {
            mDaoSession.clear();
            mDaoSession = null;
        }
    }


    private void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }
    }
}
