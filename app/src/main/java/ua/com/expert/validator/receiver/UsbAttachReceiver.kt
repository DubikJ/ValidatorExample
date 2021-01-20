package ua.com.expert.validator.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log


class UsbAttachReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("UsbAttachReceiver", "action: $action")
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            val intent = Intent(UsbActivityReceiver.ACTION_USB_STATE)
            intent.putExtra("connected", true)
            context.sendBroadcast(intent)
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            val intent = Intent(UsbActivityReceiver.ACTION_USB_STATE)
            intent.putExtra("connected", false)
            context.sendBroadcast(intent)
        }
    }
}