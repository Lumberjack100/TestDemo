package com.shmedo.mcloudapp.user.viewmodel.request

import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.lib.core.base.model.BasicUserInfo
import com.shmedo.lib.core.base.model.CompanyInfo
import com.shmedo.lib.core.base.model.UserPermissionInfo
import com.shmedo.lib.core.base.model.UserWrapperInfo
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.mcloudapp.data.repository.remote.NetDataRepository
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/29
 *
 * 描述： TODO
 *
 */
class LoginRequestViewModel : BaseViewModel() {
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
                e.printStackTrace()
            }
            val data: String =
                NetDataRepository.instance.sendSmsCode(jsonObjectRequest.toString()) { error: Throwable ->
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
            MmkvCacheUtil.setUserName(mAccount)
            MmkvCacheUtil.setPassword(mPassword)
            MmkvCacheUtil.setToken(token)

            val basicUserInfo: BasicUserInfo = getUserByToken() ?: return@launch
            MmkvCacheUtil.setUserId(basicUserInfo.subjectID)
            MmkvCacheUtil.setUserCompanyId(basicUserInfo.companyID)

            val userWrapperInfo: UserWrapperInfo =
                queryUserByID(basicUserInfo.companyID, basicUserInfo.subjectID) ?: return@launch
            MmkvCacheUtil.setUser(userWrapperInfo.user)

            val iotPermissionList = queryAllPermissionInService(basicUserInfo.companyID)?: return@launch
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

            val iotPermissionList = queryAllPermissionInService(basicUserInfo.companyID)?: return@launch
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
            e.printStackTrace()
        }
        return NetDataRepository.instance.loginByAccount(jsonObjectRequest.toString()) { error: Throwable ->
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
            e.printStackTrace()
        }
        return NetDataRepository.instance.loginByPhone(jsonObjectRequest.toString()) { error: Throwable ->
            val responseStatus = ResponseStatus()
            responseStatus.isSuccess = false
            responseStatus.errorMessage = error.errorMsg
            responseStatus.source = ResultSource.NETWORK
            _loginResult.setValue(DataResult(responseStatus = responseStatus))
        }
    }

    private suspend fun getUserByToken(): BasicUserInfo? =
        NetDataRepository.instance.getUserByToken { error: Throwable ->
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
            e.printStackTrace()
        }

        return NetDataRepository.instance.queryUserByID(jsonObjectRequest.toString()) { error: Throwable ->
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
            e.printStackTrace()
        }

        return NetDataRepository.instance.queryAllPermissionInService(jsonObjectRequest.toString()) { error: Throwable ->
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
            val userWrapperInfo: UserWrapperInfo =
                queryUserByID(companyID, userID) ?: return@launch
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
                e.printStackTrace()
            }

            val companyInfo: CompanyInfo =
                NetDataRepository.instance.queryCompanyInfoByID(jsonObjectRequest.toString()) { error: Throwable ->
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
}