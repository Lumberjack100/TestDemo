package com.shmedo.configlibrary.ble.utils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/23 <br/>
 * 描述：    CRC8校验码计算
 */
public class CRC8 {
    /**
     * CRC8 校验 多项式  x8+x2+x+1
     *
     * @param data
     * @return 校验和
     */
    public static byte calcCrc8(byte[] data) {
        byte crc = 0;
        for (int j = 0; j < data.length; j++) {
            crc ^= data[j];
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (byte) ((crc) << 1);
                    crc ^= 0x107;
                } else {
                    crc = (byte) ((crc) << 1);
                }
            }
        }
        return crc;
    }
}
