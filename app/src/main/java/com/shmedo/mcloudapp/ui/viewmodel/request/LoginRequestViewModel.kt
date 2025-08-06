package com.shmedo.mcloudapp.ui.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.data.repository.NetDataRepository
import com.shmedo.core.model.BasicUserInfo
import com.shmedo.core.model.CompanyInfo
import com.shmedo.core.model.UserPermissionInfo
import com.shmedo.core.model.UserWrapperInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.lib.network.util.BaseURL
import com.shmedo.mcloudapp.model.SingleSelectionItem
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

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
                    AuthMMKVOwner.account = mAccount
                    AuthMMKVOwner.password = mPassword
                    AuthMMKVOwner.token = token.toString()
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
                    AuthMMKVOwner.token = token.toString()
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
        AuthMMKVOwner.phone = basicUserInfo.phone

        val userWrapperInfo: UserWrapperInfo =
            queryUserByID(basicUserInfo.companyID, basicUserInfo.subjectID) ?: return
        AuthMMKVOwner.userInfo = userWrapperInfo.user

        val iotPermissionList =
            queryAllPermissionInService(basicUserInfo.companyID) ?: return
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
}