package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/18.
 * 传感器口波特率
 */
public enum  SensorBaudRate {
   BAUD_RATE_1200("1200"),
   BAUD_RATE_2400("2400"),
   BAUD_RATE_4800("4800"),
   BAUD_RATE_9600("9600"),
   BAUD_RATE_19200("19200"),
   BAUD_RATE_57600("57600"),
   BAUD_RATE_1152000("1152000");

   private String baudRate;
   SensorBaudRate(String str) {
       this.baudRate = str;
   }
   public static SensorBaudRate value(String baudRate) {
       switch (baudRate) {
           case "1200": return BAUD_RATE_1200;
           case "2400": return BAUD_RATE_2400;
           case "4800": return BAUD_RATE_4800;
           case "9600": return BAUD_RATE_9600;
           case "19200":return BAUD_RATE_19200;
           case "57600":return BAUD_RATE_57600;
           case "115200":return BAUD_RATE_1152000;
           default:return BAUD_RATE_1200;
       }
   }

    @Override
    public String toString() {
        return this.baudRate;
    }
}
