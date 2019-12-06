package com.shmedo.mcloudapp.util;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   ApiName
 * 创建者:   dpc
 * 创建时间:  2019/7/5 10:42
 *
 */
public class ApiName {

    /**
     * 获取api版本信息
     */
    public static final String API_VERSION = "ApiVersion";

    /**
     * 发送登录验证码(间隔60秒，有效期15分钟)
     */
    public static final String SEND_SMS_CODE = "SendSmsCode";

    /**
     * 使用手机号和验证码登录
     */
    public static final String SMS_LOGIN = "SmsLogin";

    /**
     * 登录用户
     */
    public static final String SIGNIN = "SignIn";

    /**
     * 获取用户信息
     */
    public static final String GET_MY_INFO = "GetMyInfo";

    /**
     * 用户上传头像接口
     */
    public static final String SET_USER_HEAD_PHOTO = "SetUserHeadPhoto";

    /**
     * v2 3.查询当前用户的项目列表(列表方式、不分页)--系统列表
     */
    public static final String QUERY_USER_LIST_PROJECTEX = "QueryUserListProjectEx";

    /**
     * v2-2  4.2 查询设备基础信息列表——地图展示项目
     */
    public static final String QUERY_DEVICE_BASIC_INFO_LIST = "QueryDeviceBasicInfoList";

    /**
     * v2-2 4.3 查询设备状态信息列表
     */
    public static final String QUERY_DEVICE_STATUS_INFO_LIST = "QueryDeviceStatusInfoList";

    /**
     * 根据系统列表信息查询用户设备
     */
    public static final String QUERY_PROJECT_DEVICE = "QueryProjectDevice";

    /**
     * 查询设备的详细信息
     */
    public static final String GET_DEVICE_DETAIL_INFO = "GetDeviceDetailInfo";
}
