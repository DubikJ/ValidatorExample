package ua.com.expert.validator.utils

import android.content.Context
import android.text.TextUtils
import ua.com.expert.validator.common.Consts.APP_CASH_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.NAME_TERMINAL
import ua.com.expert.validator.common.Consts.USER_FIRST_NAME
import ua.com.expert.validator.common.Consts.USER_LAST_NAME
import ua.com.expert.validator.common.Consts.USER_MIDDLE_NAME

object DataUtils {

    fun getUserName(context: Context): String {
        val lastName = SharedStorage.getString(context, APP_CASH_SETTINGS_PREFS, USER_LAST_NAME, "")
        val firstName = SharedStorage.getString(context, APP_CASH_SETTINGS_PREFS, USER_FIRST_NAME, "")
        val middleName = SharedStorage.getString(context, APP_CASH_SETTINGS_PREFS, USER_MIDDLE_NAME, "")
        return lastName + (if (TextUtils.isEmpty(firstName)) "" else " ") +
                firstName + (if (TextUtils.isEmpty(middleName)) "" else " ") +
                middleName
    }

    fun getNameTerminal(context: Context): String? {
        return SharedStorage.getString(context, APP_CASH_SETTINGS_PREFS, NAME_TERMINAL, "")
    }
}
