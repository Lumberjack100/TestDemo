package com.shmedo.iot.parser;

import android.text.TextUtils;

import com.shmedo.iot.enums.IOTCommandType;
import com.shmedo.iot.interfaces.IOTResultParser;
import com.shmedo.iot.model.CommonCmdResponseResult;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/2 <br/>
 * 描述：     解析设置指令响应结果
 */
public class CommonCmdResponseResultParser implements IOTResultParser<CommonCmdResponseResult> {

    private static final CommonCmdResponseResultParser ourInstance = new CommonCmdResponseResultParser();

    public static CommonCmdResponseResultParser getInstance() {
        return ourInstance;
    }


    @Override
    public CommonCmdResponseResult parse(String result) {
        CommonCmdResponseResult commonCmdResponseResult = new CommonCmdResponseResult();
        String[] strs = result.split("&");

        for (String ss : strs) {
            if (ss.startsWith("result=")) {
                String value = ss.replace("result=", "");
                if (!TextUtils.isEmpty(value) && value.equals("succ")) {
                    commonCmdResponseResult.setSucceed(true);
                }
                continue;
            }

            if (ss.startsWith("reason=")) {
                String value = ss.replace("reason=", "");
                if (!TextUtils.isEmpty(value)) {
                    commonCmdResponseResult.setReason(value);
                }
            }
        }

        return commonCmdResponseResult;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public IOTCommandType commandType() {
        return null;
    }
}
