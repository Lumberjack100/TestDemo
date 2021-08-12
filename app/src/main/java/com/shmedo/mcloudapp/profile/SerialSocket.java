package com.shmedo.mcloudapp.profile;

import android.hardware.usb.UsbDeviceConnection;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.shmedo.mcloudapp.profile.callback.SerialListener;

import java.io.IOException;

public class SerialSocket implements SerialInputOutputManager.Listener {

    private static final int WRITE_WAIT_MILLIS = 2000; // 0 blocked infinitely on unprogrammed arduino

    private SerialListener listener;
    private UsbDeviceConnection connection;
    private UsbSerialPort serialPort;
    private SerialInputOutputManager ioManager;

    public SerialSocket(UsbDeviceConnection connection, UsbSerialPort serialPort) {
        this.connection = connection;
        this.serialPort = serialPort;
    }

    public String getName() {
        return serialPort.getDriver().getClass().getSimpleName().replace("SerialDriver", "");
    }

    public void connect(SerialListener listener) throws IOException {
        this.listener = listener;
        serialPort.setDTR(true); // for arduino, ...
        serialPort.setRTS(true);
        ioManager = new SerialInputOutputManager(serialPort, this);
        ioManager.start();
    }

    public void disconnect() {
        listener = null; // ignore remaining data and errors
        if (ioManager != null) {
            ioManager.setListener(null);
            ioManager.stop();
            ioManager = null;
        }
        if (serialPort != null) {
            try {
                serialPort.setDTR(false);
                serialPort.setRTS(false);
            } catch (Exception ignored) {
            }
            try {
                serialPort.close();
            } catch (Exception ignored) {
            }
            serialPort = null;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void write(byte[] data) throws IOException {
        if (serialPort == null)
            throw new IOException("not connected");
        serialPort.write(data, WRITE_WAIT_MILLIS);
    }

    @Override
    public void onNewData(byte[] data) {
        if (listener != null)
            listener.onSerialRead(data);
    }

    @Override
    public void onRunError(Exception e) {
        if (listener != null)
            listener.onSerialIoError(e);
    }
}
