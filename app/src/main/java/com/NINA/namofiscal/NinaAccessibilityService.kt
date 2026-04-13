package com.nina.namofiscal

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NinaAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                Log.d("NINA_EYE", "App detectado: $packageName")
                
                // Notifica a Nina sobre o app aberto
                val ninaService = NinaLegalService.getInstance()
                if (ninaService != null) {
                    // Aqui você pode instanciar o NinaCmd se necessário, 
                    // ou passar o evento para o serviço processar
                    serviceScope.launch {
                        // Exemplo: NinaCmd(ninaService, applicationContext).reagirAutomaticamente(NinaEvento.AppAberto(packageName))
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
}
