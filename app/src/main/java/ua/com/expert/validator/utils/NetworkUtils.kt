package ua.com.expert.validator.utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtils {

    fun checkEthernet(context: Context): Boolean {

        val conMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = conMgr.activeNetworkInfo

        return activeNetwork != null && activeNetwork.isConnected
    }

}