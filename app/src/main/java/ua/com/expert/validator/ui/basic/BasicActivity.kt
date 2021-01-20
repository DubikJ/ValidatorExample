package ua.com.expert.validator.ui.basic

import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_basic.*
import kotlinx.android.synthetic.main.app_bar_basic.*
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.APP_CASH_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.APP_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.MODE
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.common.Consts.TIME_USE_APP
import ua.com.expert.validator.receiver.UsbActivityReceiver
import ua.com.expert.validator.receiver.UsbActivityReceiver.Companion.ACTION_USB_STATE
import ua.com.expert.validator.service.LicenseAviability
import ua.com.expert.validator.ui.*
import ua.com.expert.validator.ui.FragmentDispatchKeyEvent
import ua.com.expert.validator.ui.FragmentNFCScanListener
import ua.com.expert.validator.ui.FragmentOnBackPressed
import ua.com.expert.validator.ui.FragmentStartScanListener
import ua.com.expert.validator.ui.halls.HallsFragment
import ua.com.expert.validator.ui.settings.SettingsFragment
import ua.com.expert.validator.ui.usbvalidate.UsbValidateFragment
import ua.com.expert.validator.utils.ActivityUtils
import ua.com.expert.validator.utils.DataUtils
import ua.com.expert.validator.utils.NFCUtils
import ua.com.expert.validator.utils.SharedStorage

class BasicActivity : AppCompatActivity(), BasicMVPContract.View {

    private var doubleBackToExitPressedOnce = false
    private var mToolBarNavigationListenerIsRegistered = false

    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private var mFragmentManager: FragmentManager = supportFragmentManager
    lateinit var selectedFragment: Fragment
    private lateinit var dialogLoad: ProgressDialog
    private lateinit var basicPresenter: BasicPresenter
    private lateinit var brLic: BroadcastReceiver
    private var mAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null
    var usbConnected = false
    private val mUsbReceiver: UsbActivityReceiver = UsbActivityReceiver(object : UsbActivityReceiver.CallBackListener {
        override fun callBack(connect: Boolean) {
            usbConnected = connect
            if (selectedFragment != null && selectedFragment is FragmentStartScanListener) {
                (selectedFragment as FragmentStartScanListener).startScan(connect)
            }
        }

    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)
        setSupportActionBar(toolbar)

        basicPresenter = BasicPresenter(this as Context, this)
        initNavigationView()
        initDialogLoad()
        initDefaultData()
        listenLic()
        if (savedInstanceState == null) {

            basicPresenter.rateApp()
            basicPresenter.checkUpdate()

            if (SharedStorage.getInteger(this,
                            APP_SETTINGS_PREFS, MODE, 0) === 0) {
                nav_view.menu.performIdentifierAction(R.id.nav_halls, 0)
            }else{
                nav_view.menu.performIdentifierAction(R.id.nav_usb_valid, 1)
            }

            Handler().postDelayed({ LicenseAviability(this).check() }, 300)
        }
    }

