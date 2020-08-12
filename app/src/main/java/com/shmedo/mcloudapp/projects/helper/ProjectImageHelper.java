package com.shmedo.mcloudapp.projects.helper;

import com.shmedo.mcloudapp.R;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/12 <br/>
 * 描述：     TODO
 */
public class ProjectImageHelper {

    /**
     * 根据项目类型返回资源 Id
     *
     * @param projectTypeID
     * @return
     */
    public static int getSensorResourceID(int projectTypeID) {
        switch (projectTypeID) {
            case 1://矿山
                return R.drawable.ic_mine;

            case 2://水文水利
                return R.drawable.ic_water_conservancy;

            case 3://国土地质灾害
                return R.drawable.ic_land_disaster;

            case 5://城市基建
                return R.drawable.ic_infrastructure;

            default:
                return R.drawable.ic_project_default;

        }
    }
}
