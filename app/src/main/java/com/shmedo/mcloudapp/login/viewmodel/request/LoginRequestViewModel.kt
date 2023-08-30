package com.shmedo.mcloudapp.login.viewmodel.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.lib.network.response.DataResult
import com.shmedo.lib.network.response.ResponseStatus
import com.shmedo.lib.network.response.ResultSource
import com.shmedo.mcloudapp.data.model.bean.BasicUserInfo
import com.shmedo.mcloudapp.data.model.bean.UserWrapperInfo
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
class LoginRequestViewModel : ViewModel() {
    private val _loginResult = MutableResult<DataResult<String>>()
    val loginResult: Result<DataResult<String>> = _loginResult

    private val _userWrapperInfoResult = MutableResult<DataResult<UserWrapperInfo>>()
    val userWrapperInfoResult: Result<DataResult<UserWrapperInfo>> = _userWrapperInfoResult

    private val _updatePasswordResult = MutableResult<DataResult<String>>()
    val updatePasswordResult: Result<DataResult<String>> = _updatePasswordResult


    fun requestLogin(jsonParam: String) =
        viewModelScope.launch {
            val token: String? =
                NetDataRepository.instance.loginByAccount(jsonParam) { error: Throwable ->
                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _loginResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            MmkvCacheUtil.setToken(token)

            val basicUserInfo: BasicUserInfo? =
                NetDataRepository.instance.getUserByToken { error: Throwable ->
                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _loginResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch

            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", basicUserInfo!!.companyID)
                jsonObjectRequest.put("userID", basicUserInfo.subjectID)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val userWrapperInfo: UserWrapperInfo? =
                NetDataRepository.instance.queryUserByID(jsonObjectRequest.toString()) { error: Throwable ->
                    val responseStatus = ResponseStatus()
                    responseStatus.isSuccess = false
                    responseStatus.errorMessage = error.errorMsg
                    responseStatus.source = ResultSource.NETWORK
                    _loginResult.setValue(DataResult(responseStatus = responseStatus))
                } ?: return@launch


//            try {
//
//            } catch (error: Throwable) {
//                //请求异常，通过it拿到Throwable对象
//                val responseStatus = ResponseStatus()
//                responseStatus.isSuccess = false
//                responseStatus.errorMessage = error.errorMsg
//                responseStatus.source = ResultSource.NETWORK
//                _loginResult.setValue(DataResult(responseStatus = responseStatus))
//            }
        }
}