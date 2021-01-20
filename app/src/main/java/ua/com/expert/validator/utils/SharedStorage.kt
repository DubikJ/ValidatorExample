package ua.com.expert.validator.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ua.com.expert.validator.common.Consts.TAGLOG

@SuppressLint("LongLogTag")
object SharedStorage {

    private val MODE_STORAGE = Context.MODE_MULTI_PROCESS

    private fun getSharedPreferences(mContext: Context, type: String): SharedPreferences {
        return mContext.getSharedPreferences(type, MODE_STORAGE)
    }

    fun getString(mContext: Context, type: String, key: String, defValue: String?): String? {
        var result: String? = defValue
        try {
            result = getSharedPreferences(mContext, type).getString(key, defValue)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            Log.e(TAGLOG + "_SharedStorage", key + ": " + e.message)
        }

        return result
    }

    fun getBoolean(mContext: Context, type: String, key: String, defValue: Boolean): Boolean {
        var result = defValue
        try {
            result = getSharedPreferences(mContext, type).getBoolean(key, defValue)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            Log.e(TAGLOG + "_SharedStorage", key + ": " + e.message)
        }

        return result
    }

    fun getInteger(mContext: Context, type: String, key: String, defValue: Int): Int {
        var result = defValue
        try {
            result = getSharedPreferences(mContext, type).getInt(key, defValue)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            Log.e(TAGLOG + "_SharedStorage", key + ": " + e.message)
        }

        return result
    }

    fun getLong(mContext: Context, type: String, key: String, defValue: Long): Long {
        var result = defValue
        try {
            result = getSharedPreferences(mContext, type).getLong(key, defValue)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            Log.e(TAGLOG + "_SharedStorage", key + ": " + e.message)
        }

        return result
    }

    fun getDouble(mContext: Context, type: String, key: String, defValue: Double): Double {
        var result = defValue
        try {
            result = java.lang.Double.longBitsToDouble(getLong(mContext, type, key, java.lang.Double.doubleToLongBits(defValue)))
        } catch (e: ClassCastException) {
            e.printStackTrace()
            Log.e(TAGLOG + "_SharedStorage", key + ": " + e.message)
        }

        return result
    }

    fun setString(mContext: Context, type: String, key: String, value: String?) {
        val editor = getSharedPreferences(mContext, type).edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun setBoolean(mContext: Context, type: String, key: String, value: Boolean) {
        val editor = getSharedPreferences(mContext, type).edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun setInteger(mContext: Context, type: String, key: String, value: Int) {
        val editor = getSharedPreferences(mContext, type).edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun setLong(mContext: Context, type: String, key: String, value: Long) {
        val editor = getSharedPreferences(mContext, type).edit()
        editor.putLong(key, value)
        editor.commit()
    }

    fun setDouble(mContext: Context, type: String, key: String, value: Double) {
        val editor = getSharedPreferences(mContext, type).edit()
        editor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        editor.commit()
    }

    fun clearCache(mContext: Context, type: String) {
        val editor = getSharedPreferences(mContext, type).edit()
        editor.clear().commit()
    }
}
