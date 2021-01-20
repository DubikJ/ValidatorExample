package ua.com.expert.validator.ui.validate

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.text.TextUtils
import android.util.Log
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.SERVER
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.model.dto.CinemaSessionRequest
import ua.com.expert.validator.model.dto.CinemaSessionResponse
import ua.com.expert.validator.repo.OnRepositoryCallback
import ua.com.expert.validator.repo.Repository
import ua.com.expert.validator.utils.NetworkUtils
import ua.com.expert.validator.utils.SharedStorage


class ValidateViewModel(var mContext: Context) : ViewModel() {

    var repo = Repository.getInstance(mContext)

    var cinemaSessionResponse = MutableLiveData<CinemaSessionResponse>()

    fun getCinemaSessions(request: CinemaSessionRequest){

        if(TextUtils.isEmpty(SharedStorage.getString(mContext, Consts.APP_SETTINGS_PREFS, SERVER, ""))){
            cinemaSessionResponse.value = CinemaSessionResponse.Builder().error(mContext.getString(R.string.address_server_not_found)).build()
            return
        }

        if(!NetworkUtils.checkEthernet(mContext)){
            cinemaSessionResponse.value = CinemaSessionResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build()
            return
        }

        repo.getCinemaSessions(request, object : OnRepositoryCallback.Sessions{

            override fun onResponse(response: CinemaSessionResponse) {

                if(!TextUtils.isEmpty(response.error)) {
                    Log.e(TAGLOG, response.error)
                    cinemaSessionResponse.value = response
                    return
                }
                cinemaSessionResponse.value = response
            }

            override fun onFailure(t: Throwable) {
                Log.e(TAGLOG, t.message)
                if(NetworkUtils.checkEthernet(mContext)){
                    cinemaSessionResponse.value = CinemaSessionResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build()
                    return
                }
                cinemaSessionResponse.value = CinemaSessionResponse.Builder().error(mContext.getString(R.string.error_retrieving_data)).build()
            }

            override fun onError(errorCode: Int) {
                cinemaSessionResponse.value = CinemaSessionResponse.Builder()
                        .error(mContext.getString(R.string.error_retrieving_data))
                        .errorCode(errorCode)
                                .build()
            }
        })
    }

    class ViewModelFactory(private val mContext: Context): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ValidateViewModel::class.java!!)) {
                ValidateViewModel(this.mContext) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }

    }

}
