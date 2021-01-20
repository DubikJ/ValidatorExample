package ua.com.expert.validator.repo

import android.content.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.com.expert.validator.common.Consts.APP_CASH_TOKEN_PREFS
import ua.com.expert.validator.common.Consts.TOKEN
import ua.com.expert.validator.model.dto.*
import ua.com.expert.validator.sync.SyncService
import ua.com.expert.validator.sync.SyncServiceFactory.createService
import ua.com.expert.validator.utils.SharedStorage


class Repository(var mContext: Context) {

    private var syncService: SyncService? = null

    companion object {
        private var repository: Repository? = null
        fun getInstance(mContext: Context): Repository {
            if (repository == null) {
                repository = Repository(mContext)
            }
            return repository as Repository
        }
    }

    fun buildSyncService(){
        syncService = createService(SyncService::class.java, mContext)
    }

    fun auth(request: AuthRequest, onRepositoryCallback: OnRepositoryCallback.Auth){
        if(syncService==null){
            buildSyncService()
        }
        syncService!!.authenticate(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    onRepositoryCallback.onResponse(response.body() as AuthResponse)
                }else{
                    onRepositoryCallback.onError(response.code())
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                onRepositoryCallback.onFailure(t)
            }
        })
    }

    fun getCinemaPlaces(onRepositoryCallback: OnRepositoryCallback.Places){
        if(syncService==null){
            buildSyncService()
        }
        syncService!!.getCinemaPlaceGroup(SharedStorage.getString(mContext, APP_CASH_TOKEN_PREFS, TOKEN, "").toString()).enqueue(object : Callback<CinemaPlaceResponse> {
            override fun onResponse(call: Call<CinemaPlaceResponse>, response: Response<CinemaPlaceResponse>) {
                if (response.isSuccessful) {
                    onRepositoryCallback.onResponse(response.body() as CinemaPlaceResponse)
                }else{
                    onRepositoryCallback.onError(response.code())
                }
            }

            override fun onFailure(call: Call<CinemaPlaceResponse>, t: Throwable) {
                onRepositoryCallback.onFailure(t)
            }
        })
    }

    fun getCinemaSessions(request: CinemaSessionRequest, onRepositoryCallback: OnRepositoryCallback.Sessions){
        if(syncService==null){
            buildSyncService()
        }
        syncService!!.getCinemaSessions(
                SharedStorage.getString(mContext, APP_CASH_TOKEN_PREFS, TOKEN, "").toString(),
                request).enqueue(object : Callback<CinemaSessionResponse> {
            override fun onResponse(call: Call<CinemaSessionResponse>, response: Response<CinemaSessionResponse>) {
                if (response.isSuccessful) {
                    onRepositoryCallback.onResponse(response.body() as CinemaSessionResponse)
                }else{
                    onRepositoryCallback.onError(response.code())
                }
            }

            override fun onFailure(call: Call<CinemaSessionResponse>, t: Throwable) {
                onRepositoryCallback.onFailure(t)
            }
        })
    }
}