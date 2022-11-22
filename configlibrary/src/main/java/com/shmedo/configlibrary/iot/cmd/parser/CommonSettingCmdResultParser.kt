package com.shmedo.configlibrary.iot.cmd.parser;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/2 <br/>
 * 描述：     解析通用的设置指令响应结果
 */
public class CommonSettingCmdResultParser implements IOTResultParser<CommonSettingCmdResult> {

    private static final CommonSettingCmdResultParser ourInstance = new CommonSettingCmdResultParser();

    public static CommonSettingCmdResultParser getInstance() {
        return ourInstance;
    }


    @Override
    public CommonSettingCmdResult parse(String result) {
        CommonSettingCmdResult commonSettingCmdResult = new CommonSettingCmdResult();
        String[] strs = result.split("&");

        for (String ss : strs) {
            if (ss.endsWith("=succ")) {
                commonSettingCmdResult.setSucceed(true);
                continue;
            }

            if (ss.endsWith("=fail")) {
                commonSettingCmdResult.setSucceed(false);
                continue;
            }

            if (ss.startsWith("reason=")) {
                String value = ss.replace("reason=", "");
                if (!TextUtils.isEmpty(value)) {
                    commonSettingCmdResult.setReason(value);
                }
            }
        }

        return commonSettingCmdResult;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public IOTCommandType commandType() {
        return null;
    }
}
