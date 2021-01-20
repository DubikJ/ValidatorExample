package ua.com.expert.validator.ui.validate

import android.content.Context
import android.util.Log
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts.APP_CASH_TOKEN_PREFS
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.common.Consts.TOKEN
import ua.com.expert.validator.model.dto.CheckTiketRequest
import ua.com.expert.validator.model.dto.DownloadResponse
import ua.com.expert.validator.sync.SyncService
import ua.com.expert.validator.sync.SyncServiceFactory
import ua.com.expert.validator.utils.NetworkUtils
import ua.com.expert.validator.utils.SharedStorage

class ValidPresenter(var mContext: Context, var viewListener: ValidMVPContract.View?) : ValidMVPContract.Repository {

    var syncService : SyncService = SyncServiceFactory.createService(SyncService::class.java, mContext)

    override fun onDestroy() {
        viewListener = null
    }

    override fun checkTicket(request: CheckTiketRequest){
        viewListener?.onStartLoad(null, null)


        if (!NetworkUtils.checkEthernet(mContext)) {
            viewListener?.onError(mContext.getString(R.string.error_internet_connecting))
            Log.e(TAGLOG, mContext.getString(R.string.error_internet_connecting))
            return
        }

        syncService.checkTicket(
                SharedStorage.getString(mContext, APP_CASH_TOKEN_PREFS, TOKEN, "").toString(),
                request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<DownloadResponse>{
                    override fun onComplete() {}

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(value: DownloadResponse) {
                        if (value == null) {
                            Log.e(TAGLOG, mContext.getString(R.string.error_no_data))
                            viewListener?.onError(mContext.getString(R.string.error_retrieving_data))
                            return
                        }

                        viewListener?.result(value)
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

}