package vn.com.nghiemduong.lockpattern.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object PrefUtils {
    private const val PASSWORD_PATTERN = "PASSWORD_PATTERN"
    private var sharedPreferences: SharedPreferences? = null

    private fun newInstance(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            return context.getSharedPreferences("LOCK_PATTERN", Activity.MODE_PRIVATE)
        }

        return sharedPreferences!!
    }

    fun saveLockPattern(password: String, context: Context) {
        val pref = newInstance(context)
        pref.edit().putString(PASSWORD_PATTERN, password).apply()
    }

    fun fetchLockPattern(context: Context): String {
        val pref = newInstance(context)
        return pref.getString(PASSWORD_PATTERN, "").toString()
    }
}
