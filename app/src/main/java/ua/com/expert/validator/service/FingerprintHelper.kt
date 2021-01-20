package ua.com.expert.validator.service

import android.content.Context
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.util.Log
import ua.com.expert.validator.common.Consts.APP_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.TAGLOG_FINGERPRINT
import ua.com.expert.validator.common.Consts.USER_PASS
import ua.com.expert.validator.utils.CryptoUtils
import ua.com.expert.validator.utils.FingerprintUtils
import ua.com.expert.validator.utils.SharedStorage

class FingerprintHelper (val mContext: Context, var callBackListener: CallBackListener): FingerprintManagerCompat.AuthenticationCallback() {

        private var mCancellationSignal: CancellationSignal? = null

        fun startAuth() {

            if (FingerprintUtils.isSensorStateAt(FingerprintUtils.mSensorState.READY, mContext)) {
                val cryptoObject = CryptoUtils.cryptoObject
                if (cryptoObject != null) {
                    mCancellationSignal = CancellationSignal()
                    val manager = FingerprintManagerCompat.from(mContext)
                    manager.authenticate(cryptoObject, 0, mCancellationSignal, this, null)
                    callBackListener?.onStartAuth(true)
                } else {
                    SharedStorage.setString(mContext, APP_SETTINGS_PREFS, USER_PASS, "")
                    callBackListener?.onStartAuth(false)
                }
            }

        }

        fun cancel() {
            mCancellationSignal?.cancel()
        }

        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            Log.e(TAGLOG_FINGERPRINT, errString!!.toString())
            callBackListener?.onAuthenticationError(errMsgId, errString)
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
            Log.e(TAGLOG_FINGERPRINT, helpString!!.toString())
            callBackListener?.onAuthenticationHelp(helpMsgId, helpString)
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            Log.e(TAGLOG_FINGERPRINT, "succeeded")
            callBackListener!!.onAuthenticationSucceeded(result)

        }

        override fun onAuthenticationFailed() {
            Log.e(TAGLOG_FINGERPRINT, "failed")
            callBackListener?.onAuthenticationFailed()
        }

        interface CallBackListener {

            fun onStartAuth(started: Boolean)

            fun onAuthenticationError(errMsgId: Int, errString: CharSequence)

            fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence)

            fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?)

            fun onAuthenticationFailed()
        }

    }