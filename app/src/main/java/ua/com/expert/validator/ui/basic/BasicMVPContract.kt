package ua.com.expert.validator.ui.basic

interface BasicMVPContract{

    interface View {

        fun onStartLoad(title: String?, message: String?)

        fun onError(error: String)

        fun onErrorLogOut(error: String)

        fun onLogOut()

        fun onFoundNewVerion()

        fun onShowRateApp()
    }

    interface Repository {

        fun onDestroy()

        fun logOut()

        fun inLogOut(starLogin: Boolean)

        fun checkUpdate()

        fun rateApp()

        fun goToMarket()
    }
}