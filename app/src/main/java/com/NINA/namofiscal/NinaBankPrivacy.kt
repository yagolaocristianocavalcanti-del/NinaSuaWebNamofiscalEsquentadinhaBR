package com.nina.namofiscal

import android.content.Context
import java.util.Calendar

data class NinaBankAccess(
    val openCount: Int,
    val shouldHideBalance: Boolean,
    val complaint: String
)

object NinaBankPrivacy {
    private const val PREFS_NAME = "NinaPrefs"
    private const val KEY_BANK_DAY = "nina_bank_day"
    private const val KEY_BANK_OPEN_COUNT = "nina_bank_open_count"
    private const val HIDE_BALANCE_AFTER_OPENS = 3

    fun registerOpen(context: Context): NinaBankAccess {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = bankDayKey(context)
        val savedDay = prefs.getString(KEY_BANK_DAY, "")
        val currentCount = if (savedDay == today) prefs.getInt(KEY_BANK_OPEN_COUNT, 0) else 0
        val nextCount = currentCount + 1

        prefs.edit()
            .putString(KEY_BANK_DAY, today)
            .putInt(KEY_BANK_OPEN_COUNT, nextCount)
            .apply()

        return NinaBankAccess(
            openCount = nextCount,
            shouldHideBalance = nextCount >= HIDE_BALANCE_AFTER_OPENS,
            complaint = complaintFor(nextCount)
        )
    }

    fun isBalanceHidden(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BANK_DAY, "") == bankDayKey(context) &&
            prefs.getInt(KEY_BANK_OPEN_COUNT, 0) >= HIDE_BALANCE_AFTER_OPENS
    }

    private fun complaintFor(openCount: Int): String {
        return when (openCount) {
            1 -> "Ei. Por que você abriu meu banco? Olha rápido e sai."
            2 -> "De novo olhando meu dinheiro? Tá fiscalizando minha vida agora?"
            else -> "Pronto. Escondi meu saldo. Curioso demais perde privilégio."
        }
    }

    private fun bankDayKey(context: Context): String {
        val now = NinaTime.now(context)
        return "${now.get(Calendar.YEAR)}-${now.get(Calendar.DAY_OF_YEAR)}"
    }
}
