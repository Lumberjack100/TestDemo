package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.DataCenterLinkNumber;
import com.shmedo.core.interfaces.ResultParser;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    解析数据中心链路编号
 */
public class DataCenterLinkNumberParser implements ResultParser<DataCenterLinkNumber> {
    @Override
    public DataCenterLinkNumber parse(String result) {
        return DataCenterLinkNumber.valueOf(result.substring(5));
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_DATA_CENTER_PARAM;
    }
}
