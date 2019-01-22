package com.shmedo.mcloudapp.bluetooth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Liudongdong on 18/2/1.
 * 标识一个方法必须在UI线程中调用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunOnUiThread {
}
