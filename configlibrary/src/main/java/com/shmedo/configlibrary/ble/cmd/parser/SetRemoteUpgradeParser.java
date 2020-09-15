package com.shmedo.configlibrary.ble.cmd.parser;


import com.shmedo.configlibrary.ble.annotations.Parser;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SetRemoteUpgrade;
import com.shmedo.configlibrary.ble.interfaces.ResultParser;
import com.shmedo.configlibrary.ble.model.SetRemoteUpgradeInfo;

/**
 * Created by adu on 2017/12/18.
 *  解析设置远程升级
 */
@Parser
public class SetRemoteUpgradeParser implements ResultParser<SetRemoteUpgradeInfo> {
    @Override
    public SetRemoteUpgradeInfo parse(String result) {
        SetRemoteUpgradeInfo info = new SetRemoteUpgradeInfo();
        info.setModel(SetRemoteUpgrade.valueOf(Integer.parseInt(result.substring(5))));
        info.setPort(Integer.parseInt(result.substring(6)));
        return info;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.SETTING_REMOTE_UPGRADE;
    }
}
