package com.shmedo.iot.utils;

import com.shmedo.core.utils.Holder;
import com.shmedo.iot.IOTCommandResult;
import com.shmedo.iot.enums.IOTCommandType;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     TODO
 */
public class IOTStringUtil {

    /**
     * 从DAS返回结果中提取指令类型
     *
     * @param result $cmd=reqtime&time=2020-08-31 14:27:21&apikey=80c8b131-80e5-4504-bb67-72e8cf93a542&msgid=63d30270-80f6-4458-bafb-c669585f5e4b
     * @return 从DAS返回结果中提取指令类型
     */
    public static IOTCommandType extractCommandType(String result) {

        String[] strs = result.split("&");
        String cmd = strs[0].replace(IOTCommandResult.COMMAND_HEADER, "").trim();
        Holder<IOTCommandType> cmdTypeHolder = new Holder<>();
        for (IOTCommandType commandType : IOTCommandType.values()) {
            if (commandType.toString().equals(cmd)) {
                cmdTypeHolder.setData(commandType);
            }
        }

        IOTCommandType cmdType = cmdTypeHolder.getData();
        if (cmdType == null)
            throw new IllegalArgumentException("未找到命令:" + result);

        return cmdType;
    }
}
