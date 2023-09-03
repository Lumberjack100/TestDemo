package com.shmedo.mcloudapp.data.repository.remote

import android.annotation.SuppressLint
import com.blankj.utilcode.util.AppUtils
import com.shmedo.lib.core.base.model.BasicUserInfo
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.DeviceStatisticInfo
import com.shmedo.lib.core.base.model.ProductInfo
import com.shmedo.lib.core.base.model.UserWrapperInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.network.parser.PgyerApiResponseParser
import com.shmedo.lib.network.response.PageList
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.common.model.CheckSoftModel
import com.shmedo.mcloudapp.device.model.DispatchCmdItem
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

    /**
     * 查询公司设备在线统计信息
     */
    suspend fun getDeviceStatByCompanyID(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): DeviceStatisticInfo? =
        RxHttp.postJson("/GetDeviceStatByCompanyID")
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
     * 分页查询设备列表
     */
    suspend fun queryDeviceList(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): PageList<DeviceInfo>? =
        RxHttp.postJson("/GetDeviceList")
            .setDomainIfAbsent(BaseURL.IOT_MANAGER_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<PageList<DeviceInfo>>()
            .tryAwait(onCatch)

    /**
     * 批量透明指令下发(限定同一产品)
     */
    suspend fun batchDispatchRawCmd(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<DispatchCmdItem>? =
        RxHttp.postJson("/BatchDispatchRawCmd")
            .setDomainIfAbsent(BaseURL.IOT_INTERACTIVE_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<List<DispatchCmdItem>>()
            .tryAwait(onCatch)

    /**
     * 查询指令响应结果
     */
    suspend fun queryCmdResultByMsgID(
        jsonParam: String,
        onCatch: ((Throwable) -> Unit)? = null
    ): List<DispatchCmdItem>? =
        RxHttp.postJson("/QueryCmdResultByMsgID")
            .setDomainIfAbsent(BaseURL.IOT_INTERACTIVE_SERVICE_ADDRESS.baseUrl)
            .addAll(jsonParam)
            .toAwaitResponse<List<DispatchCmdItem>>()
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