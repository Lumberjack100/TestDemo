package com.shmedo.mcloudapp.deviceconfig.model.params;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/23 <br/>
 * 描述：     查询指令执行结果参数
 */
public class QueryCmdResultParam {
    private List<String> msgIDList;//指令下发后返回的MsgID列表

    public List<String> getMsgIDList() {
        return msgIDList;
    }

    public void setMsgIDList(List<String> msgIDList) {
        this.msgIDList = msgIDList;
    }
}
