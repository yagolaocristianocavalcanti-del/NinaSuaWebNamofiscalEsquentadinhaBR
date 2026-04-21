package com.nina.namofiscal

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NinaAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ninaCmd: NinaCmd? = null
    private var ultimoPacoteDetectado: String? = null
    private var ultimaDeteccaoMs: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val pacoteAberto = event.packageName?.toString() ?: return
                if (pacoteAberto == applicationContext.packageName) return

                val agora = System.currentTimeMillis()
                if (pacoteAberto == ultimoPacoteDetectado && agora - ultimaDeteccaoMs < 2500) {
                    return
                }

                ultimoPacoteDetectado = pacoteAberto
                ultimaDeteccaoMs = agora

                Log.d("NINA_EYE", "App detectado: $pacoteAberto")
                
                val ninaService = NinaLegalService.getInstance()
                if (ninaService != null) {
                    val cmd = ninaCmd ?: NinaCmd(ninaService, applicationContext).also {
                        ninaCmd = it
                    }
                    serviceScope.launch {
                        cmd.reagirAutomaticamente(NinaEvento.AppAberto(pacoteAberto))
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.e("NINA_EYE", "Acessibilidade interrompida!")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("NINA_EYE", "O Olho da Nina foi ativado com sucesso! 👁️🚩")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
