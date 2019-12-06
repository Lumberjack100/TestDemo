package com.shmedo.mcloudapp.util.page;

import com.shmedo.mcloudapp.util.GsonFactory;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.page
 * 文件名:   BasePage
 * 创建者:   dpc
 * 创建时间:  2019/4/4 10:31
 *
 */
public abstract class BasePage<T> {


    protected T parameter;

    public BasePage() {
    }
    /**
     * 反序列化参数，校验参数(如果需要)
     *
     * @param strParameter
     * @param tClass
     */
    public BasePage(String strParameter, Class<T> tClass) {
        parameter = GsonFactory.getLowerCaseGson().fromJson(strParameter, tClass);
        if (parameter == null) {
            throw new RuntimeException("非法的参数");
        }
        if (parameter instanceof ParameterValidate) {
            ParameterValidate parameterValidate = (ParameterValidate) parameter;
            parameterValidate.validate();
        }
    }
    protected abstract List<String> generate();

}
