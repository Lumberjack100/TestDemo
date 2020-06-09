package com.shmedo.mcloudapp;


import org.junit.Test;

import java.util.Locale;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testRain() {

        //2.000000e+00
        String result = String.format(Locale.getDefault(), "%.3f", Double.parseDouble("4.400000e+01"));

        double dd = Double.parseDouble("4.400000e+01");

        int tt = (int) dd;
        System.out.println(-12345678.0);

    }

}