package com.shmedo.mcloudapp.model.api;

import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceDetailInfo;
import com.shmedo.mcloudapp.entity.PageResult;
import com.shmedo.mcloudapp.entity.ProjectDeviceInfo;
import com.shmedo.mcloudapp.entity.ResultWrapper;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.ApiName;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.model.api
 * 文件名:   ApiService
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:41
 * 描述：    TODO
 */
public interface ApiService {

    String HEADER_API_VERSION = "access_type:android";

    //获取api版本信息
    @Headers({HEADER_API_VERSION})
    @GET(ApiName.API_VERSION)
    Observable<ResultWrapper<String>> getApiVerson();

    //登录用户
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.SIGNIN)
    Observable<ResultWrapper<String>> getSingIn(@Body RequestBody parameter);

    //获取用户信息
    @Headers({HEADER_API_VERSION})
    @GET(ApiName.GET_MY_INFO)
    Observable<ResultWrapper<UserInfo>> getMyInfo(@Header(CommonVariable.ACCESS_TOKEN) String token);

    //发送登录验证码(间隔60秒，有效期15分钟)
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.SEND_SMS_CODE)
    Observable<ResultWrapper<String>> sendSmsCode(@Header("app_key") String appKey, @Header("app_secret") String appSecret, @Body RequestBody parameter);

    //使用手机号和验证码登录
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.SMS_LOGIN)
    Observable<ResultWrapper<String>> SmsLogin(@Body RequestBody parameter);

    //用户上传头像
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.SET_USER_HEAD_PHOTO)
    Observable<ResultWrapper<String>> setUserHeadPhoto(@Body RequestBody parameter);

    //系统接口v2  7.10 查询设备的详情信息
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.GET_DEVICE_DETAIL_INFO)
    Observable<ResultWrapper<DeviceDetailInfo>> GetDeviceDetailInfo(@Header(CommonVariable.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2 7.3 查询当前用户的项目列表(项目类型方式) QueryUserListProjectEx
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.QUERY_USER_LIST_PROJECTEX)
    Observable<ResultWrapper<List<SystemDataInfo>>> QueryUserListProject(@Header(CommonVariable.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2-2 4.3 查询设备状态信息列表 QueryDeviceStatusInfoList
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.QUERY_DEVICE_STATUS_INFO_LIST)
    Observable<ResultWrapper<List<StatusInfoResult>>> QueryDeviceStatusInfoList(@Header(CommonVariable.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2-2 4.2 查询设备基础信息列表 QueryDeviceBasicInfoList
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.QUERY_DEVICE_BASIC_INFO_LIST)
    Observable<ResultWrapper<List<DeviceBasicInfoResult>>> QueryDeviceBasicInfoList(@Header(CommonVariable.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2  7.15 查询项目设备
    @Headers({HEADER_API_VERSION})
    @POST(ApiName.QUERY_PROJECT_DEVICE)
    Observable<ResultWrapper<PageResult<ProjectDeviceInfo>>> QueryProjectDevice(@Header(CommonVariable.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //验证E60设备,需传入一个完整的 Url，不需要调用接口
    @GET
    Observable<ResultWrapper<String>> ValidateDeviceE60(@Url String url);
}
