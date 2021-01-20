package ua.com.expert.validator.ui.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.layout_fingerprinter.view.*
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts.APP_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.DONT_ASK_USING_FINGEPRINT
import ua.com.expert.validator.common.Consts.MAX_PASS_LENGTH
import ua.com.expert.validator.common.Consts.TAGLOG_FINGERPRINT
import ua.com.expert.validator.common.Consts.USER_PASS
import ua.com.expert.validator.common.Consts.USE_FINGEPRINT
import ua.com.expert.validator.model.dto.AuthRequest
import ua.com.expert.validator.model.dto.AuthResponse
import ua.com.expert.validator.service.FingerprintHelper
import ua.com.expert.validator.service.FingerprintHelper.CallBackListener
import ua.com.expert.validator.ui.basic.BasicActivity
import ua.com.expert.validator.ui.login.LoginViewModel.ViewModelFactory
import ua.com.expert.validator.ui.settings.SettingsFragment
import ua.com.expert.validator.ui.welcome.WelcomeActivity
import ua.com.expert.validator.utils.ActivityUtils
import ua.com.expert.validator.utils.CryptoUtils
import ua.com.expert.validator.utils.FingerprintUtils
import ua.com.expert.validator.utils.SharedStorage
import javax.crypto.Cipher

class LoginFragment : Fragment(), View.OnClickListener  {
    private var FRAGMENT_LOGIN_PASS = "fragment_login_pass"
    private val LAYOUT = R.layout.fragment_login
    private lateinit var viewModel: LoginViewModel

    private var dialogFingerPrint: AlertDialog? = null
    private var mFingerprintHelper: FingerprintHelper? = null
    private var measuredWidth: Int = 0
    private var fingerPrinterTV: TextView? = null

