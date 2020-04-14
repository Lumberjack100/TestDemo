package com.shmedo.core.cmd.parser;


import com.shmedo.core.annotations.Parser;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.SettingRemoteUpgrade;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.SettingRemoteUpgradeInfo;

/**
 * Created by adu on 2017/12/18.
 *  解析设置远程升级
 */
@Parser
public class SettingRemoteUpgradeParser implements ResultParser<SettingRemoteUpgradeInfo> {
    @Override
    public SettingRemoteUpgradeInfo parse(String result) {
        SettingRemoteUpgradeInfo info = new SettingRemoteUpgradeInfo();
        info.setModel(SettingRemoteUpgrade.valueOf(Integer.valueOf(result.substring(5))));
        info.setPort(Integer.valueOf(result.substring(6)));
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
