package com.shmedo.core.cmd;

import com.shmedo.core.enums.CommandType;

/**
 * Created by Liudongdong on 17/12/11.
 * 命令返回结果封装类
 */
public  class CommandResult<T> {
    public static final String COMMAND_RESULT_HEADER = "$$";
    public static final int RESULT_MIN_LENGTH = 5;
    public static final String ERROR_END = "e";

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
    private CommandType commandType;
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

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

}