    companion object {
        fun getInstance(): LoginFragment {
            val args = Bundle()
            val fragment = LoginFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(LAYOUT, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this,
                ViewModelFactory(activity as Context))
                .get(LoginViewModel::class.java)
        viewModel.authRespons.observe(this, Observer<AuthResponse> { t ->
            if(t!=null) {
                if (TextUtils.isEmpty(t.error)) {
                    hideAnimationLoad(false, "")
                } else {
                    hideAnimationLoad(true, t.error!!)
                }
            }else{
                hideAnimationLoad(true, resources.getString(R.string.error_retrieving_data))
            }})

        initStartData(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn0.setOnClickListener(this)
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
        btn3.setOnClickListener(this)
        btn4.setOnClickListener(this)
        btn5.setOnClickListener(this)
        btn6.setOnClickListener(this)
        btn7.setOnClickListener(this)
        btn8.setOnClickListener(this)
        btn9.setOnClickListener(this)
        btnClear.setOnClickListener(this)
        btnEnter.setOnClickListener(this)

        image_settings.setOnClickListener(this)
        password_show.setOnClickListener(this)

        btnFingerPrint.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn0 -> addSymbol("0")
            R.id.btn1 -> addSymbol("1")
            R.id.btn2 -> addSymbol("2")
            R.id.btn3 -> addSymbol("3")
            R.id.btn4 -> addSymbol("4")
            R.id.btn5 -> addSymbol("5")
            R.id.btn6 -> addSymbol("6")
            R.id.btn7 -> addSymbol("7")
            R.id.btn8 -> addSymbol("8")
            R.id.btn9 -> addSymbol("9")
            R.id.btnClear -> tv_password.text =""
            R.id.btnEnter -> {
                var showDialog = false
                if (!SharedStorage.getBoolean(activity as Context, APP_SETTINGS_PREFS, DONT_ASK_USING_FINGEPRINT, false)) {
                    SharedStorage.setBoolean(activity as Context, APP_SETTINGS_PREFS, DONT_ASK_USING_FINGEPRINT, true)
                    showDialog = true
                }
                if (FingerprintUtils.checkSensorState(activity as Context) === FingerprintUtils.mSensorState.READY &&
                        (SharedStorage.getBoolean(activity as Context, APP_SETTINGS_PREFS, USE_FINGEPRINT, true) &&
                                TextUtils.isEmpty(SharedStorage.getString(activity as Context, APP_SETTINGS_PREFS, USER_PASS, null))
                                || showDialog)) {
                    showFingerPrintMessage(activity!!.getString(R.string.touch_to_scanner_for_confirm))
                } else {
                    showAnimLoad()
                }
            }
            R.id.image_settings -> {
                viewModel.authRespons.removeObservers(this)
                (activity as WelcomeActivity).showDownFragment(SettingsFragment.getInstance(true))
            }
            R.id.password_show -> {
                password_show.isSelected = !password_show.isSelected
                if (password_show.isSelected) {
                    tv_password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    tv_password.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
            R.id.btnFingerPrint -> showFingerPrintMessage(activity!!.getString(R.string.touch_to_scanner))
        }
    }

    override fun onResume() {
        super.onResume()
        initFingerPrint()
        if(FingerprintUtils.checkSensorState(activity as Context) == FingerprintUtils.mSensorState.READY &&
                SharedStorage.getBoolean(activity as Context, APP_SETTINGS_PREFS, USE_FINGEPRINT, true) &&
                !TextUtils.isEmpty(SharedStorage.getString(activity as Context, APP_SETTINGS_PREFS, USER_PASS, null))){
            btnFingerPrint.visibility = View.VISIBLE
            showFingerPrintMessage(resources.getString(R.string.touch_to_scanner))
        }else{
            btnFingerPrint.visibility = View.INVISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        dialogFingerPrint?.dismiss()
        mFingerprintHelper?.cancel()
    }


    override fun onSaveInstanceState(savedInstanceState : Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        if(tv_password!=null && tv_password.text!=null) {
            savedInstanceState.putString(FRAGMENT_LOGIN_PASS, tv_password.text.toString())
        }
    }

    private fun initStartData(savedInstanceState : Bundle?){

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FRAGMENT_LOGIN_PASS)) {
                addSymbol(savedInstanceState.getString(FRAGMENT_LOGIN_PASS))
            }
        }
    }

    private fun addSymbol(value: String?) {
        val passOld = tv_password.text.toString()
        if (passOld.length >= MAX_PASS_LENGTH) return
        tv_password.text = passOld + value!!
    }

    private fun showProgressDialog() {
        progress_bar.alpha = 1f
        progress_bar
                .indeterminateDrawable
                .setColorFilter(resources.getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN)
        progress_bar.visibility = View.VISIBLE
    }

    private fun fadeOutTextAndShowProgressDialog() {
        tv_password.visibility = View.GONE
        btnEnter.visibility = View.GONE
        password_show.visibility = View.GONE
        tv_password.animate().alpha(0f)
                .setDuration(250)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        layout_password.setBackgroundColor(resources.getColor(R.color.colorTransparent))
                        showProgressDialog()
                    }
                })
                .start()
    }

    private fun showFingerPrintMessage(textMessage: String) {

        val builder = AlertDialog.Builder(activity, R.style.WhiteDialogTheme)

        val layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        builder.setTitle(activity!!.getString(R.string.use_fingerprint))

        val viewTitle = layoutInflater.inflate(R.layout.dialog_title, null)
        viewTitle.text_title.text = activity!!.getString(R.string.use_fingerprint)
        viewTitle.image_title.visibility = View.GONE
        builder.setCustomTitle(viewTitle)

        val viewMessage = layoutInflater.inflate(R.layout.layout_fingerprinter, null)
        fingerPrinterTV = viewMessage.text_fingerprinter
        fingerPrinterTV?.text = textMessage
        builder.setView(viewMessage)

        builder.setNegativeButton(activity!!.getString(R.string.questions_answer_cancel)) { dialog, _ -> dialog.dismiss() }

        builder.setNeutralButton(activity!!.getString(R.string.questions_answer_not_use)) { dialog, _ ->
            dialog.dismiss()
            SharedStorage.setBoolean(activity as Context, APP_SETTINGS_PREFS, USE_FINGEPRINT, false)
        }

        builder.setOnCancelListener {
            mFingerprintHelper?.cancel()
        }

        dialogFingerPrint = builder.create()
        dialogFingerPrint?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFingerPrint?.show()

        val button1 = dialogFingerPrint?.findViewById(android.R.id.button1) as Button
        button1.setTextColor(activity!!.resources.getColor(R.color.colorAccent))

        mFingerprintHelper?.startAuth()

    }

    private fun showAnimLoad() {
        measuredWidth = layout_password.measuredWidth
        val anim = ValueAnimator.ofInt(measuredWidth, progress_bar.measuredWidth)
        anim.addUpdateListener { valueAnimator ->
            layout_password.layoutParams.width = valueAnimator.animatedValue as Int
            layout_password.requestLayout()
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                startAuth()
            }
        })
        anim.duration = 250
        anim.start()


        fadeOutTextAndShowProgressDialog()
    }

    private fun hideAnimationLoad(isError: Boolean, text: String ) {

        if(isError){

            var anim : ValueAnimator  = ValueAnimator.ofInt(progress_bar.measuredWidth, if(measuredWidth==0) 500 else measuredWidth)

            anim.addUpdateListener{ valueAnimator ->
                layout_password.layoutParams.width = valueAnimator.animatedValue as Int
                layout_password.requestLayout()
            }
            anim.duration = 250
            anim.start()

            progress_bar.animate().alpha(0f).setDuration(200).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    tv_password.visibility = View.VISIBLE
                    tv_password.animate().alpha(1f)
                            .setDuration(250)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    ActivityUtils.showMessage(activity as Context, null,
                                            null, text)
                                    try {
                                        layout_password.background = resources.getDrawable(R.drawable.shape_edit_pass_white)
                                        password_show.visibility = View.VISIBLE
                                        btnEnter.visibility = View.VISIBLE
                                    }catch (e: Exception){}
                                }
                            }).start()
                }
            }).start()
        }else{
            startActivity(Intent(activity, BasicActivity::class.java))
            Handler().postDelayed({ activity?.finish() }, 2000)
        }
    }

    private fun startAuth(){
        viewModel.auth(
                AuthRequest(tv_password.text.toString(),
                        Settings.Secure.getString(activity!!.contentResolver,
                                Settings.Secure.ANDROID_ID)))
    }

    private fun initFingerPrint(){

        if(FingerprintUtils.isSensorStateAt(FingerprintUtils.mSensorState.READY, activity as Context)){
            mFingerprintHelper = FingerprintHelper(activity as Context, object : CallBackListener {
                override fun onStartAuth(started: Boolean) {
                    if (!started) {
                        dialogFingerPrint?.dismiss()
                        ActivityUtils.showShortToast(activity as Context, getString(R.string.error_start_fingerprint))
                    }
                }

                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
                    Log.e(TAGLOG_FINGERPRINT, errString.toString())
                    fingerPrinterTV?.text = resources.getString(R.string.re_scan_fingerprint)
                }

                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
                    Log.e(TAGLOG_FINGERPRINT, helpString.toString())
                    fingerPrinterTV?.text = resources.getString(R.string.re_scan_fingerprint)
                }

                override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                    Log.e(TAGLOG_FINGERPRINT, "succeeded")

                    dialogFingerPrint?.dismiss()

                    if (TextUtils.isEmpty(SharedStorage.getString(activity as Context,
                                    APP_SETTINGS_PREFS, USER_PASS, null))) {
                        Log.e(TAGLOG_FINGERPRINT, "write new")
                        var encoded: String? = CryptoUtils.encode(tv_password.text.toString())
                        SharedStorage.setString(activity as Context, APP_SETTINGS_PREFS, USER_PASS, encoded)
                        showAnimLoad()
                    } else {
                        Log.e(TAGLOG_FINGERPRINT, "read old")
                        var cipher: Cipher? = result?.cryptoObject?.cipher
                        var encoded: String? = SharedStorage
                                .getString(activity as Context, APP_SETTINGS_PREFS, USER_PASS, null)
                        var clearPass = false
                        if (encoded != null && cipher != null) {
                            var decoded: String? = CryptoUtils.decode(encoded, cipher)
                            if (!TextUtils.isEmpty(decoded)) {
                                tv_password.text = decoded
                                showAnimLoad()
                            } else {
                                clearPass = true
                            }
                        } else {
                            clearPass = true
                        }
                        if (clearPass) {
                            SharedStorage.setString(activity as Context,
                                    APP_SETTINGS_PREFS, USER_PASS, null)
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    Log.e(TAGLOG_FINGERPRINT, "failed")
                    fingerPrinterTV?.text = resources.getString(R.string.re_scan_fingerprint)
                }

            })
        }
    }
}
