package com.shmedo.mcloudapp.util.permission;

import java.io.*;

/**
 *
 */
public class IOUtils {

    public static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[2048];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            out.flush();
        }
    }

    public static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void flush(Flushable flushable) {
        try {
            flushable.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
