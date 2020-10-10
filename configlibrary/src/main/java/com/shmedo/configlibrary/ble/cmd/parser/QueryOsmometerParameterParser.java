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
        info.setDepthTrigger(strs[3]);
        info.setDepthCorrect(strs[4]);
        info.setTemperatureTrigger(strs[5]);
        info.setTemperatureCorrect(strs[6]);
        info.setCordLenght(strs[7]);
        info.setInstallHeight(strs[8]);
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
