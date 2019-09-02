package com.shmedo.mcloudapp.util.page.model;

import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.ui.fragment.ParameterConfigFragment;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.page.BasePage;
import com.shmedo.mcloudapp.util.page.ParameterValidate;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.page.model
 * 文件名:   SetSensorPage
 * 创建者:   dpc
 * 创建时间:  2019/4/4 17:10
 * 描述：    TODO
 */
public class SetSensorPage extends BasePage<SetSensorPage.BaseSensorParameter> {

    public SetSensorPage(String strParameter) {

    }


    @Override protected List<String> generate() {
        /**
         ##150zzxxXXXX\r\n：设置采集器接入的传感器
         */
        StringBuilder builderFirst= new StringBuilder();
        List<String> listFirst = new ArrayList<>();
        List<String> cmdList = new ArrayList<>();

        builderFirst.append("##150");
        BaseSensorParameter  baseSensorParameter = GsonFactory.getLowerCaseGson().fromJson("",BaseSensorParameter.class);
        //02代表采集器型号 01是传感器个数
        builderFirst.append(DeviceFragment.collectorType+"01");
        builderFirst.append(StringUtil.formatStringTwo(baseSensorParameter.getSensorAddress())+
            StringUtil.formatStringTwo(baseSensorParameter.getSensorType())+"\r\n");

        return null;
    }

    public static class BaseSensorParameter {
        protected String sensorType;
        protected String sensorAddress;


        public String getSensorType() {
            return sensorType;
        }

        public void setSensorType(String sensorType) {
            this.sensorType = sensorType;
        }
        public String getSensorAddress() {
            return sensorAddress;
        }
        public void setSensorAddress(String sensorAddress) {
            this.sensorAddress = sensorAddress;
        }
    }

    /**
     * 裂缝计
     */
    public static  class SetDsSensorPageParameter  implements ParameterValidate {
        private String sensorAddress;
        private String correctionValue;
        private String triggerThreshold;


        public String getSensorAddress() {
            return sensorAddress;
        }

        public void setSensorAddress(String sensorAddress) {
            this.sensorAddress = sensorAddress;
        }

        public String getCorrectionValue() {
            return correctionValue;
        }

        public void setCorrectionValue(String correctionValue) {
            this.correctionValue = correctionValue;
        }

        public String getTriggerThreshold() {
            return triggerThreshold;
        }

        public void setTriggerThreshold(String triggerThreshold) {
            this.triggerThreshold = triggerThreshold;
        }

        @Override public void validate() {

        }
    }

}
