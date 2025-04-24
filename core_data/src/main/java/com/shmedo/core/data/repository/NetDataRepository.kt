package com.shmedo.core.data.repository

import android.annotation.SuppressLint
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.model.BasicCompanyInfo
import com.shmedo.core.model.BasicUserInfo
import com.shmedo.core.model.CompanyInfo
import com.shmedo.core.model.DeviceBackupInfo
import com.shmedo.core.model.DeviceDebugAddress
import com.shmedo.core.model.DeviceDetailInfo
import com.shmedo.core.model.MR702PortSensorConfig
import com.shmedo.core.model.UserPermissionInfo
import com.shmedo.core.model.UserWrapperInfo
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.util.BaseURL
import rxhttp.tryAwait
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/8/28 <br/>
 * 描述：     TODO
 */
@SuppressLint("CheckResult")
class NetDataRepository private constructor() {

    //<editor-fold desc="孙建伟通用配置接口">
    /**
     * 获取App应用信息
     */
    suspend fun appRemoteConfigLogin(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/auth/SignIn")
            .setDomainIfAbsent(BaseURL.MIYITONG_REMOTE_CONFIG_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 获取配置信息
     */
    suspend fun queryMR702SensorConfigList(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): MR702PortSensorConfig? =
        RxHttp.postJson("/config/QueryMR702SensorConfigList")
            .setDomainIfAbsent(BaseURL.MIYITONG_REMOTE_CONFIG_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getRemoteConfigToken())
            .addAll(jsonParam)
            .toAwaitResponse<MR702PortSensorConfig>()
            .tryAwait(onCatch)

    /**
     * 查询设备远程调试连接地址信息
     */
    suspend fun getRemoteDeviceLogin(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceDebugAddress? =
        RxHttp.postJson("/DeviceLogin")
            .setDomainIfAbsent(BaseURL.MIYITONG_REMOTE_CONFIG_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<DeviceDebugAddress>()
            .tryAwait(onCatch)
    // </editor-fold>

    //<editor-fold desc="登录、用户信息">
    /**
     * 发送验证码
     */
    suspend fun sendSmsCode(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/SendSmsCode")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 账户密码登录
     */
    suspend fun loginByAccount(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/SignIn")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("access_type", "android")
            .addHeader("access_service", "mcloud")
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 手机验证码登录
     */
    suspend fun loginByPhone(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/SmsLogin")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("access_type", "android")
            .addHeader("access_service", "mcloud")
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 通过token获取用户基本信息
     */
    suspend fun getUserByToken(onCatch: ((Throwable) -> Unit)? = null): BasicUserInfo? =
        RxHttp.get("/GetUserByToken")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .toAwaitResponse<BasicUserInfo>()
            .tryAwait(onCatch)

    /**
     * 查询用户详细信息
     */
    suspend fun queryUserByID(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): UserWrapperInfo? =
        RxHttp.postJson("/QueryUserByID")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<UserWrapperInfo>()
            .tryAwait(onCatch)

    /**
     * 修改用户信息
     */
    suspend fun updateUserInfo(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/UpdateUser")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 上传用户头像
     */
    suspend fun uploadUserAvatar(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/UploadUserAvatar")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 重置用户密码
     */
    suspend fun resetPassword(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/ResetPassword")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 查询用户在某公司某服务中的所有权限
     */
    suspend fun queryAllPermissionInService(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<UserPermissionInfo>? =
        RxHttp.postJson("/QueryAllPermissionInService")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<List<UserPermissionInfo>>()
            .tryAwait(onCatch)

    suspend fun queryCompanyInfoByID(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): CompanyInfo? =
        RxHttp.postJson("/GetCompanyInfo")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<CompanyInfo>()
            .tryAwait(onCatch)

    /**
     * 查询用户所在的所有公司列表
     */
    suspend fun queryUserInCompanyList(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<BasicCompanyInfo>? =
        RxHttp.postJson("/QueryUserInCompanyList")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<List<BasicCompanyInfo>>()
            .tryAwait(onCatch)
    // </editor-fold>

    /**
     * 查询设备详细信息
     */
    suspend fun getDeviceDetailInfo(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceDetailInfo? =
        RxHttp.postJson("/GetDeviceDetail")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<DeviceDetailInfo>()
            .tryAwait(onCatch)

    /**
     * 分页设备备份记录列表
     */
    suspend fun queryDeviceBackupListWithPage(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<DeviceBackupInfo>? =
        RxHttp.postJson("/QueryDeviceBackup")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addHeader("Authorization", MmkvCacheUtil.getToken())
            .addAll(jsonParam)
            .toAwaitResponse<PageList<DeviceBackupInfo>>()
            .tryAwait(onCatch)

    companion object {
        val instance = NetDataRepository()
    }
}