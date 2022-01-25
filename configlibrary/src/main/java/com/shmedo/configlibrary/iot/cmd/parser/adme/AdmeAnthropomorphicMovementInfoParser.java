package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeAnthropomorphicMovementInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/1/25 <br/>
 * 描述：     TODO
 */
public class AdmeAnthropomorphicMovementInfoParser implements IOTResultParser<AdmeAnthropomorphicMovementInfo> {
    @Override
    public AdmeAnthropomorphicMovementInfo parse(String result) {
        AdmeAnthropomorphicMovementInfo info = new AdmeAnthropomorphicMovementInfo();
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
            info.setMode(TextUtils.isEmpty(keyValueMap.get("mode")) ? "" : keyValueMap.get("mode"));

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
        return IOTCommandType.ADME_MD_GET_ANTHROPOMORPHIC_MOVEMENT_MODE;
    }
}
