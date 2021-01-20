package ua.com.expert.validator.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.model.dto.LicenseResponse
import ua.com.expert.validator.sync.SyncService
import ua.com.expert.validator.sync.SyncServiceFactory
import ua.com.expert.validator.utils.NetworkUtils
import ua.com.expert.validator.utils.SharedStorage

class LicenseAviability(var mContext: Context){
    companion object {
        var BROADCAST_LIC = "ua.com.expertsolution.posandroidapp.licensebroadcast"
        var BROADCAST_LIC_ERROR = "lic_error"
    }

    fun check() {

        Thread {

            if (!NetworkUtils.checkEthernet(mContext)) {
                Log.e(TAGLOG, "no internet connection")
                mContext.sendBroadcast(
                        Intent(BROADCAST_LIC)
                                .putExtra(BROADCAST_LIC_ERROR,
                                        mContext.getString(R.string.error_connect_server_activation)))
            }

            val syncService = SyncServiceFactory.createService(
                    SyncService::class.java,
                    mContext)

            syncService.validateLicense(SharedStorage.getString(mContext, Consts.APP_CASH_TOKEN_PREFS, Consts.TOKEN, "").toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread())
                    .subscribe(object : Observer<LicenseResponse> {
                        override fun onSubscribe(d: Disposable) {}

                        override fun onNext(response: LicenseResponse) {

                            if (response == null) {
                                Log.e(TAGLOG, "null response")
                                mContext.sendBroadcast(
                                        Intent(BROADCAST_LIC)
                                                .putExtra(BROADCAST_LIC_ERROR,
                                                        mContext.getString(R.string.error_activation)))
                                return
                            }
                            val textError = response!!.error
                            if (!TextUtils.isEmpty(textError)) {
                                Log.e(TAGLOG, textError)
                                mContext.sendBroadcast(
                                        Intent(BROADCAST_LIC)
                                                .putExtra(BROADCAST_LIC_ERROR, textError))
                                return
                            }

                            Log.i(TAGLOG, "license confirmed")
                            mContext.sendBroadcast(
                                    Intent(BROADCAST_LIC)
                                            .putExtra(BROADCAST_LIC_ERROR, ""))
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAGLOG, e.toString())
                            (mContext as Activity).runOnUiThread {
                                mContext.sendBroadcast(
                                        Intent(BROADCAST_LIC)
                                                .putExtra(BROADCAST_LIC_ERROR,
                                                        mContext.getString(R.string.error_connect_server_activation)))
                            }
                        }

                        override fun onComplete() {}
                    })
        }.start()
    }
}