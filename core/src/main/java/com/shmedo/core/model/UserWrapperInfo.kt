package com.shmedo.core.model

import android.text.TextUtils

/**
 * 项目名：  mobileAndroid
 * 包名：    com.shmedo.mobileandroid.entity
 * 文件名:   UserInfo
 * 创建者:   dpc
 * 创建时间:  2018/11/27 14:48
 */
class UserWrapperInfo {
    var user: UserInfo? = null
    var departments: List<DepartmentInfo>? = null

    class UserInfo {
        /**
         * "userID": 636,
         * "companyID": 138,
         * "companyName": "上海米度测控科技有限公司",
         * "account": "medo_gh",
         * "name": "宫贺",
         * "cellPhone": "13915272257",
         * "position": "",
         * "email": "",
         * "phone": "",
         * "address": "",
         * "allowAccessType": 0,
         * "headPhotoPath": null,
         * "userEnable": true,
         * "createUserID": 1,
         * "createTime": "2021-12-20 17:40:31",
         * "updateUserID": 636,
         * "updateTime": "2021-12-21 17:10:41",
         * "expireTime": null,
         * "ssoUser": false,
         * "ssoToken": null
         */
        var userID = 0
        var companyID = 0
        var companyName: String? = null
        var account: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var name: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var cellPhone: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var position: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var email: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var phone: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var address: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var allowAccessType = 0
        var headPhotoPath: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var isUserEnable = false
        var createUserID = 0
        var createTime: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var updateUserID = 0
        var updateTime: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        private val expireTime: String? = null
        private val ssoUser = false
        private val ssoToken: String? = null
    }

    class DepartmentInfo {
        /**
         * "id": 326,
         * "name": "产品开发部",
         * "companyID": 138,
         * "parentID": 323,
         * "desc": null,
         * "level": 2,
         * "readOnly": false,
         * "displayOrder": 1,
         * "createUserID": 539,
         * "createTime": "2021-07-22 11:32:08",
         * "updateUserID": 539,
         * "updateTime": "2021-07-22 11:32:08",
         * "hasChild": false,
         * "delete": false
         */
        var id = 0
        var name: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var companyID = 0
        var parentID = 0
        var desc: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var level = 0
        var isReadOnly = false
        var displayOrder = 0
        var createUserID = 0
        var createTime: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var updateUserID = 0
        var updateTime: String? = null
            get() = if (TextUtils.isEmpty(field)) "" else field
        var isHasChild = false
        var isDelete = false
    }
}