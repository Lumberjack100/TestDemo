package com.shmedo.mcloudapp.ui.viewmodel.request

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.TimeUtils
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.data.repository.NetDataRepository
import com.shmedo.core.model.AppConfigInfo
import com.shmedo.core.model.BasicUserInfo
import com.shmedo.core.model.CompanyInfo
import com.shmedo.core.model.UserPermissionInfo
import com.shmedo.core.model.UserWrapperInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.BuildConfig
import com.shmedo.mcloudapp.model.SingleSelectionItem
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述： TODO
 *
 */
class LoginRequestViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) :
    BaseRequestViewModel(loggerRepositoryImp) {

    private val _sendCodeResult = MutableResult<DataResult<String>>()
    val sendCodeResult: Result<DataResult<String>> = _sendCodeResult

    private val _loginResult = MutableResult<DataResult<String>>()
    val loginResult: Result<DataResult<String>> = _loginResult

    private val _userWrapperInfoResult =
        MutableResult<DataResult<UserWrapperInfo>>()
    val userWrapperInfoResult: Result<DataResult<UserWrapperInfo>> =
        _userWrapperInfoResult

    private val _uploadUserAvataResult = MutableResult<DataResult<String>>()
    val uploadUserAvataResult: Result<DataResult<String>> = _uploadUserAvataResult

    private val _updateUserInfoResult = MutableResult<DataResult<String>>()
    val updateUserInfoResult: Result<DataResult<String>> = _updateUserInfoResult

    private val _companyInfoResult = MutableResult<DataResult<CompanyInfo>>()
    val companyInfoResult: Result<DataResult<CompanyInfo>> =
        _companyInfoResult

    private val _updatePasswordResult = MutableResult<DataResult<String>>()
    val updatePasswordResult: Result<DataResult<String>> = _updatePasswordResult

    /**
     * 发送验证码
     */
    fun requestSendSmsCode(mobile: String) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject().apply {
                put("phone", mobile)
            }
            val data: String =
                NetDataRepository.instance.sendSmsCode(jsonObjectRequest.toString()) { error: Throwable ->
                    handleError(
                        _sendCodeResult,
                        error,
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/SendSmsCode"
                    )
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _sendCodeResult.postValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    /**
     * 通过token登录
     */
    fun requestLoginByToken() {
        viewModelScope.launch {
            handleLogin(
                loginMethod = { getBasicUserInfoByToken() },
                onTokenReceived = {
                }
            )
        }
    }

    /**
     * 通过账户密码登录
     */
    fun requestLoginByAccount(mAccount: String, mPassword: String) {
        viewModelScope.launch {
            handleLogin(
                loginMethod = { loginByAccount(mAccount, mPassword) },
                onTokenReceived = { token ->
                    MmkvCacheUtil.setAccount(mAccount)
                    MmkvCacheUtil.setPassword(mPassword)
                    MmkvCacheUtil.setToken(token.toString())
                }
            )
        }
    }

    /**
     * 通过手机验证码登录
     */
    fun requestLoginByPhoneCaptcha(phone: String, captcha: String) {
        viewModelScope.launch {
            handleLogin(
                loginMethod = { loginByPhone(phone, captcha) },
                onTokenReceived = { token ->
                    MmkvCacheUtil.setToken(token.toString())
                }
            )
        }
    }

    private suspend fun handleLogin(
        loginMethod: suspend () -> Any?,
        onTokenReceived: (Any) -> Unit
    ) {
        val tokenOrBasicUserInfo = loginMethod() ?: return
        onTokenReceived(tokenOrBasicUserInfo)

        val basicUserInfo = when (tokenOrBasicUserInfo) {
            is BasicUserInfo -> tokenOrBasicUserInfo
            is String -> getBasicUserInfoByToken() ?: return
            else -> throw IllegalArgumentException("Unexpected return type from login method")
        }
        AuthMMKVOwner.userID = basicUserInfo.subjectID
        AuthMMKVOwner.companyID = basicUserInfo.companyID
        AuthMMKVOwner.realName = basicUserInfo.subjectName.trim()

        val userWrapperInfo: UserWrapperInfo =
            queryUserByID(basicUserInfo.companyID, basicUserInfo.subjectID) ?: return
        AuthMMKVOwner.userInfo = userWrapperInfo.user

        val iotPermissionList =
            queryAllPermissionInService(basicUserInfo.companyID) ?: return
        MmkvCacheUtil.setUserPermissionList(iotPermissionList)
        iotPermissionList.forEach {
            if (it.permissionToken == "ListSuperInfo") {
                AuthMMKVOwner.listSuperInfoPermission = true
            }
        }

        val responseStatus = ResponseStatus()
        responseStatus.isSuccess = true
        responseStatus.responseCode = "0"
        responseStatus.source = ResultSource.NETWORK
        _loginResult.value = DataResult("success", responseStatus = responseStatus)
    }

    private suspend fun loginByAccount(mAccount: String, mPassword: String): String? {
        val jsonObjectRequest = JSONObject().apply {
            put("account", mAccount)
            put("password", mPassword)
        }
        return NetDataRepository.instance.loginByAccount(jsonObjectRequest.toString()) { error: Throwable ->
            handleError(_loginResult, error, "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/SignIn")
        }
    }

    private suspend fun loginByPhone(phone: String, code: String): String? {
        val jsonObjectRequest = JSONObject().apply {
            put("phone", phone)
            put("code", code)
        }
        return NetDataRepository.instance.loginByPhone(jsonObjectRequest.toString()) { error: Throwable ->
            handleError(
                _loginResult,
                error,
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/SmsLogin"
            )
        }
    }

    private suspend fun getBasicUserInfoByToken(): BasicUserInfo? =
        NetDataRepository.instance.getUserByToken { error: Throwable ->
            handleError(
                _loginResult,
                error,
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/GetUserByToken"
            )
        }

    private suspend fun queryUserByID(
        companyID: Int = 0,
        userID: Int = 0
    ): UserWrapperInfo? {
        val jsonObjectRequest = JSONObject().apply {
            put("companyID", companyID)
            put("userID", userID)
        }

        return NetDataRepository.instance.queryUserByID(jsonObjectRequest.toString()) { error: Throwable ->
            handleError(
                _loginResult,
                error,
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/QueryUserByID"
            )
        }
    }

    private suspend fun queryAllPermissionInService(companyID: Int = 0): List<UserPermissionInfo>? {
        val jsonObjectRequest = JSONObject().apply {
            put("companyID", companyID)
            put("serviceName", "iot")
        }

        return NetDataRepository.instance.queryAllPermissionInService(jsonObjectRequest.toString()) { error: Throwable ->
            handleError(
                _loginResult,
                error,
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/QueryAllPermissionInService"
            )
        }
    }

    fun requestUpdateUserInfo(jsonParam: String) {
        viewModelScope.launch {
            val data: String =
                NetDataRepository.instance.updateUserInfo(jsonParam) { error: Throwable ->
                    handleError(
                        _updateUserInfoResult,
                        error,
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/UpdateUser"
                    )
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _updateUserInfoResult.postValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    fun uploadUserAvatar(jsonParam: String) {
        viewModelScope.launch {
            val data: String =
                NetDataRepository.instance.uploadUserAvatar(jsonParam) { error: Throwable ->
                    handleError(
                        _uploadUserAvataResult,
                        error,
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/UploadUserAvatar"
                    )
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _uploadUserAvataResult.postValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    fun requestUpdatePassword(
        companyID: Int,
        userID: Int,
        newPassword: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject().apply {
                put("companyID", companyID)
                put("userID", userID)
                put("newPassword", newPassword)
                put("confirmPassword", confirmPassword)
            }

            val data: String =
                NetDataRepository.instance.resetPassword(jsonObjectRequest.toString()) { error: Throwable ->
                    handleError(
                        _updatePasswordResult,
                        error,
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/ResetPassword"
                    )
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _updatePasswordResult.postValue(DataResult(data, responseStatus))
        }
    }

    fun refreshUserInfo(companyID: Int = 0, userID: Int = 0) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject().apply {
                put("companyID", companyID)
                put("userID", userID)
            }
            val userWrapperInfo: UserWrapperInfo =
                NetDataRepository.instance.queryUserByID(jsonObjectRequest.toString()) { error: Throwable ->
                    handleError(
                        _userWrapperInfoResult,
                        error,
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/QueryUserByID"
                    )
                } ?: return@launch
            AuthMMKVOwner.userInfo = userWrapperInfo.user

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _userWrapperInfoResult.postValue(
                DataResult(
                    userWrapperInfo,
                    responseStatus = responseStatus
                )
            )
        }
    }

    fun queryCompanyInfoByID(companyID: Int = 0) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject().apply {
                put("companyID", companyID)
            }

            val companyInfo: CompanyInfo =
                NetDataRepository.instance.queryCompanyInfoByID(jsonObjectRequest.toString()) { error: Throwable ->
                    handleError(
                        _companyInfoResult,
                        error,
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/GetCompanyInfo"
                    )
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _companyInfoResult.postValue(
                DataResult(
                    companyInfo,
                    responseStatus = responseStatus
                )
            )
        }
    }

    suspend fun getCompanyList(companyName: String = ""): MutableList<SingleSelectionItem> =
        withContext(Dispatchers.Default) {
            val jsonObject = JSONObject().apply {
                put("companyName", companyName)
            }
            val tempList = NetDataRepository.instance.queryUserInCompanyList(
                jsonObject.toString()
            ) ?: emptyList()

            tempList.map {
                SingleSelectionItem(
                    name = it.companyName,
                    extValue = it.companyID.toString()
                )
            }.toMutableList()
        }

    //<editor-fold desc="孙建伟通用配置接口">
    /**
     * 加载外部配置
     */
    fun loadExternalConfig() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val originalConfigJson = ResourceUtils.readAssets2String("app_config.json")
                val originalConfigInfo =
                    MoshiUtil.fromJson<AppConfigInfo>(originalConfigJson) ?: AppConfigInfo()
                if (MmkvCacheUtil.getAppConfigInfo() == null) {
                    MmkvCacheUtil.setAppConfigInfo(originalConfigJson)
                } else {
                    val cacheAppConfigInfo: AppConfigInfo = MmkvCacheUtil.getAppConfigInfo()!!
                    val originalDate = TimeUtils.string2Date(originalConfigInfo.lastTime)
                    val cacheDate = TimeUtils.string2Date(cacheAppConfigInfo.lastTime)
                    //资源配置文件较新，则更新本地缓存配置
                    if (cacheDate.before(originalDate)) {
                        MmkvCacheUtil.setAppConfigInfo(originalConfigJson)
                    }
                }

                //与远程配置文件比较
                val amsToken: String = appConfigLogin() ?: return@launch
                MmkvCacheUtil.setAmsToken(amsToken)

                val remoteAppConfigInfo: AppConfigInfo = queryConfigInfoItem() ?: return@launch
                val cacheAppConfigInfo: AppConfigInfo = MmkvCacheUtil.getAppConfigInfo()!!

                val remoteDate = TimeUtils.string2Date(remoteAppConfigInfo.lastTime)
                val localDate = TimeUtils.string2Date(cacheAppConfigInfo.lastTime)
                //本地配置文件较新，则更新远程配置
                if (remoteAppConfigInfo.configPara.isEmpty()
                    || remoteAppConfigInfo.configPara == "{}"
                    || remoteDate.before(localDate)
                ) {
                    //更新远程配置
                    updateConfigInfoItem(
                        remoteAppConfigInfo.id,
                        cacheAppConfigInfo.configPara.replace("\\", "")
                    )
                    return@launch
                }

                //远程配置文件较新，则更新本地配置
                MmkvCacheUtil.setAppConfigInfo(remoteAppConfigInfo)
            } catch (e: Exception) {
                Timber.e(e)
                val msg =
                    "call loadExternalConfig() error: ${e.localizedMessage}" //这里的msg是网络请求的错误信息
                addLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = Log.ERROR,
                    data = msg
                )
            }
        }
    }

    private suspend fun appConfigLogin(): String? {
        val jsonObjectRequest = JSONObject()//接口请求参数
        jsonObjectRequest.put("appKey", BuildConfig.AMS_APP_KEY)
        jsonObjectRequest.put("appSecret", BuildConfig.AMS_APP_SECRET)
        jsonObjectRequest.put("account", MmkvCacheUtil.getAccount())
        jsonObjectRequest.put("password", MmkvCacheUtil.getPassword())
        return NetDataRepository.instance.appConfigLogin(jsonObjectRequest.toString()) { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AMS_CONFIG_ADDRESS.baseUrl}/Login error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = CommonMMKVOwner.appLogSessionId,
                priority = Log.ERROR,
                data = msg
            )
        }
    }

    private suspend fun queryConfigInfoItem(): AppConfigInfo? =
        NetDataRepository.instance.queryConfigInfoItem { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AMS_CONFIG_ADDRESS.baseUrl}/QueryConfigInfoItem error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = CommonMMKVOwner.appLogSessionId,
                priority = Log.ERROR,
                data = msg
            )
        }

    private suspend fun updateConfigInfoItem(id: Int, configPara: String): String? {
        val jsonObjectRequest = JSONObject()//接口请求参数
        jsonObjectRequest.put("id", id)
        jsonObjectRequest.put("configPara", configPara)
        jsonObjectRequest.put("desc", "")
        return NetDataRepository.instance.updateConfigInfoItem(jsonObjectRequest.toString()) { error: Throwable ->
            error.printStackTrace()

            val msg =
                "${BaseURL.AMS_CONFIG_ADDRESS.baseUrl}/UpdateConfigInfoItem error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = CommonMMKVOwner.appLogSessionId,
                priority = Log.ERROR,
                data = msg
            )
        }
    }
    // </editor-fold>
}