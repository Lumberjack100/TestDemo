package com.shmedo.core.cmd.entity;


import android.text.TextUtils;

import com.shmedo.core.exception.DASParameterException;
import com.shmedo.core.interfaces.Validater;
import com.shmedo.core.utils.DesUtil;
import com.shmedo.core.utils.StringUtil;

import java.nio.charset.StandardCharsets;

/**
 * Created by adu on 2018/1/10.
 * 配置认证请求的参数
 */
public class AuthenticationConfigEntity implements Validater {
    private String sn;
    private String secureCode;

    public AuthenticationConfigEntity(String sn, String secureCode) {
        this.sn = sn;
        this.secureCode = secureCode;
    }

    @Override
    public void validate() {
        //验证sn必须为7位字符
        if (sn.length() != 7)
            throw new DASParameterException("SN号有误");
        //验证secureCode必须为八位数或者八的倍数
        if (TextUtils.isEmpty(secureCode) || secureCode.length() % 8 != 0)
            throw new DASParameterException("参数异常");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(",");
        builder.append(this.sn);
        builder.append(",");

        String tempString = StringUtil.getRandomString() + secureCode;
        byte[] asciiByte = tempString.getBytes(StandardCharsets.US_ASCII);
        byte[] encryptByte = new byte[0];
        try {
            encryptByte = DesUtil.encrypt(asciiByte, secureCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String keyString = StringUtil.bytesToHexString(encryptByte);
        builder.append( keyString);

        return builder.toString();
    }


}
