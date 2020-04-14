package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/19.
 * 设置服务器地址端口的中服务器编号
 */
public enum  SetServerAddressPort {
    NUMBER_ONE(1),

    NUMBER_TWO(2);



    private int number;
    SetServerAddressPort(int number) {
        this.number = number;
    }
    public static SetServerAddressPort valueOf(int number) {
        switch (number) {
            case 1: return NUMBER_ONE;
            case 2: return NUMBER_TWO;
            default:return NUMBER_ONE;
        }
    }
}
