package com.shmedo.core.cmd.entity;

import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/27 <br/>
 * 描述：    设置 AppKey (米度/北京平台特有)
 */
public class APPKeyEntity implements Validater {
    private int number;//服务器(数据中心)编号，取值1,2,3
    private String appKey;

    public APPKeyEntity(int number, String appKey) {
        this.number = number;
        this.appKey = appKey;
    }

    @Override
    public void validate() {
        if (number != 1 && number != 2 && number != 3)
            throw new DASParameterException("服务器编号错误");

        if (TextUtils.isEmpty(appKey))
            throw new DASParameterException("appKey不能为空");
    }

    @Override
    public String toString() {
        return this.number + "" + this.appKey;
    }
}
