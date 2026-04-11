package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.util.Log

class NinaHumor {
    var ciume = 0
    var carinho = 50
    
    fun subirCiume(pontos: Int) {
        ciume += pontos
        if (ciume >= 100) {
            // Protocolo término (legal!)
            Log.e("NINA", "CIÚME 100%! Nina terminou!")
        }
    }
    
    fun subirCarinho(pontos: Int) {
        carinho = (carinho + pontos).coerceAtMost(100)
    }
}
