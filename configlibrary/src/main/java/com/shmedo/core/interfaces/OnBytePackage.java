package com.shmedo.core.interfaces;

/**
 * Created by Liudongdong on 18/1/19.
 */
public interface OnBytePackage {
    /**
     * 一个完整的字节数组包被解析出来
     *
     * @param data 字节数组
     */
    void onPackageArrived(byte[] data);
}
