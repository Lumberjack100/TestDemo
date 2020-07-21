package com.shmedo.mcloudapp.network.api;

import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceDetailInfo;
import com.shmedo.mcloudapp.entity.PageResult;
import com.shmedo.mcloudapp.entity.ProjectDeviceInfo;
import com.shmedo.mcloudapp.entity.QueryCloudDataInfo;
import com.shmedo.mcloudapp.entity.ResultWrapper;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectBaseInfo;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.user.model.CompanyInfo;

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
 */
public interface ApiService {

    //获取api版本信息
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @GET("ApiVersion")
    Observable<ResultWrapper<String>> getApiVerson();

    //登录用户
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("SignIn")
    Observable<ResultWrapper<String>> getSingIn(@Body RequestBody parameter);

    //获取用户信息
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @GET("GetMyInfo")
    Observable<ResultWrapper<UserInfo>> getMyInfo(@Header(NetworkConst.ACCESS_TOKEN) String token);

    //发送登录验证码(间隔60秒，有效期15分钟)
    @Headers({NetworkConst.HEADER_ACCESS_TYPE, NetworkConst.HEADER_APP_KEY, NetworkConst.HEADER_APP_SECRET})
    @POST("SendSmsCode")
    Observable<ResultWrapper<String>> sendSmsCode(@Body RequestBody parameter);


    //使用手机号和验证码登录
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("SmsLogin")
    Observable<ResultWrapper<String>> SmsLogin(@Body RequestBody parameter);

    //系统接口v2  修改当前登录用户的密码
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("ChangeMyPassword")
    Observable<ResultWrapper<String>> ChangeMyPassword(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2  修改我的手机号码
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("UpdateMyCellPhone")
    Observable<ResultWrapper<String>> UpdateMyCellPhone(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2  修改我的信息
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("UpdateMyInfo")
    Observable<ResultWrapper<String>> UpdateMyInfo(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //用户上传头像
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("SetUserHeadPhoto")
    Observable<ResultWrapper<String>> setUserHeadPhoto(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);


    //系统接口v2  公司模块 4.查询单个公司信息
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("GetCompanyInfo")
    Observable<ResultWrapper<CompanyInfo>> GetCompanyInfo(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);


    //系统接口v2  7.10 查询设备的详情信息
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("GetDeviceDetailInfo")
    Observable<ResultWrapper<DeviceDetailInfo>> GetDeviceDetailInfo(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //项目接口V2 7.3 查询当前用户的项目列表(项目类型方式)
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("QueryUserListProjectEx")
    Observable<ResultWrapper<List<ProjectBaseInfo>>> QueryUserListProject(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //项目接口V2-4  查询警报阈值列表
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("QueryProjectListInfo")
    Observable<ResultWrapper<List<ProjectDetailInfo>>> QueryProjectListInfo(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);



    //系统接口v2-2 4.3 查询设备状态信息列表 QueryDeviceStatusInfoList
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("QueryDeviceStatusInfoList")
    Observable<ResultWrapper<List<StatusInfoResult>>> QueryDeviceStatusInfoList(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2-2 4.2 查询设备基础信息列表——地图展示项目 QueryDeviceBasicInfoList
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("QueryDeviceBasicInfoList")
    Observable<ResultWrapper<List<DeviceBasicInfoResult>>> QueryDeviceBasicInfoList(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //系统接口v2  7.15 根据系统列表信息查询用户设备
    @Headers({NetworkConst.HEADER_ACCESS_TYPE})
    @POST("QueryProjectDevice")
    Observable<ResultWrapper<PageResult<ProjectDeviceInfo>>> QueryProjectDevice(@Header(NetworkConst.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //验证E60设备,需传入一个完整的 Url，不需要调用接口
    @GET
    Observable<ResultWrapper<String>> ValidateDeviceE60(@Url String url);

    //查询数据
    @POST("queryCloudData")
    Observable<ResultWrapper<List<QueryCloudDataInfo>>> QueryCloudData(@Body RequestBody parameter);
}
