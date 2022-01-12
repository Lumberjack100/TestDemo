package com.shmedo.mcloudapp.network.api;

import com.shmedo.core.model.BasicUserInfo;
import com.shmedo.core.model.UserPermissionInfo;
import com.shmedo.core.model.UserWrapperInfo;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.deviceconfig.model.BasicDeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceStatisticInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.ProductInfo;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCloudDataInfo;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ResponseWrapper;
import com.shmedo.mcloudapp.user.model.BasicCompanyInfo;
import com.shmedo.mcloudapp.user.model.CompanyInfo;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
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
 */
public interface ApiService {
    /**
     * 权限
     */
    //查询用户在某公司某服务中的所有权限
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("QueryAllPermissionInService")
    Observable<ResponseWrapper<List<UserPermissionInfo>>> queryAllPermissionInService(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    /**
     * 登录模块
     */
    //用户名密码登录
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("SignIn")
    Observable<ResponseWrapper<String>> getSingIn(@Body RequestBody parameter);

    //发送登录验证码(间隔60秒，有效期15分钟)
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("SendSmsCode")
    Observable<ResponseWrapper<String>> sendSmsCode(@Body RequestBody parameter);

    //使用手机号和验证码登录
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("SmsLogin")
    Observable<ResponseWrapper<String>> smsLogin(@Body RequestBody parameter);

    /**
     * 用户信息模块
     */
    //通过token获取用户信息
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @GET("GetUserByToken")
    Observable<ResponseWrapper<BasicUserInfo>> getUserByToken(@Header(RequestHeader.ACCESS_TOKEN) String token);

    //查询用户信息
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("QueryUserByID")
    Observable<ResponseWrapper<UserWrapperInfo>> queryUserByID(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //重置用户密码
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("ResetPassword")
    Observable<ResponseWrapper<String>> resetPassword(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //修改用户信息
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("UpdateUser")
    Observable<ResponseWrapper<String>> updateUser(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //用户上传头像
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("UploadUserAvatar")
    Observable<ResponseWrapper<String>> uploadUserAvatar(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //分页查询用户所在的所有公司
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("QueryUserInCompany")
    Observable<ResponseWrapper<PageResult<BasicCompanyInfo>>> queryUserInCompany(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //获取公司信息
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("GetCompanyInfo")
    Observable<ResponseWrapper<CompanyInfo>> getCompanyInfo(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    /**
     * 设备模块
     */
    //统计公司下的设备
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("GetDeviceStatByCompanyID")
    Observable<ResponseWrapper<DeviceStatisticInfo>> getDeviceStatByCompanyID(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //分页查询产品列表
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("QueryProduct")
    Observable<ResponseWrapper<PageResult<ProductInfo>>> queryProduct(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //查询设备列表
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("GetDeviceList")
    Observable<ResponseWrapper<PageResult<DeviceInfo>>> getDeviceList(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //获取设备概要信息
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("DescribeDeviceSimpleInfo")
    Observable<ResponseWrapper<BasicDeviceInfo>> getDescribeDeviceSimpleInfo(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //查询固件列表
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("GetFirmwareList")
    Observable<ResponseWrapper<PageResult<FirmWareInfo>>> getFirmwareList(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //对单个设备进行固件升级
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("BatchFirmwareUpgrade")
    Observable<ResponseWrapper<String>> batchFirmwareUpgrade(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    /**
     * 指令交互
     */
    //批量透明指令下发(限定同一产品)
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("BatchDispatchRawCmd")
    Observable<ResponseWrapper<List<DispatchCmdItem>>> batchDispatchRawCmd(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //查询指令响应结果
    @Headers({RequestHeader.HEADER_ACCESS_TYPE})
    @POST("QueryCmdResultByMsgID")
    Observable<ResponseWrapper<List<QueryCmdResult>>> queryCmdResultByMsgID(@Header(RequestHeader.ACCESS_TOKEN) String token, @Body RequestBody parameter);

    //查询数据
    @POST("queryCloudData")
    Observable<ResponseWrapper<List<QueryCloudDataInfo>>> QueryCloudData(@Body RequestBody parameter);
}
