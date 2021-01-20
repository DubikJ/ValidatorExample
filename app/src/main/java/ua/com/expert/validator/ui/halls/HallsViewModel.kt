package ua.com.expert.validator.ui.halls

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
import ua.com.expert.validator.model.dto.CinemaPlaceResponse
import ua.com.expert.validator.repo.OnRepositoryCallback
import ua.com.expert.validator.repo.Repository
import ua.com.expert.validator.utils.NetworkUtils
import ua.com.expert.validator.utils.SharedStorage


class HallsViewModel(var mContext: Context) : ViewModel() {

    var repo = Repository.getInstance(mContext)

    var cinemaPlaceResponse = MutableLiveData<CinemaPlaceResponse>()

    fun getCinemaPlaces(){

        if(TextUtils.isEmpty(SharedStorage.getString(mContext, Consts.APP_SETTINGS_PREFS, SERVER, ""))){
            cinemaPlaceResponse.value = CinemaPlaceResponse.Builder().error(mContext.getString(R.string.address_server_not_found)).build()
            return
        }

        if(!NetworkUtils.checkEthernet(mContext)){
            cinemaPlaceResponse.value = CinemaPlaceResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build()
            return
        }

        repo.getCinemaPlaces(object : OnRepositoryCallback.Places{

            override fun onResponse(response: CinemaPlaceResponse) {

                if(!TextUtils.isEmpty(response.error)) {
                    Log.e(TAGLOG, response.error)
                    cinemaPlaceResponse.value = response
                    return
                }
                cinemaPlaceResponse.value = response
            }

            override fun onFailure(t: Throwable) {
                Log.e(TAGLOG, t.message)
                if(NetworkUtils.checkEthernet(mContext)){
                    cinemaPlaceResponse.value = CinemaPlaceResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build()
                    return
                }
                cinemaPlaceResponse.value = CinemaPlaceResponse.Builder().error(mContext.getString(R.string.error_retrieving_data)).build()
            }

            override fun onError(errorCode: Int) {
                cinemaPlaceResponse.value = CinemaPlaceResponse.Builder()
                        .error(mContext.getString(R.string.error_retrieving_data))
                        .errorCode(errorCode)
                                .build()
            }
        })
    }

    class ViewModelFactory(private val mContext: Context): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HallsViewModel::class.java!!)) {
                HallsViewModel(this.mContext) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }

    }

}
