package ua.com.expert.validator.ui.usbvalidate

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.text.TextUtils
import android.util.Log
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.SERVER
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.model.ValidateEvent
import ua.com.expert.validator.model.dto.ValidateRequest
import ua.com.expert.validator.model.dto.ValidateResponse
import ua.com.expert.validator.sync.SyncService
import ua.com.expert.validator.sync.SyncServiceFactory
import ua.com.expert.validator.utils.NetworkUtils
import ua.com.expert.validator.utils.SharedStorage


class UsbValidateViewModel(var mContext: Context) : ViewModel() {

    var syncService : SyncService = SyncServiceFactory.createService(SyncService::class.java, mContext)

    var validateResponse = MutableLiveData<ValidateEvent>()

    fun validate(request: ValidateRequest?){

        if(request == null || request!!.qrCode.isNullOrEmpty()){
            validateResponse.value = ValidateEvent(ValidateEvent.ERROR, ValidateResponse.Builder().error(mContext.getString(R.string.qr_code_empty)).build())
            validateResponse.value = null
            return
        }

        if(TextUtils.isEmpty(SharedStorage.getString(mContext, Consts.APP_SETTINGS_PREFS, SERVER, ""))){
            validateResponse.value = ValidateEvent(ValidateEvent.ERROR, ValidateResponse.Builder().error(mContext.getString(R.string.address_server_not_found)).build())
            validateResponse.value = null
            return
        }

        if(!NetworkUtils.checkEthernet(mContext)){
            validateResponse.value = ValidateEvent(ValidateEvent.ERROR, ValidateResponse.Builder().error(mContext.getString(R.string.error_internet_connecting)).build())
            validateResponse.value = null
            Log.e(TAGLOG, mContext.getString(R.string.error_internet_connecting))
            return
        }

        validateResponse.value = ValidateEvent(ValidateEvent.START)

        syncService.qrCodeValidate(
                SharedStorage.getString(mContext, Consts.APP_CASH_TOKEN_PREFS, Consts.TOKEN, "").toString(), request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ValidateResponse> {
                    override fun onComplete() {}

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(value: ValidateResponse) {
                        if (value == null) {
                            Log.e(TAGLOG, mContext.getString(R.string.error_no_data))
                            validateResponse.value = ValidateEvent(ValidateEvent.ERROR, ValidateResponse.Builder()
                                    .error(mContext.getString(R.string.error_retrieving_data))
                                    .build())
                            validateResponse.value = null
                            return
                        }

                        validateResponse.value = ValidateEvent(ValidateEvent.FINISH, value)
                        validateResponse.value = null
                    }

                    override fun onError(e: Throwable) {
                        if (!NetworkUtils.checkEthernet(mContext)) {
                            Log.e(TAGLOG, mContext.getString(R.string.error_internet_connecting))
                            validateResponse.value = ValidateEvent(ValidateEvent.ERROR,
                                    ValidateResponse.Builder()
                                            .error(mContext.getString(R.string.error_internet_connecting))
                                            .build())
                            validateResponse.value = null
                            return
                        }

                        try {
                            if ((e as HttpException).code() == 401) {
                                Log.e(TAGLOG, e.message)
                                validateResponse.value = ValidateEvent(ValidateEvent.ERROR,
                                        ValidateResponse.Builder()
                                                .error(mContext.getString(R.string.error_retrieving_data))
                                                .errorCode(401)
                                                .build())
                                validateResponse.value = null
                                return
                            }
                        } catch (exep: Exception) {
                            Log.e(TAGLOG, exep.message)
                        }

                        if (e != null) {
                            Log.e(TAGLOG, e.message)
                            validateResponse.value = ValidateEvent(ValidateEvent.ERROR,
                                    ValidateResponse.Builder()
                                            .error(mContext.getString(R.string.error_retrieving_data))
                                            .build())
                            validateResponse.value = null
                        }
                    }
                } )
    }

    class ViewModelFactory(private val mContext: Context): ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(UsbValidateViewModel::class.java!!)) {
                UsbValidateViewModel(this.mContext) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }

    }

}