    private fun initNavigationView() {

        mDrawerToggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

        nav_view.setNavigationItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.nav_halls -> initSelectedFragment(HallsFragment.getInstance())
                R.id.nav_settings -> initSelectedFragment(SettingsFragment.getInstance(false))
                R.id.nav_usb_valid -> initSelectedFragment(UsbValidateFragment.getInstance())
                R.id.nav_exit -> logoutDialog()
            }

            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }

        setVisibilityNavigationViewItems()
    }

    fun setVisibilityNavigationViewItems() {
        if (SharedStorage.getInteger(this,
                        APP_SETTINGS_PREFS, MODE, 0) === 0) {
            nav_view.menu.getItem(0).isVisible = true
            nav_view.menu.getItem(1).isVisible = false
        } else {
            nav_view.menu.getItem(0).isVisible = false
            nav_view.menu.getItem(1).isVisible = true
        }
    }

    private fun initDefaultData() {

        val header = nav_view.getHeaderView(0)
        val nameUser = header.findViewById<View>(R.id.name_user)as TextView?
        nameUser!!.text = DataUtils.getUserName(this)
        val nameTerminal = header.findViewById<View>(R.id.name_terminal)as TextView?
        nameTerminal!!.text = DataUtils.getNameTerminal(this)

    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(selectedFragment!=null && selectedFragment is FragmentOnBackPressed){
            (selectedFragment as FragmentOnBackPressed).onBackPressed()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if(selectedFragment!=null && selectedFragment is FragmentDispatchKeyEvent){
            (selectedFragment as FragmentDispatchKeyEvent).dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onStartLoad(title: String?, message: String?) {
        showDialogLoad(title, message)
    }

    override fun onError(error: String) {
        cancelDialogLoad()
        ActivityUtils.showMessage(this@BasicActivity, null, null, error)
    }

    override fun onErrorLogOut(error: String) {
        cancelDialogLoad()
        ActivityUtils.showMessageWihtCallBack(this@BasicActivity,
                null, null, error, object : ActivityUtils.MessageCallBack {
            override fun onPressOk() {
                basicPresenter.inLogOut(true)
            }
        })
    }

    override fun onLogOut() {
        cancelDialogLoad()
        Handler().postDelayed({ finish() }, 500)
    }

    override fun onFoundNewVerion() {
        val textUpdate = SpannableString(getString(R.string.new_version_app))
        textUpdate.setSpan(UnderlineSpan(), 35, textUpdate.length - 1, 0)

        ActivityUtils.showSnackBar(this@BasicActivity, drawer_layout, 0,
                textUpdate.toString(), object : ActivityUtils.SnackBarCallBack {
            override fun onCallBack() {
                basicPresenter.goToMarket()
            }
        })
    }

    override fun onShowRateApp() {
        ActivityUtils.showQuestion(this@BasicActivity, getString(R.string.action_rate),
                null, getString(R.string.questions_rate),
                null, null, getString(R.string.questions_answer_late),
                object : ActivityUtils.QuestionAnswer {
                    override fun onPositiveAnswer() {

                        basicPresenter.goToMarket()

                        SharedStorage.setInteger(this@BasicActivity,
                                APP_CASH_SETTINGS_PREFS, TIME_USE_APP, -1)
                    }

                    override fun onNegativeAnswer() {
                        SharedStorage.setInteger(this@BasicActivity,
                                APP_CASH_SETTINGS_PREFS, TIME_USE_APP, -1)
                    }

                    override fun onNeutralAnswer() {
                        SharedStorage.setInteger(this@BasicActivity,
                                APP_CASH_SETTINGS_PREFS, TIME_USE_APP, 0)
                    }
                })
    }

    override fun onResume() {
        super.onResume()
        startUsbListener()
    }

    override fun onStop() {
        super.onStop()
        stopUsbListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        basicPresenter.onDestroy()
        unregisterReceiver(brLic)
    }

    fun inLogOut(starLogin: Boolean) {
        basicPresenter.inLogOut(starLogin)
    }

    private fun initDialogLoad() {
        dialogLoad = ProgressDialog(this)
        dialogLoad.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialogLoad.setMessage(getString(R.string.loading_title))
        dialogLoad.isIndeterminate = true
        dialogLoad.setCanceledOnTouchOutside(false)
    }

    fun showDialogLoad(title: String?, message: String?) {
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(message)) {
            dialogLoad.setTitle(title)
            dialogLoad.setMessage(message)
        } else if (!TextUtils.isEmpty(title)) {
            dialogLoad.setTitle(title)
            dialogLoad.setMessage("")
        } else {
            dialogLoad.setTitle("")
            dialogLoad.setMessage(getString(R.string.loading_title))
        }
        try {
            dialogLoad.show()
        } catch (e: Exception) {
            Log.e(TAGLOG, e.toString())
        }

    }

    fun cancelDialogLoad() {
        if (dialogLoad != null) {
            try {
                dialogLoad.cancel()
            } catch (e: Exception) {
            }

        }
    }

    private fun initSelectedFragment(newFragment: Fragment) {

        val ft = mFragmentManager.beginTransaction()
        selectedFragment = newFragment
        ft.replace(R.id.content_main_frame, selectedFragment).commitAllowingStateLoss()

    }

    fun showNextFragment(newFragment: Fragment) {

        val ft = mFragmentManager.beginTransaction()
        selectedFragment = newFragment
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        ft.replace(R.id.content_main_frame, selectedFragment)
        ft.addToBackStack(null)
        ft.commit()

    }

    fun showDownFragment(newFragment: Fragment) {

        val ft = mFragmentManager.beginTransaction()
        selectedFragment = newFragment
        ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
        ft.replace(R.id.content_main_frame, selectedFragment)
        ft.addToBackStack(null)
        ft.commit()

    }

    private fun logoutDialog() {

        ActivityUtils.showQuestion(
                this@BasicActivity,
                getString(R.string.action_exit),
                resources.getDrawable(R.drawable.ic_exit_white),
                getString(R.string.questions_exit), null, null,
                null, object : ActivityUtils.QuestionAnswer {
            override fun onPositiveAnswer() {
                basicPresenter.logOut()
            }

            override fun onNegativeAnswer() {}

            override fun onNeutralAnswer() {}
        })
    }

    private fun listenLic() {

        brLic = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val textError = intent.getStringExtra(LicenseAviability.BROADCAST_LIC_ERROR)
                if (!TextUtils.isEmpty(textError)) {
                    ActivityUtils.showMessageWihtCallBack(this@BasicActivity,
                            getString(R.string.action_activation), null,
                            textError, object : ActivityUtils.MessageCallBack {
                        override fun onPressOk() {
                            finish()
                        }
                    })
                }
            }
        }
        registerReceiver(brLic, IntentFilter(LicenseAviability.BROADCAST_LIC))

    }

    fun enableNavigationView(enable: Boolean, isClose: Boolean) {
        if (enable) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            mDrawerToggle.isDrawerIndicatorEnabled = true
            mDrawerToggle.toolbarNavigationClickListener = null
            mToolBarNavigationListenerIsRegistered = false
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            mDrawerToggle.isDrawerIndicatorEnabled = false
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            if (isClose) {
                supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close_white)
            } else {
                supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            }
            if (!mToolBarNavigationListenerIsRegistered) {
                mDrawerToggle.setToolbarNavigationClickListener { v ->
                  //  (selectedFragment as FragmentNavigationClickListener).onNavigationClick(v)
                    onBackPressed()
                }
                mToolBarNavigationListenerIsRegistered = true
            }
        }
    }

    fun startUsbListener() {
        if(!SharedStorage.getBoolean(this,
                        Consts.APP_SETTINGS_PREFS, Consts.BLOCK_AUDIO_VALIDATE, false)){
            val filter = IntentFilter(ACTION_USB_STATE)
            registerReceiver(mUsbReceiver, filter)
            try {
                val i = registerReceiver(null, IntentFilter("android.hardware.usb.action.USB_STATE"))
                usbConnected = i!!.extras!!.getBoolean("connected") || usbConnected
            }catch (e: java.lang.Exception){}
        }

    }

    fun stopUsbListener() {
        if(!SharedStorage.getBoolean(this,
                        Consts.APP_SETTINGS_PREFS, Consts.BLOCK_AUDIO_VALIDATE, false)){
            try {
                unregisterReceiver(mUsbReceiver)
            }catch (e:java.lang.Exception){}
        }

    }

    fun initNFCScan(){
        if (!NFCUtils.enabledNFC(this)) {
            ActivityUtils.showMessageWihtCallBack(this, null, null,
                    getString(R.string.nfc_disabled), object : ActivityUtils.MessageCallBack {
                override fun onPressOk() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                        startActivity(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(intent)
                    }
                }
            })
        }
        mAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mAdapter == null) {
            ActivityUtils.showMessageWihtCallBack(this, null, null,
                    getString(R.string.nfc_start_error), object : ActivityUtils.MessageCallBack {
                override fun onPressOk() {
                }
            })
            return
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, Intent(this,
                javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
    }

    fun startNFCScan(){
        mAdapter!!.enableForegroundDispatch(this, mPendingIntent, null, null)
    }

    fun stopNFCScan(){
        if (mAdapter != null) {
            mAdapter!!.disableForegroundDispatch(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        resolveIntent(intent)
    }

    private fun resolveIntent(intent: Intent) {
        val msgs: Array<NdefMessage?>
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMsgs != null) {
                msgs = arrayOfNulls(rawMsgs.size)
                for (i in rawMsgs.indices) {
                    msgs[i] = rawMsgs[i] as NdefMessage
                }
            } else {
                val tag = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as Tag
                        ?: return
                val code = NFCUtils.toDec(tag.id).toString()
                if(selectedFragment is FragmentNFCScanListener){
                    (selectedFragment as FragmentNFCScanListener).onFound(code)
                }
            }
        }
    }
}