package ua.com.expert.validator.utils

import android.content.Context
import android.text.TextUtils
import ua.com.expert.validator.common.Consts.APP_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.LENGTH_SCAN_CARD_CODE
import ua.com.expert.validator.common.Consts.POSTFIX_SCAN_CARD_CODE
import ua.com.expert.validator.common.Consts.PREFIX_SCAN_CARD_CODE
import ua.com.expert.validator.utils.SharedStorage.getInteger
import ua.com.expert.validator.utils.SharedStorage.getString

object ScanCardUtils {
    fun getCardCode(context: Context?, code: String): String {
        var code = code
        code += getString(context!!, APP_SETTINGS_PREFS, POSTFIX_SCAN_CARD_CODE, "")
        val preFix = getString(context, APP_SETTINGS_PREFS, PREFIX_SCAN_CARD_CODE, "")
        val lengthCode = getInteger(context, APP_SETTINGS_PREFS, LENGTH_SCAN_CARD_CODE, 0)
        if (!TextUtils.isEmpty(preFix)) {
            if (lengthCode > 0) {
                while (code.length < lengthCode) {
                    code = preFix + code
                }
            } else {
                code = preFix + code
            }
        }
        return code
    }
}