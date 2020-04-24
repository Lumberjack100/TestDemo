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
        String command = "##9161,123.12412,30.123\r\n";

        String cmd = command.replace("\r\n", "").substring(7);
        System.out.println(cmd);
    }

}