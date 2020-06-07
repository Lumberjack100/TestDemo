package com.shmedo.core.utils;

import android.text.TextUtils;

import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.parser.ParseManager;
import com.shmedo.core.model.GetAllSensorConfigInfo;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    获取数据解析后生成的实体对象工具类
 */
public class ResultParserUtil {
    /**
     * 获取所有配置信息
     *
     * @param result
     * @return
     */
    public static GetAllSensorConfigInfo getAllConfigInfo(String result) {
        CommandResult<GetAllSensorConfigInfo> commandResult = ParseManager.getInstance().parse(result);
        GetAllSensorConfigInfo allInfo = new GetAllSensorConfigInfo();
        if (commandResult.isSuccess()) {
            allInfo = commandResult.getResult();
        }

        return allInfo;
    }


    /**
     * 获取实体对象
     *
     * @param result
     * @return
     */
    public static <T> T getEntityObject(String result) {
        if (TextUtils.isEmpty(result)) {
            Timber.e("待解析的指令结果字符串为空");
            return null;
        }

        CommandResult<T> commandResult = ParseManager.getInstance().parse(result);
        T info = null;
        if (commandResult.isSuccess()) {
            info = (T) commandResult.getResult();
        }

        return info;
    }

}
