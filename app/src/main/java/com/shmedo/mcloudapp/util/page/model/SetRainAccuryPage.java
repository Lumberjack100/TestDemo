package com.shmedo.mcloudapp.util.page.model;

import com.shmedo.mcloudapp.util.page.BasePage;
import com.shmedo.mcloudapp.util.page.ParameterValidate;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.page
 * 文件名:   SetRainpage
 * 创建者:   dpc
 * 创建时间:  2019/4/4 09:24
 * 描述：    TODO
 */
public class SetRainAccuryPage extends BasePage<SetRainAccuryPage.SetRianAccuryParameter> {
    //"##12150/r/n" +         //雨量计精度

    /**
     * 设置雨量计精度
     */
    @Override protected List<String> generate() {
        List<String> list = new ArrayList<>();
        StringBuilder result=new StringBuilder();
        result.append("##121");
        result.append(parameter.getRainAccury()+"\r\n");
        list.add(String.valueOf(result));
        return list;
    }


    public static class SetRianAccuryParameter implements ParameterValidate {
        /**
         * 雨量站精度
         */
        private String rainAccury;
        public String getRainAccury() {
            return rainAccury;
        }
        public void setRainAccury(String rainAccury) {
            this.rainAccury = rainAccury;
        }


        @Override public void validate() {

        }
    }
}
