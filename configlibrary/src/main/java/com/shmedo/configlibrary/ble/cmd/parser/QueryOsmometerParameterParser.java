package com.shmedo.configlibrary.ble.cmd.parser;

import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.OsmometerStatus;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.QueryOsmometerParameterInfo;

/**
 * Created by adu on 2018/1/19.
 * 解析查询数字式渗压计参数
 */
@Parser
public class QueryOsmometerParameterParser implements ResultParser<QueryOsmometerParameterInfo> {
    @Override
    public QueryOsmometerParameterInfo parse(String result) {
        QueryOsmometerParameterInfo info = new QueryOsmometerParameterInfo();
        String [] strs = result.split(",");
        info.setOsmometerStatus(OsmometerStatus.valueOf(Integer.valueOf(strs[1])));
        info.setOsmometerAddress(strs[2]);
        info.setDepthTrigger(Integer.valueOf(strs[3]));
        info.setDepthCorrect(Double.valueOf(strs[4]));
        info.setTemperatureTrigger(Integer.parseInt(strs[5]));
        info.setTemperatureCorrect(Double.valueOf(strs[6]));
        info.setCordLenght(Double.valueOf(strs[7]));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_OSMOMETER_PARAMETER;
    }
}
