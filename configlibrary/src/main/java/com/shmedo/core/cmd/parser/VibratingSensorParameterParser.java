package com.shmedo.core.cmd.parser;

import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.VibratingSensorParameterInfo;

/**
 * Created by adu on 2017/12/28.
 * 解析设置振弦式传感器修正参数
 */
@Parser
public class VibratingSensorParameterParser implements ResultParser<VibratingSensorParameterInfo> {
    @Override
    public VibratingSensorParameterInfo parse(String result) {
        VibratingSensorParameterInfo info = new VibratingSensorParameterInfo();
        info.setCollectorModel(CollectorModel.value(result.substring(5,7)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.VIBRATING_SENSOR_PARAMETER;
    }
}
