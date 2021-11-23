package com.shmedo.configlibrary.iot.cmd.parser.adme;

import com.blankj.utilcode.util.GsonUtils;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeModuleErrorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/11/23 <br/>
 * 描述：     解析 ADME 模块异常信息参数
 */
public class AdmeModuleErrorInfoParser implements IOTResultParser<AdmeModuleErrorInfo> {
    @Override
    public AdmeModuleErrorInfo parse(String result) {
        AdmeModuleErrorInfo info = null;
        try {
            String[] keyValues = result.split("&");
            HashMap<String, String> keyValueMap = new HashMap<>();

            for (String keyValue : keyValues) {
                String[] strs = keyValue.split("=");
                if (strs.length < 2) {
                    keyValueMap.put(strs[0], "");
                } else {
                    keyValueMap.put(strs[0], strs[1]);
                }
            }
            String status = keyValueMap.get("status");
            info = GsonUtils.fromJson(status, AdmeModuleErrorInfo.class);

            return info;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public IOTCommandType commandType() {
        return IOTCommandType.ADME_MD_GET_MODULE_ERROR_INFO;
    }
}
