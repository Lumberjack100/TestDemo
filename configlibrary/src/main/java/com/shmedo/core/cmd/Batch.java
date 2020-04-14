package com.shmedo.core.cmd;

import java.util.List;

/**
 * Created by adu on 2018/1/9.
 */
public class Batch {
    /**
     * 当前批的顺序，从0开始
     */
    private int order;
    /**
     * 批处理中的命令
     */
    private List<String> commands;
    private boolean isReboot;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }


    public boolean isReboot() {
        return isReboot;
    }

    public void setReboot(boolean reboot) {
        isReboot = reboot;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private int order;
        private List<String> commands;
        private boolean isReboot;

        public Builder setOrder(int order) {
            this.order = order;
            return this;
        }

        public Builder setCommands(List<String> commands) {
            this.commands = commands;
            return this;
        }

        public Builder setReboot(boolean reboot) {
            isReboot = reboot;
            return this;
        }

        public Batch build()
        {
            Batch batch=new Batch();
            batch.setReboot(isReboot);
            batch.setOrder(order);
            batch.setCommands(commands);
            return batch;
        }
    }
}
