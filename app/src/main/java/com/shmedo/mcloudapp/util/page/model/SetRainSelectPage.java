package com.shmedo.mcloudapp.util.page.model;

import com.shmedo.mcloudapp.util.page.BasePage;
import com.shmedo.mcloudapp.util.page.ParameterValidate;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.page.model
 * 文件名:   SetRainSelectPage
 * 创建者:   dpc
 * 创建时间:  2019/4/4 10:40
 * 描述：    雨量站开关
 */
public class SetRainSelectPage extends BasePage<SetRainSelectPage.SetSelectRainParameter> {

    /**
     * 反序列化参数，校验参数(如果需要)
     */
    public SetRainSelectPage(String strParameter, Class<SetSelectRainParameter> setSelectRainParameterClass) {
        super(strParameter, setSelectRainParameterClass);
    }

        //"##0051/r/n" +          //雨量站
    /**
     * 开启/关闭 雨量站   3断线报警器开启3
     */
    @Override protected List<String> generate() {
        List<String> list = new ArrayList<>();
        StringBuilder result=new StringBuilder();
        result.append("##005");
        if (parameter.getRainSelect().equals("1")){
            result.append("1\r\n");
        }else if (parameter.getRainSelect().equals("2")){
            result.append("2\r\n");
        }else if (parameter.getRainSelect().equals("3")){
            result.append("3\r\n");
        }
        list.add(String.valueOf(result));
        return list;
    }


    public static class SetSelectRainParameter implements ParameterValidate {
        /**
         * 开启或者关闭雨量站
         */
        private String rainSelect;

        public String getRainSelect() {
            return rainSelect;
        }

        public void setRainSelect(String rainSelect) {
            this.rainSelect = rainSelect;
        }


        @Override public void validate() {

        }
    }
}
