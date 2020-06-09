package com.shmedo.mcloudapp;


import com.shmedo.core.utils.ValidateUtil;

import org.junit.Test;

import timber.log.Timber;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testRain() {

        System.out.println(-12345678.0);

        boolean result = ValidateUtil.isDouble("222.888r");

        Timber.d("结果：" + result);
    }

}