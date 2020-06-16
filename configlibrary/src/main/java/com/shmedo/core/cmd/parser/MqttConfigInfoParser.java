package com.shmedo.core.cmd.parser;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.MqttConfigInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/24 <br/>
 * 描述：    TODO
 */
public class MqttConfigInfoParser implements ResultParser<MqttConfigInfo> {
    @Override
    public MqttConfigInfo parse(String result) {
        String[] cmd = result.split(",", -1);
        MqttConfigInfo mqttConfigInfo = new MqttConfigInfo();
        mqttConfigInfo.setCommunicationProtocol(cmd[2]);//通讯协议，2：MDM协议，4：MQTT自动注册，5：MQTT手动注册
        mqttConfigInfo.setDataPlatformAddress(cmd[3]);//数据平台地址
        mqttConfigInfo.setKeepAliveValue(cmd[4]);
        mqttConfigInfo.setDeviceSn(cmd[5]);//设备SN号
        mqttConfigInfo.setProductId(cmd[6]);//产品ID
        mqttConfigInfo.setRegisterCode(cmd[7]);//注册码
        mqttConfigInfo.setRegisterPlatform(cmd[8]);//注册平台类型，0：地大平台，1：成都理工平台，2：米度平台
        mqttConfigInfo.setRegisterPlatformAddress(cmd[9]);//注册平台地址
        mqttConfigInfo.setAppKey(cmd[10]);
        mqttConfigInfo.setMqttDeviceId(cmd[11]);//MQTT设备ID
        mqttConfigInfo.setMqttUsername(cmd[12]);//MQTT用户名
        mqttConfigInfo.setMqttPassword(cmd[13]);//MQTT密码

        return mqttConfigInfo;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.QUERY_DATA_CENTER_PARAM;
    }
}
