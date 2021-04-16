package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/15 <br/>
 * 描述：     获取服务器(数据中心)编号参数
 */
public class IndexEntity implements Validater {
    private int index;

    public IndexEntity(int number) {
        this.index = number;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        return "index=" + index;
    }
}
