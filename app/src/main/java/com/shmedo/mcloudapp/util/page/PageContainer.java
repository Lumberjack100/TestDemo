package com.shmedo.mcloudapp.util.page;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.page
 * 文件名:   PageContainer
 * 创建者:   dpc
 * 创建时间:  2019/4/4 13:39
 *
 */
public class PageContainer {

    private HashMap<String, BasePage> pages = new HashMap<>();



    public List<String> generateCmd() {
        if (pages.size() == 0) {
            return Collections.emptyList();
        }
        List<BasePage> basePages = new LinkedList<>(pages.values());

        List<String> result = new LinkedList<>();

        for (BasePage page : basePages) {
            result.addAll(page.generate());
        }
        return result;
    }
}
