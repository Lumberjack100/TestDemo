package com.shmedo.core.enums;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：   数据中心链路编号
 */
public enum DataCenterLinkNumber {
    NUMBER_ONE(1),
    NUMBER_TWO(2),
    NUMBER_THREE(3);

    private int number;

    DataCenterLinkNumber(int number) {
        this.number = number;
    }

    public int toInt() {
        return number;
    }

    public static DataCenterLinkNumber valueOf(int number) {
        switch (number) {
            case 1:
                return NUMBER_ONE;
            case 2:
                return NUMBER_TWO;
            case 3:
                return NUMBER_THREE;
            default:
                return NUMBER_ONE;
        }
    }
}
