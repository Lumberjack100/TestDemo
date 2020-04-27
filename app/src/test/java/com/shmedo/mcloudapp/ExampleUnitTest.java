package com.shmedo.mcloudapp;


import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testRain() {
        String command = " 123.30.123 8098 ";
        command = command.trim();

        String[] cmd = command.split(" ");
        System.out.println(cmd);
    }

}