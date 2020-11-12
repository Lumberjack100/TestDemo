package com.shmedo.configlibrary.iot.cmd;

import com.shmedo.configlibrary.iot.enums.IOTCommandType;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     TODO
 */
public class IOTCommandResult<T> {
    public static final String COMMAND_HEADER = "$cmd=";
    public static final int RESULT_MIN_LENGTH = 5;
    public static final String ERROR_FLAG = "result=fail";

    /**
     * 命令执行是否成功
     */
    private boolean success;
    /**
     * 执行失败的错误信息
     */
    private String message;

    /**
     * 命令类型
     */
    private IOTCommandType commandType;
    /**
     * 解析结果数据
     */
    private T result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public IOTCommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(IOTCommandType commandType) {
        this.commandType = commandType;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
