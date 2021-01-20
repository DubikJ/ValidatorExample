package ua.com.expert.validator.ui.welcome

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import ua.com.expert.validator.R
import ua.com.expert.validator.ui.FragmentNavigationClickListener
import ua.com.expert.validator.ui.login.LoginFragment

class WelcomeActivity : AppCompatActivity() {

    private var mFragmentManager: FragmentManager = supportFragmentManager
    private lateinit var selectedFragment: Fragment
    private lateinit var dialogLoad: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_activity)
        initDialogLoad()

        if (savedInstanceState == null) {
            selectedFragment = LoginFragment.getInstance()
            val ft = mFragmentManager.beginTransaction()
            ft.replace(R.id.container, selectedFragment).commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun initDialogLoad() {
        dialogLoad = ProgressDialog(this)
        dialogLoad.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialogLoad.setMessage(getString(R.string.loading_title))
        dialogLoad.isIndeterminate = true
        dialogLoad.setCanceledOnTouchOutside(false)
    }

    fun showDownFragment(newFragment: Fragment) {

        selectedFragment = newFragment
        val ft = mFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top)
        ft.replace(R.id.container, newFragment)
        ft.addToBackStack(null)
        ft.commit()

    }
}