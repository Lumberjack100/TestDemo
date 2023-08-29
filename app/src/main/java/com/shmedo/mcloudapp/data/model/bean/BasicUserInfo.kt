package com.shmedo.mcloudapp.data.model.bean

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/12/22 <br></br>
 * 描述：     基本用户信息
 */
class BasicUserInfo {
    var subjectID //用户ID
            = 0
    var subjectName //用户名称
            : String? = null
    var companyID //公司ID
            = 0
    var subjectType //类型,默认USER
            : String? = null
    var imageUrl //用户头像
            : String? = null
    var phone //用户电话
            : String? = null
    var email: String? = null
}