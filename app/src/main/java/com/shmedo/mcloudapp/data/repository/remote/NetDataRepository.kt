package com.shmedo.mcloudapp.data.repository.remote

import android.annotation.SuppressLint
import com.blankj.utilcode.util.AppUtils
import com.shmedo.lib.core.base.model.BasicUserInfo
import com.shmedo.lib.core.base.model.CompanyInfo
import com.shmedo.lib.core.base.model.DeviceBackupInfo
import com.shmedo.lib.core.base.model.DeviceDetailInfo
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.DeviceStatisticInfo
import com.shmedo.lib.core.base.model.ProductInfo
import com.shmedo.lib.core.base.model.UserPermissionInfo
import com.shmedo.lib.core.base.model.UserWrapperInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.network.parser.CloudPlatformApiResponseParser
import com.shmedo.lib.network.parser.PgyerApiResponseParser
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.common.model.CheckSoftModel
import com.shmedo.mcloudapp.device.model.CloudDeviceData
import com.shmedo.mcloudapp.device.model.DispatchCmdItem
import com.shmedo.mcloudapp.device.model.QueryCmdResult
import rxhttp.toAwait
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
    //<editor-fold desc="用户">
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
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 通过token获取用户基本信息
     */
    suspend fun getUserByToken(onCatch: ((Throwable) -> Unit)? = null): BasicUserInfo? =
        RxHttp.get("/GetUserByToken")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
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
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    suspend fun queryCompanyInfoByID(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): CompanyInfo? =
        RxHttp.postJson("/GetCompanyInfo")
            .setDomainIfAbsent(BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<CompanyInfo>()
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
            .addAll(jsonParam)
            .toAwaitResponse<List<UserPermissionInfo>>()
            .tryAwait(onCatch)
    // </editor-fold>


    /**
     * 根据公司ID对公司下的设备进行统计 或者 统计系统所有设备的信息(系统权限，预定义权限，不允许授予第三方)
     *
     * 总数 = 启用+未启用= 在线+离线+未知
     */
    suspend fun getDeviceStatByCompanyID(
        jsonParam: String,
        isHasListSuperInfoPermission: Boolean = false,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceStatisticInfo? =
        RxHttp.postJson(if (isHasListSuperInfoPermission) "/ListSuperDeviceStat" else "/GetDeviceStatByCompanyID")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<DeviceStatisticInfo>()
            .tryAwait(onCatch)

    /**
     * 分页查询产品列表
     */
    suspend fun getProductList(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<ProductInfo>? =
        RxHttp.postJson("/QueryProduct")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<PageList<ProductInfo>>()
            .tryAwait(onCatch)

    /**
     * 查询用户在其所在的所有公司的产品列表，包含其所在公司的所有下级公司
     */
    suspend fun getUserCompanyProductList(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<ProductInfo>? =
        RxHttp.postJson("/ListUserCompanyProduct")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<PageList<ProductInfo>>()
            .tryAwait(onCatch)

    /**
     * 分页查询设备列表(默认排序是创建时间倒序)或者 查询系统所有设备列表(系统权限，预定义权限，不允许授予第三方)
     */
    suspend fun queryDeviceList(
        jsonParam: String,
        isHasListSuperInfoPermission: Boolean = false,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<DeviceInfo>? =
        RxHttp.postJson(if (isHasListSuperInfoPermission) "/ListSuperDevice" else "/QueryDeviceList")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<PageList<DeviceInfo>>()
            .tryAwait(onCatch)

    /**
     * 查询设备详细信息
     */
    suspend fun getDeviceDetailInfo(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceDetailInfo? =
        RxHttp.postJson("/GetDeviceDetail")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
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
            .addAll(jsonParam)
            .toAwaitResponse<PageList<DeviceBackupInfo>>()
            .tryAwait(onCatch)

    /**
     * 给一个设备应用一个备份
     */
    suspend fun applyBackup(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): String? =
        RxHttp.postJson("/ApplyBackup")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<String>()
            .tryAwait(onCatch)

    /**
     * 批量透明指令下发(限定同一产品)
     */
    suspend fun batchDispatchRawCmd(
        jsonParam: String
    ): List<DispatchCmdItem> =
        RxHttp.postJson("/BatchDispatchRawCmd")
            .setDomainIfAbsent(BaseURL.IOT_INTERACTIVE_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<List<DispatchCmdItem>>()
            .await()

    /**
     * 查询指令响应结果
     */
    suspend fun queryCmdResultByMsgID(
        jsonParam: String
    ): List<QueryCmdResult> =
        RxHttp.postJson("/QueryCmdResultByMsgID")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<List<QueryCmdResult>>()
            .await()

    /**
     * 分页查询设备数据列表
     */
    suspend fun queryCloudDataExWithPage(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<CloudDeviceData>? =
        RxHttp.postJson("/QueryCloudDataEx")
            .setDomainIfAbsent(BaseURL.CLOUD_PLATFORM_DATA_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwait(object : CloudPlatformApiResponseParser<PageList<CloudDeviceData>>() {})
            .tryAwait(onCatch)

    suspend fun checkAppVersion(
        onCatch: ((Throwable) -> Unit)? = null
    ): CheckSoftModel? =
        RxHttp.postForm("/check")
            .setDomainIfAbsent(BaseURL.PGYER_SERVICE_ADDRESS.baseUrl)
            .add("_api_key", AppContants.PGY_API_KEY)
            .add("appKey", AppContants.PGY_APP_KEY)
            .add("buildVersion", AppUtils.getAppVersionName())
            .toAwait(object : PgyerApiResponseParser<CheckSoftModel>() {})
            .tryAwait(onCatch)

    companion object {
        val instance = NetDataRepository()
    }
}