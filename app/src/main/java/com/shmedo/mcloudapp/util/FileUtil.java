package com.shmedo.mcloudapp.util;

import android.os.Environment;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.Utils
 * 文件名:   FileUtil
 * 创建者:   dpc
 * 创建时间:  2018/1/25 17:56
 * 描述：    TODO
 */

public class FileUtil {
    public static String toString(String filePath)
    {
        try {
            File f=new File(filePath);
            if(!f.exists())
                return null;
            InputStream is = new FileInputStream(f);
            byte[]bs=new byte[is.available()];
            is.read(bs);
            String fileContent= Base64.encodeToString(bs, Base64.DEFAULT);
            return fileContent;
        }
        catch (Exception ex)
        {throw new RuntimeException(ex);}
    }

    public static String getFileName(String filePath)
    {
        int index=filePath.lastIndexOf("/");
        return filePath.substring(index+1);
    }

    /**
     * 返回文件大小，单位MB
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath)
    {
        File f=new File(filePath);
        if(!f.exists())
            return 0;
        return f.length();
    }

    public static String getFileSizeString(String filePath)
    {
        double fileSize=(double)getFileSize(filePath);
        return getFileSizeString(fileSize);
    }

    public static String getFileSizeString(double fileSize)
    {
        double fileSizeDouble=0;
        String unit="B";
        if(fileSize<=1024)
        {
            fileSizeDouble=fileSize;
            unit="B";
        }
        else if(fileSize<=1024*1024)
        {
            fileSizeDouble=fileSize/1024;
            unit="KB";
        }
        else
        {
            fileSizeDouble=fileSize/(1024*1024);
            unit="MB";
        }
        NumberFormat nf=NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        String result=nf.format(fileSizeDouble)+unit;
        return result;
    }

    public static File getOutputMediaFile(int uploadMediaType) {
        File mediaStorageDir = null;
        try {
            mediaStorageDir = new File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"NetMonitor/");
        } catch (Exception e) {
            //Log.e(ErrCode.ERROR_TAG, e.getMessage(), e);
        }
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                //Log.i(ErrCode.INFO_TAG, "failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(new Date());
        File mediaFile;
        if (uploadMediaType == UploadMediaType.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        } else if (uploadMediaType == UploadMediaType.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static class UploadMediaType {
        public static final int MEDIA_TYPE_IMAGE = 1;
        public static final int MEDIA_TYPE_VIDEO = 2;
    }
}
