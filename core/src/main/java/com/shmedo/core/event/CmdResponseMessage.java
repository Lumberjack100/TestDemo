package com.shmedo.core.event;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/10 <br/>
 * 描述：     指令应答消息
 */
public class CmdResponseMessage extends MessageEvent {
    private String result;

    public CmdResponseMessage(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
