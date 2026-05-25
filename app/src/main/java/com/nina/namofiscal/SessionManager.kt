package com.nina.namofiscal

import android.content.Context
import com.nina.namofiscal.model.UserRole

object SessionManager {
    private const val PREFS_NAME = "ParkingPrefs"
    private const val KEY_ROLE = "user_role"

    fun setRole(context: Context, role: UserRole) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROLE, role.name)
            .apply()
    }

    fun getRole(context: Context): UserRole {
        val roleName = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, UserRole.AUXILIAR.name)
        return UserRole.valueOf(roleName!!)
    }

    fun isMobile(context: Context): Boolean {
        // Lógica simplificada para fins de protótipo: assume true se for Android mobile
        return true
    }
}
