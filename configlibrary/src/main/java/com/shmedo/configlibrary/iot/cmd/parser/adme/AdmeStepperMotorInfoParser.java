package com.shmedo.configlibrary.iot.cmd.parser.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;

import java.util.HashMap;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：    解析ADME步进电机参数
 */
public class AdmeStepperMotorInfoParser implements IOTResultParser<AdmeStepperMotorInfo> {
    @Override
    public AdmeStepperMotorInfo parse(String result) {
        AdmeStepperMotorInfo info = new AdmeStepperMotorInfo();
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
            info.setPosnegtest(TextUtils.isEmpty(keyValueMap.get("posnegtest")) ? "" : keyValueMap.get("posnegtest"));
            info.setAbsprsion(TextUtils.isEmpty(keyValueMap.get("absprsion")) ? "" : keyValueMap.get("absprsion"));
            info.setMovspeed(TextUtils.isEmpty(keyValueMap.get("movspeed")) ? "" : keyValueMap.get("movspeed"));
            info.setMovesm(TextUtils.isEmpty(keyValueMap.get("movesm")) ? "" : keyValueMap.get("movesm"));

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
        return IOTCommandType.ADME_MD_GET_STEPPER_MOTOR_PARAMETERS;
    }
}
