package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

/**
 * 项目名：  CheckAndroid
 * 包名：    cn.shmedo.checkandroid.util
 * 文件名:   UserConfig
 * 创建者:   yuchao
 * 创建时间:  2016/11/10 16:18
 * 描述：    用户参数类
 */

public class UserConfig {

    private SharedPreferences preferences;

    public static UserConfig getConfig(Context con, String configName) {
        return new UserConfig(con, configName);
    }

    public UserConfig(Context con, String configName) {
        preferences = con.getSharedPreferences(configName, Context.MODE_PRIVATE);
    }

    public void writeString(String key, String value) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            byte[] bs = value.getBytes("UTF-8");
            String base64Str = Base64.encodeToString(bs, Base64.DEFAULT);
            editor.putString(key, base64Str);
            editor.commit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);

        }
    }

    public String readString(String key) {
        try {
            String base64String = preferences.getString(key, null);
            if (base64String == null) {
                return null;
            }
            byte[] bs = Base64.decode(base64String, Context.MODE_PRIVATE);
            return new String(bs, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void delete(String key){
        try {
            SharedPreferences.Editor editor=preferences.edit();
            editor.remove(key);
            editor.commit();
        }catch (Exception ex){
            throw  new RuntimeException(ex);
        }

    }
}
