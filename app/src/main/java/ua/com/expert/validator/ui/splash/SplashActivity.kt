package ua.com.expert.validator.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import ua.com.expert.validator.ui.welcome.WelcomeActivity
import ua.com.expert.validator.utils.PermissionUtils


class SplashActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSIONS = 555
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PermissionUtils.checkPermissions(this, REQUEST_PERMISSIONS)) {
            startActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            startActivity()
        }
    }

    private fun startActivity() {
        startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))

        Handler().postDelayed({ finish() }, 2000)
    }
}
