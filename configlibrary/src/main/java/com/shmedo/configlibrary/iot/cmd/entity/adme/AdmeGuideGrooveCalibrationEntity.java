package com.shmedo.configlibrary.iot.cmd.entity.adme;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：      生成ADME导槽校准配置参数拼接指令
 */
public class AdmeGuideGrooveCalibrationEntity  implements Validater {
    private String movementway;//运动方式（0:正转，1:反转）
    private String motorspeed;//电机速度
    private String moveangle;//运动距离

    public void setMovementway(String movementway) {
        this.movementway = movementway;
    }

    public void setMotorspeed(String motorspeed) {
        this.motorspeed = motorspeed;
    }

    public void setMoveangle(String moveangle) {
        this.moveangle = moveangle;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("movementway=" + movementway);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(motorspeed)) {
            stringBuilder.append("motorspeed=" + motorspeed);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(moveangle)) {
            stringBuilder.append("moveangle=" + moveangle);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
