package ua.com.expert.validator.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class UsbActivityReceiver(val callBackListener: CallBackListener) : BroadcastReceiver() {
    companion object {
        const val ACTION_USB_STATE = "ua.com.expertsolution.serviovalidator.USB_STATE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ACTION_USB_STATE.equals(action)) {
            if (intent.extras != null && intent.extras!!.getBoolean("connected")) {
                callBackListener.callBack(true)
            } else {
                callBackListener.callBack(false)
            }
        }
    }

    interface CallBackListener{
        fun callBack(connect: Boolean)
    }
}