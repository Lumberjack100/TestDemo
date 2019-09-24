package com.shmedo.mcloudapp.util.page.model;

import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.page.BasePage;
import com.shmedo.mcloudapp.util.page.ParameterValidate;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.page.model
 * 文件名:   SetCollectorPage
 * 创建者:   dpc
 * 创建时间:  2019/4/26 15:51
 * 描述：    设置采集器
 */
public class SetCollectorPage extends BasePage<SetCollectorPage.SetCollectorPageParameter> {

    /**
     * 反序列化参数，校验参数(如果需要)
     */
    public SetCollectorPage(String strParameter, Class<SetCollectorPageParameter> setCollectorPageParameterClass) {
        super(strParameter, setCollectorPageParameterClass);
    }

    @Override protected List<String> generate() {
        /**
         "##1001600500500/r/n" + //获取XX采集器配置--地址、待机时长、工作时长、采集间隔
         */
        List<String> list = new ArrayList<>();
        StringBuilder result1=new StringBuilder();
        StringBuilder result2=new StringBuilder();
        StringBuilder result3=new StringBuilder();
        StringBuilder result4=new StringBuilder();

        result1.append("##147"+parameter.getCaijiAddress()+"\r\n");
        result3.append("##163"+ DeviceFragment.collectorType + StringUtil.formatStringFour(parameter.getCajiJiesunTime())+"\r\n");
        result2.append("##161"+ DeviceFragment.collectorType +  StringUtil.formatStringFive(parameter.getCaijijianGe())+"\r\n");
        result4.append("##160"+ DeviceFragment.collectorType + StringUtil.formatStringFour(parameter.getDaijijianGe())+"\r\n");
        list.add(String.valueOf(result1));
        list.add(String.valueOf(result2));
        list.add(String.valueOf(result3));
        list.add(String.valueOf(result4));
        return list;
    }


    public static class  SetCollectorPageParameter implements ParameterValidate {
        private String caijiAddress;
        private String cajiJiesunTime;
        private String daijijianGe;
        private String caijijianGe;

        public String getCaijiAddress() {
            return caijiAddress;
        }

        public void setCaijiAddress(String caijiAddress) {
            this.caijiAddress = caijiAddress;
        }

        public String getCajiJiesunTime() {
            return cajiJiesunTime;
        }
        public void setCajiJiesunTime(String cajiJiesunTime) {
            this.cajiJiesunTime = cajiJiesunTime;
        }
        public String getDaijijianGe() {
            return daijijianGe;
        }
        public void setDaijijianGe(String daijijianGe) {
            this.daijijianGe = daijijianGe;
        }
        public String getCaijijianGe() {
            return caijijianGe;
        }
        public void setCaijijianGe(String caijijianGe) {
            this.caijijianGe = caijijianGe;
        }

        @Override public void validate() {

        }
    }
}
