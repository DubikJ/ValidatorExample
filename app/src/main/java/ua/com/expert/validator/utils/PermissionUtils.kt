package ua.com.expert.validator.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import ua.com.expert.validator.common.Consts.TAGLOG
import java.util.*

object PermissionUtils {
    fun checkPermissions(activity: Activity, requestPermissions: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ungrantedPermissions = requiredPermissionsStillNeeded(activity)
            return if (ungrantedPermissions.size == 0) {
                false
            } else {
                ActivityCompat.requestPermissions(activity, ungrantedPermissions, requestPermissions)
                true
            }
        }
        return false
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requiredPermissionsStillNeeded(mContext: Context): Array<String?> {
        val permissions: MutableSet<String?> = HashSet()
        for (permission in getRequiredPermissions(mContext)) {
            permissions.add(permission)
        }
        val i = permissions.iterator()
        while (i.hasNext()) {
            val permission = i.next()
            if (ContextCompat.checkSelfPermission(mContext, permission!!) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAGLOG, "Permission: $permission already granted.")
                i.remove()
            } else {
                Log.d(TAGLOG, "Permission: $permission not yet granted.")
            }
        }
        return permissions.toTypedArray()
    }

    fun getRequiredPermissions(mContext: Context): Array<String?> {
        var permissions: Array<String?>? = null
        try {
            permissions = mContext.packageManager.getPackageInfo(mContext.packageName,
                    PackageManager.GET_PERMISSIONS).requestedPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return permissions?.clone() ?: arrayOfNulls(0)
    }
}