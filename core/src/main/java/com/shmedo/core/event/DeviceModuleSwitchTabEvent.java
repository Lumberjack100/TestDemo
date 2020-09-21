package com.shmedo.core.event;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/21 <br/>
 * 描述：    设备模块切换 Tab 消息
 */
public class DeviceModuleSwitchTabEvent extends MessageEvent {
    private int tabPosition = 0;

    public DeviceModuleSwitchTabEvent(int tabPosition) {
        this.tabPosition = tabPosition;
    }

    public int getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(int tabPosition) {
        this.tabPosition = tabPosition;
    }
}
