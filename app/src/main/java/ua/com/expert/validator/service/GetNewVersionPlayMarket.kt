package ua.com.expert.validator.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.util.Log
import org.jsoup.Jsoup
import ua.com.expert.validator.BuildConfig.APPLICATION_ID
import ua.com.expert.validator.common.Consts.CONNECT_TIMEOUT_SECONDS_RETROFIT
import ua.com.expert.validator.common.Consts.TAGLOG
import java.io.IOException

class GetNewVersionPlayMarket(var mContext: Context, var callBackListener: CallBackListener) :
        AsyncTask<Void, String, Boolean>() {

    override fun doInBackground(vararg voids: Void): Boolean? {

        var marketVersion: String? = null
        var currentVersion = "0"
        var showMessage: Boolean? = false

        try {
            val document = Jsoup.connect("https://play.google.com/store/apps/details?id=$APPLICATION_ID&hl=en")
                    .timeout((CONNECT_TIMEOUT_SECONDS_RETROFIT * 1000).toInt())
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
            if (document != null) {
                val element = document!!.getElementsContainingOwnText("Current Version")
                for (ele in element) {
                    if (ele.siblingElements() != null) {
                        val sibElemets = ele.siblingElements()
                        for (sibElemet in sibElemets) {
                            marketVersion = sibElemet.text()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            currentVersion = mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (marketVersion != null && !marketVersion.isEmpty()) {
            try {
                val marketMassive = marketVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val currentMassive = currentVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in marketMassive.indices) {
                    if (Integer.valueOf(marketMassive[i]) > Integer.valueOf(currentMassive[i])) {
                        showMessage = true
                        break
                    }
                }
            } catch (e: Exception) {
            }

        }
        Log.d(TAGLOG, "Current version $currentVersion playstore version $marketVersion")

        return showMessage
    }


    override fun onPostExecute(showMessage: Boolean?) {
        super.onPostExecute(showMessage)

        if (showMessage!!) {
            if (callBackListener != null) {
                callBackListener!!.onFoundNewVerion()
            }
        } else {
            if (callBackListener != null) {
                callBackListener!!.onCancel()
            }
        }
    }

    interface CallBackListener {
        fun onFoundNewVerion()
        fun onCancel()
    }
}