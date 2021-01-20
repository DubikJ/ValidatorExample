package ua.com.expert.validator.ui.basic

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts.APP_CASH_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.APP_CASH_TOKEN_PREFS
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.common.Consts.TIME_USE_APP
import ua.com.expert.validator.common.Consts.TOKEN
import ua.com.expert.validator.common.Consts.USER_ID
import ua.com.expert.validator.common.Consts.VALID_DATE
import ua.com.expert.validator.model.dto.DownloadResponse
import ua.com.expert.validator.service.GetNewVersionPlayMarket
import ua.com.expert.validator.sync.SyncService
import ua.com.expert.validator.sync.SyncServiceFactory
import ua.com.expert.validator.ui.welcome.WelcomeActivity
import ua.com.expert.validator.utils.NetworkUtils
import ua.com.expert.validator.utils.SharedStorage

class BasicPresenter(var mContext: Context, var viewListener: BasicMVPContract.View?) : BasicMVPContract.Repository {

    var syncService : SyncService = SyncServiceFactory.createService(SyncService::class.java, mContext)

    override fun onDestroy() {
        viewListener = null
    }

    override fun logOut() {
        viewListener?.onStartLoad(null, null)


        if (!NetworkUtils.checkEthernet(mContext)) {
            viewListener?.onError(mContext.getString(R.string.error_internet_connecting))
            Log.e(TAGLOG, mContext.getString(R.string.error_internet_connecting))
            return
        }

        syncService.logOut(SharedStorage.getString(mContext, APP_CASH_TOKEN_PREFS, TOKEN, "").toString())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<DownloadResponse>{
                    override fun onComplete() {}

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(value: DownloadResponse) {
                        if (value == null) {
                            Log.e(TAGLOG, mContext.getString(R.string.error_no_data))
                            viewListener?.onErrorLogOut(mContext.getString(R.string.error_retrieving_data))
                            return
                        }
                        val textError : String = value.error!!
                        if (!TextUtils.isEmpty(textError)) {
                            Log.e(TAGLOG, textError)
                            viewListener?.onErrorLogOut(textError)
                            return
                        }

                        Log.i(TAGLOG, "On next")
                        viewListener?.onLogOut()
                    }

                    override fun onError(e: Throwable) {
                        if (!NetworkUtils.checkEthernet(mContext)) {
                            Log.e(TAGLOG, mContext.getString(R.string.error_internet_connecting))
                            viewListener?.onError(mContext.getString(R.string.error_internet_connecting))
                            return
                        }

                        try {
                            if ((e as HttpException).code() == 401) {
                                viewListener?.onLogOut()
                                return
                            }
                        } catch (exep: Exception) {
                            Log.e(TAGLOG, exep.message)
                        }

                        if (e != null) {
                            try {
                                Log.e(TAGLOG, e.message)
                                viewListener?.onError(e.message!!)
                            } catch (ex: Exception) {
                                Log.e(TAGLOG, "error load")
                                viewListener?.onError(e.message!!)
                            }
                        }
                    }
                } )
    }

    override fun inLogOut(starLogin: Boolean) {
        if (mContext == null) {
            return
        }
        if (starLogin) {
            mContext.startActivity(Intent(mContext, WelcomeActivity::class.java))
        }

        Handler().postDelayed({
            SharedStorage.setLong(mContext, APP_CASH_TOKEN_PREFS, VALID_DATE, 0)
            SharedStorage.setString(mContext, APP_CASH_TOKEN_PREFS, TOKEN, "")
            SharedStorage.setInteger(mContext, APP_CASH_SETTINGS_PREFS, USER_ID, 0)

        }, 200)
    }

    override fun checkUpdate() {
        GetNewVersionPlayMarket(mContext, object : GetNewVersionPlayMarket.CallBackListener {
            override fun onFoundNewVerion() {
                viewListener?.onFoundNewVerion()
            }

            override fun onCancel() {
            }
        }).execute()
    }

    override fun rateApp() {
        if (mContext == null) {
            return
        }
        val timeUseApp = SharedStorage.getInteger(mContext, APP_CASH_SETTINGS_PREFS, TIME_USE_APP, 0)

        if (timeUseApp < 0) {
            return
        } else if (timeUseApp in 0..4) {
            SharedStorage.setInteger(mContext, APP_CASH_SETTINGS_PREFS, TIME_USE_APP, timeUseApp + 1)
            return
        } else {
            if (viewListener != null) {
                viewListener?.onShowRateApp()
            }
        }
    }

    override fun goToMarket() {
        if (mContext != null) {
            try {
                val rateIntent = rateIntentForUrl("market://details")
                mContext.startActivity(rateIntent)
            } catch (e: ActivityNotFoundException) {
                val rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details")
                mContext.startActivity(rateIntent)
            }

        }
    }

    private fun rateIntentForUrl(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, mContext.packageName)))
        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        if (Build.VERSION.SDK_INT >= 21) {
            flags = flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        } else {
            flags = flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
        }
        intent.addFlags(flags)
        return intent
    }

}