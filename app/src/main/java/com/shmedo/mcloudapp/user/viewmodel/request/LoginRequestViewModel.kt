package com.shmedo.mcloudapp.user.viewmodel.request

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.TimeUtils
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.lib.core.base.model.AppConfigInfo
import com.shmedo.lib.core.base.model.BasicUserInfo
import com.shmedo.lib.core.base.model.CompanyInfo
import com.shmedo.lib.core.base.model.UserPermissionInfo
import com.shmedo.lib.core.base.model.UserWrapperInfo
import com.shmedo.lib.core.base.viewmodel.BaseRequestViewModel
import com.shmedo.lib.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.BuildConfig
import com.shmedo.mcloudapp.data.repository.remote.NetDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
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

    private val _userWrapperInfoResult = MutableResult<DataResult<UserWrapperInfo>>()
    val userWrapperInfoResult: Result<DataResult<UserWrapperInfo>> = _userWrapperInfoResult

    private val _uploadUserAvataResult = MutableResult<DataResult<String>>()
    val uploadUserAvataResult: Result<DataResult<String>> = _uploadUserAvataResult

    private val _updateUserInfoResult = MutableResult<DataResult<String>>()
    val updateUserInfoResult: Result<DataResult<String>> = _updateUserInfoResult

    private val _companyInfoResult = MutableResult<DataResult<CompanyInfo>>()
    val companyInfoResult: Result<DataResult<CompanyInfo>> = _companyInfoResult

    private val _updatePasswordResult = MutableResult<DataResult<String>>()
    val updatePasswordResult: Result<DataResult<String>> = _updatePasswordResult

    /**
     * 发送验证码
     */
    fun requestSendSmsCode(mobile: String) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()//接口请求参数
            try {
                jsonObjectRequest.put("phone", mobile)
            } catch (e: JSONException) {
                Timber.e(e)
            }
            val data: String =
                NetDataRepository.instance.sendSmsCode(jsonObjectRequest.toString()) { error: Throwable ->
                    error.printStackTrace()
                    val msg =
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/SendSmsCode error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
                    addLogItem(
                        sessionId = MmkvCacheUtil.getAppLogSessionId(),
                        priority = Log.ERROR,
                        data = msg
                    )

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _sendCodeResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _sendCodeResult.setValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    /**
     * 通过账户密码登录
     */
    fun requestLogin(mAccount: String, mPassword: String) {
        viewModelScope.launch {
            val token: String = loginByAccount(mAccount, mPassword) ?: return@launch
            MmkvCacheUtil.setAccount(mAccount)
            MmkvCacheUtil.setPassword(mPassword)
            MmkvCacheUtil.setToken(token)

            val basicUserInfo: BasicUserInfo = getUserByToken() ?: return@launch
            MmkvCacheUtil.setUserId(basicUserInfo.subjectID)
            MmkvCacheUtil.setUserCompanyId(basicUserInfo.companyID)
            MmkvCacheUtil.setUserRealName(basicUserInfo.subjectName)

            val userWrapperInfo: UserWrapperInfo =
                queryUserByID(basicUserInfo.companyID, basicUserInfo.subjectID) ?: return@launch
            MmkvCacheUtil.setUser(userWrapperInfo.user)

            val iotPermissionList =
                queryAllPermissionInService(basicUserInfo.companyID) ?: return@launch
            MmkvCacheUtil.setUserPermissionList(iotPermissionList)
            iotPermissionList.forEach {
                if (it.permissionToken == "ListSuperInfo") {
                    MmkvCacheUtil.setHasListSuperInfoPermission(true)
                }
            }

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(token, responseStatus = responseStatus))
        }
    }

    /**
     * 通过手机验证码登录
     */
    fun requestQuickLogin(phone: String, code: String) {
        viewModelScope.launch {
            val token: String = loginByPhone(phone, code) ?: return@launch
            MmkvCacheUtil.setToken(token)

            val basicUserInfo: BasicUserInfo = getUserByToken() ?: return@launch
            MmkvCacheUtil.setUserId(basicUserInfo.subjectID)
            MmkvCacheUtil.setUserCompanyId(basicUserInfo.companyID)

            val userWrapperInfo: UserWrapperInfo =
                queryUserByID(basicUserInfo.companyID, basicUserInfo.subjectID) ?: return@launch
            MmkvCacheUtil.setUser(userWrapperInfo.user)

            val iotPermissionList =
                queryAllPermissionInService(basicUserInfo.companyID) ?: return@launch
            MmkvCacheUtil.setUserPermissionList(iotPermissionList)
            iotPermissionList.forEach {
                if (it.permissionToken == "ListSuperInfo") {
                    MmkvCacheUtil.setHasListSuperInfoPermission(true)
                }
            }

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(token, responseStatus = responseStatus))
        }
    }

    private suspend fun loginByAccount(mAccount: String, mPassword: String): String? {
        val jsonObjectRequest = JSONObject()//接口请求参数
        try {
            jsonObjectRequest.put("account", mAccount)
            jsonObjectRequest.put("password", mPassword)
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return NetDataRepository.instance.loginByAccount(jsonObjectRequest.toString()) { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/SignIn error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
                priority = Log.ERROR,
                data = msg
            )

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = false
            responseStatus.errorMessage = error.errorMsg
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(responseStatus = responseStatus))
        }
    }

    private suspend fun loginByPhone(phone: String, code: String): String? {
        val jsonObjectRequest = JSONObject()//接口请求参数
        try {
            jsonObjectRequest.put("phone", phone)
            jsonObjectRequest.put("code", code)
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return NetDataRepository.instance.loginByPhone(jsonObjectRequest.toString()) { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/SmsLogin error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
                priority = Log.ERROR,
                data = msg
            )

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = false
            responseStatus.errorMessage = error.errorMsg
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(responseStatus = responseStatus))
        }
    }

    private suspend fun getUserByToken(): BasicUserInfo? =
        NetDataRepository.instance.getUserByToken { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/GetUserByToken error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
                priority = Log.ERROR,
                data = msg
            )

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = false
            responseStatus.errorMessage = error.errorMsg
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(responseStatus = responseStatus))
        }

    private suspend fun queryUserByID(companyID: Int = 0, userID: Int = 0): UserWrapperInfo? {
        val jsonObjectRequest = JSONObject()
        try {
            jsonObjectRequest.put("companyID", companyID)
            jsonObjectRequest.put("userID", userID)
        } catch (e: JSONException) {
            Timber.e(e)
        }

        return NetDataRepository.instance.queryUserByID(jsonObjectRequest.toString()) { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/QueryUserByID error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
                priority = Log.ERROR,
                data = msg
            )

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = false
            responseStatus.errorMessage = error.errorMsg
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(responseStatus = responseStatus))
        }
    }

    private suspend fun queryAllPermissionInService(companyID: Int = 0): List<UserPermissionInfo>? {
        val jsonObjectRequest = JSONObject()
        try {
            jsonObjectRequest.put("companyID", companyID)
            jsonObjectRequest.put("serviceName", "iot")
        } catch (e: JSONException) {
            Timber.e(e)
        }

        return NetDataRepository.instance.queryAllPermissionInService(jsonObjectRequest.toString()) { error: Throwable ->
            error.printStackTrace()
            val msg =
                "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/QueryAllPermissionInService error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
            addLogItem(
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
                priority = Log.ERROR,
                data = msg
            )

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = false
            responseStatus.errorMessage = error.errorMsg
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(responseStatus = responseStatus))
        }
    }

    fun requestUpdateUserInfo(jsonParam: String) {
        viewModelScope.launch {
            val data: String =
                NetDataRepository.instance.updateUserInfo(jsonParam) { error: Throwable ->
                    error.printStackTrace()
                    val msg =
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/UpdateUser error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
                    addLogItem(
                        sessionId = MmkvCacheUtil.getAppLogSessionId(),
                        priority = Log.ERROR,
                        data = msg
                    )

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _updateUserInfoResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _updateUserInfoResult.setValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    fun uploadUserAvatar(jsonParam: String) {
        viewModelScope.launch {
            val data: String =
                NetDataRepository.instance.uploadUserAvatar(jsonParam) { error: Throwable ->
                    error.printStackTrace()
                    val msg =
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/UploadUserAvatar error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
                    addLogItem(
                        sessionId = MmkvCacheUtil.getAppLogSessionId(),
                        priority = Log.ERROR,
                        data = msg
                    )

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _uploadUserAvataResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _uploadUserAvataResult.setValue(DataResult(data, responseStatus = responseStatus))
        }
    }

    fun refreshUserInfo(companyID: Int = 0, userID: Int = 0) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", companyID)
                jsonObjectRequest.put("userID", userID)
            } catch (e: JSONException) {
                Timber.e(e)
            }
            val userWrapperInfo: UserWrapperInfo =
                NetDataRepository.instance.queryUserByID(jsonObjectRequest.toString()) { error: Throwable ->
                    error.printStackTrace()
                    val msg =
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/QueryUserByID error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
                    addLogItem(
                        sessionId = MmkvCacheUtil.getAppLogSessionId(),
                        priority = Log.ERROR,
                        data = msg
                    )

                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _userWrapperInfoResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch
            MmkvCacheUtil.setUser(userWrapperInfo.user)

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _userWrapperInfoResult.setValue(
                DataResult(
                    userWrapperInfo,
                    responseStatus = responseStatus
                )
            )
        }
    }

    fun queryCompanyInfoByID(companyID: Int = 0) {
        viewModelScope.launch {
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", companyID)
            } catch (e: JSONException) {
                Timber.e(e)
            }

            val companyInfo: CompanyInfo =
                NetDataRepository.instance.queryCompanyInfoByID(jsonObjectRequest.toString()) { error: Throwable ->
                    error.printStackTrace()
                    val msg =
                        "${BaseURL.AUTHORITY_SERVICE_ADDRESS.baseUrl}/GetCompanyInfo error: ${error.localizedMessage}" //这里的msg是网络请求的错误信息
                    addLogItem(
                        sessionId = MmkvCacheUtil.getAppLogSessionId(),
                        priority = Log.ERROR,
                        data = msg
                    )
                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _companyInfoResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = true
            responseStatus.responseCode = "0"
            responseStatus.source = ResultSource.NETWORK
            _companyInfoResult.setValue(
                DataResult(
                    companyInfo,
                    responseStatus = responseStatus
                )
            )
        }
    }

    //<editor-fold desc="孙建伟通用配置接口">
    /**
     * 加载外部配置
     */
    fun loadExternalConfig() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (MmkvCacheUtil.getAppConfigInfo() == null) {
                    val originalConfigJson = ResourceUtils.readAssets2String("app_config.json")
                    MmkvCacheUtil.setAppConfigInfo(originalConfigJson)
                }
                val amsToken: String = appConfigLogin() ?: return@launch
                MmkvCacheUtil.setAmsToken(amsToken)

                val remoteAppConfigInfo: AppConfigInfo = queryConfigInfoItem() ?: return@launch
                val localAppConfigInfo: AppConfigInfo = MmkvCacheUtil.getAppConfigInfo()!!

                val remoteDate = TimeUtils.string2Date(remoteAppConfigInfo.lastTime)
                val localDate = TimeUtils.string2Date(localAppConfigInfo.lastTime)
                if (remoteAppConfigInfo.configPara.isEmpty() || remoteAppConfigInfo.configPara == "{}"
                    || remoteDate.before(localDate)
                ) {
                    //更新远程配置
                    updateConfigInfoItem(
                        remoteAppConfigInfo.id,
                        localAppConfigInfo.configPara.replace("\\", "")
                    )
                    return@launch
                }
                //更新本地配置
                MmkvCacheUtil.setAppConfigInfo(remoteAppConfigInfo)
            } catch (e: Exception) {
                Timber.e(e)
                val msg =
                    "call loadExternalConfig() error: ${e.localizedMessage}" //这里的msg是网络请求的错误信息
                addLogItem(
                    sessionId = MmkvCacheUtil.getAppLogSessionId(),
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
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
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
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
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
                sessionId = MmkvCacheUtil.getAppLogSessionId(),
                priority = Log.ERROR,
                data = msg
            )
        }
    }
    // </editor-fold>
}