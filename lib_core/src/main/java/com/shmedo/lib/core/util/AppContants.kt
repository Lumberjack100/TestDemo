package com.shmedo.lib.core.util

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/2/13 <br/>
 * 描述：     TODO
 */
interface AppContants {

    companion object {
        const val PGY_API_KEY = "64454bf76fe2abd8dec45200c11fc93b"
        const val PGY_APP_KEY = "b8a852c106c6cc532332081e22f218a9"
    }

    interface Extras {
        companion object {
            //状态栏颜色
            const val STATUS_BAR_COLOR = "status_bar_color"

            const val IS_REFRESH_USER_INFO = "is_refresh_user_info"

            const val USER_INFO = "user_info"
        }
    }
}