package ua.com.expert.validator.ui.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.text.TextUtils
import android.util.Log
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.APP_CASH_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.APP_CASH_TOKEN_PREFS
import ua.com.expert.validator.common.Consts.DATE_VALID_FORMAT
import ua.com.expert.validator.common.Consts.NAME_TERMINAL
import ua.com.expert.validator.common.Consts.SERVER
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.common.Consts.TOKEN
import ua.com.expert.validator.common.Consts.USER_FIRST_NAME
import ua.com.expert.validator.common.Consts.USER_ID
import ua.com.expert.validator.common.Consts.USER_LAST_NAME
import ua.com.expert.validator.common.Consts.USER_MIDDLE_NAME
import ua.com.expert.validator.common.Consts.VALID_DATE
import ua.com.expert.validator.model.dto.AuthRequest
import ua.com.expert.validator.model.dto.AuthResponse
import ua.com.expert.validator.repo.OnRepositoryCallback
import ua.com.expert.validator.repo.Repository
import ua.com.expert.validator.utils.NetworkUtils
import ua.com.expert.validator.utils.SharedStorage
import kotlinx.coroutines.*


class LoginViewModel(var mContext: Context) : ViewModel() {

    var repo = Repository.getInstance(mContext)

    var authRespons = MutableLiveData<AuthResponse>()
    private val job = Job()
    private val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
    private val scopeIO = CoroutineScope(job + Dispatchers.IO)

    fun auth(request : AuthRequest){

        scopeIO.launch {
            if (TextUtils.isEmpty(SharedStorage.getString(mContext, Consts.APP_SETTINGS_PREFS, SERVER, ""))) {
                scopeMainThread.launch { authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.address_server_not_found)).build()}
                return@launch
            }

            if (!NetworkUtils.checkEthernet(mContext)) {
                scopeMainThread.launch {authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build()}
                return@launch
            }

            repo.auth(request, object : OnRepositoryCallback.Auth {
                override fun onError(errorCode: Int) {
                    if (!NetworkUtils.checkEthernet(mContext)) {
                        scopeMainThread.launch {authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build()}
                        return
                    }
                    scopeMainThread.launch {authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.error_retrieving_data)).build()}
                }

                override fun onResponse(response: AuthResponse) {

                    if (!TextUtils.isEmpty(response.error)) {
                        Log.e(TAGLOG, response.error)
                        scopeMainThread.launch {authRespons.value = response}
                        return
                    }

                    if (response.userId == 0) {
                        Log.e(TAGLOG, "no user id")
                        scopeMainThread.launch {authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.error_retrieving_data)).build()}
                        return
                    }

                    SharedStorage.setInteger(mContext, APP_CASH_SETTINGS_PREFS, USER_ID, response.userId)
                    SharedStorage.setString(mContext, APP_CASH_SETTINGS_PREFS, USER_FIRST_NAME, response.firstName)
                    SharedStorage.setString(mContext, APP_CASH_SETTINGS_PREFS, USER_LAST_NAME, response.lastName)
                    SharedStorage.setString(mContext, APP_CASH_SETTINGS_PREFS, USER_MIDDLE_NAME, response.middleName)

                    var dateTimeValid: Long = 0;
                    try {
                        dateTimeValid = DATE_VALID_FORMAT.parse(response.valid).time.toLong()
                    } catch (e: Exception) {
                        e.printStackTrace();
                        scopeMainThread.launch {authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.error_retrieving_data)).build()}
                        return
                    }
                    SharedStorage.setLong(mContext, APP_CASH_TOKEN_PREFS, VALID_DATE, dateTimeValid)
                    SharedStorage.setString(mContext, APP_CASH_TOKEN_PREFS, TOKEN, response.token)

                    SharedStorage.setString(mContext, APP_CASH_SETTINGS_PREFS, NAME_TERMINAL, response.nameTerminal)

                    scopeMainThread.launch {authRespons.value = response}
                }

                override fun onFailure(t: Throwable) {
                    Log.e(TAGLOG, t.message)
                    if (!NetworkUtils.checkEthernet(mContext)) {
                        scopeMainThread.launch {authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build()}
                        return
                    }
                    scopeMainThread.launch {authRespons.value = AuthResponse.Builder().error(mContext.getString(R.string.error_retrieving_data)).build()}
                }
            })
        }
    }

    class ViewModelFactory(private val mContext: Context): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(LoginViewModel::class.java!!)) {
                LoginViewModel(this.mContext) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }

    }

}
