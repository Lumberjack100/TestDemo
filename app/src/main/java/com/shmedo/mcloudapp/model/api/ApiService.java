package com.shmedo.mcloudapp.model.api;

import com.shmedo.mcloudapp.entity.ResultWrapper;
import com.shmedo.mcloudapp.entity.UserInfo;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

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

    @Headers({HEADER_API_VERSION})
    @GET("ApiVersion")
    Observable<ResultWrapper<String>> getApiVerson();

    @Headers({HEADER_API_VERSION})
    @POST("SignIn")
    Observable<ResultWrapper<String>> getSingIn(@Body RequestBody parameter);

    @Headers({HEADER_API_VERSION})
    @GET("GetMyInfo")
    Observable<ResultWrapper<UserInfo>> getMyInfo(@Header(CommonVariable.ACCESS_TOKEN) String token);

    @Headers({HEADER_API_VERSION})
    @POST("SendSmsCode")
    Observable<ResultWrapper<String>> sendSmsCode(@Header("app_key") String appKey,
                                                  @Header("app_secret") String appSecret, String phoneNumber);

    @Headers({HEADER_API_VERSION})
    @POST("SmsLogin")
    Observable<ResultWrapper<String>> SmsLogin(@Body RequestBody parameter);
}
