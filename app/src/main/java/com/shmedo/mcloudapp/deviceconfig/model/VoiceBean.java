package com.shmedo.mcloudapp.deviceconfig.model;

import java.util.ArrayList;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/1 <br/>
 * 描述：     语音实体类
 */
public class VoiceBean {
    public ArrayList<WSBean> ws;

    public class WSBean {
        public ArrayList<CWBean> cw;
    }

    public class CWBean {
        public String w;
    }
}
