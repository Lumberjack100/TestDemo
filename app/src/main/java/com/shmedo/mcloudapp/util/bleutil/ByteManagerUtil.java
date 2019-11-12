package com.shmedo.mcloudapp.util.bleutil;

import com.shmedo.das.utils.ByteManager;
import com.shmedo.das.utils.OnBytePackage;

import java.nio.charset.StandardCharsets;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.bleutil
 * 文件名:   ByteManagerUtil
 * 创建者:   dpc
 * 创建时间:  2019/1/21 11:03
 * 描述：    TODO
 */
public class ByteManagerUtil {
    private static final int MAX_LENGTH = 1024;
    private static final byte[] DEFAULT_SPLIT_BYTES = "\r\n".getBytes(StandardCharsets.UTF_8);
    private static ByteManager instance;

    public static void init(OnBytePackage onBytePackage) {
        instance = new ByteManager(DEFAULT_SPLIT_BYTES, onBytePackage, MAX_LENGTH);
    }

    public static void clear() {
        if (instance == null) {
            throw new RuntimeException("字节管理器尚未初始化");
        }
        instance.clear();
    }

    public static ByteManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("字节管理器尚未初始化");
        }
        return instance;
    }
}
