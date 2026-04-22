package com.nina.namofiscal

import android.content.Context
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object NinaTime {
    private const val PREFS_NAME = "NinaPrefs"
    private const val KEY_ANCHOR_REAL_TIME = "nina_time_anchor_real_time"
    private const val KEY_ANCHOR_NINA_TIME = "nina_time_anchor_nina_time"
    const val DAYS_PER_REAL_DAY = 4

    fun now(context: Context): Calendar {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val realNow = System.currentTimeMillis()
        val anchorReal = prefs.getLong(KEY_ANCHOR_REAL_TIME, 0L)
        val anchorNina = prefs.getLong(KEY_ANCHOR_NINA_TIME, 0L)

        if (anchorReal == 0L || anchorNina == 0L) {
            prefs.edit()
                .putLong(KEY_ANCHOR_REAL_TIME, realNow)
                .putLong(KEY_ANCHOR_NINA_TIME, realNow)
                .apply()
            return Calendar.getInstance()
        }

        val ninaNow = anchorNina + ((realNow - anchorReal) * DAYS_PER_REAL_DAY)
        return Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = ninaNow
        }
    }

    fun describeScale(): String {
        return "No tempo da Nina, 1 dia real passa como $DAYS_PER_REAL_DAY dias para ela."
    }

    fun phoneClock(context: Context): String {
        val now = now(context)
        return String.format(
            Locale("pt", "BR"),
            "%02d:%02d",
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE)
        )
    }
}
