package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/15.
 * 获取服务器的地址和端口
 */
public enum ServerAddress {
    ADDRESS_ONE(1),
    ADDRESS_TWO(2);

    private int port;

    ServerAddress(int port) {
        this.port = port;
    }

    public int toInt() {
        return port;
    }

    public static ServerAddress valueOf(int port) {
        switch (port) {
            case 1:
                return ADDRESS_ONE;
            case 2:
                return ADDRESS_TWO;
            default:
                return ADDRESS_ONE;
        }
    }

}
