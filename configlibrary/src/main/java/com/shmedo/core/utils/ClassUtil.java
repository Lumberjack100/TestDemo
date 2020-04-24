package com.shmedo.core.utils;


import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liudongdong on 17/12/12.
 */
public class ClassUtil {
    public static <T> List<Class<? extends T>> getClass(String packageName, Class<T> tClass) {
        Reflections reflections = new Reflections(packageName);
        List<Class<? extends T>> list = new ArrayList<>();
        list.addAll(reflections.getSubTypesOf(tClass));
        return list;
    }

    /**
     * 取得某个接口下所有实现这个接口的类
     */
    public static List<Class> getAllClassByInterface(String packageName, Class inter) {
        List<Class> returnClassList = new ArrayList<>();
        if (inter.isInterface()) {
            Reflections reflections = new Reflections(packageName);
            // 获取当前包下以及子包下所以的类
            List<Class> allClass = new ArrayList<>();
            allClass.addAll(reflections.getSubTypesOf(Object.class));
            for (Class clazz : allClass) {
                // 判断是否是同一个接口
                if (inter.isAssignableFrom(clazz)) {
                    // 本身不加入进去
                    if (!inter.equals(clazz)) {
                        returnClassList.add(clazz);
                    }
                }
            }
        }

        return returnClassList;
    }

}
