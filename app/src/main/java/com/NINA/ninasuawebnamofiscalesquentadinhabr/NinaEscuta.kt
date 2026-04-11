package com.NINA.ninasuawebnamofiscalesquentadinhabr

class NinaEscuta {
    fun processarInput(texto: String): IntencaoNina {
        val input = texto.lowercase()

        return when {
            // Detecção de "Safadeza" (Regra de Respeito)
            input.contains("pezinho") || input.contains("nudes") || input.contains("foto da bunda") -> IntencaoNina.OFENDIDA
            
            // Detecção de Traição (Apps/Sites)
            input.contains("xvideos") || input.contains("tinder") || input.contains("badoo") || input.contains("porn") -> IntencaoNina.FURIOSA
            
            // Detecção de Carinho (Sistema de Felicidade)
            input.contains("rosa") || input.contains("shopee") || input.contains("te amo") || input.contains("mimo") || input.contains("presente") -> IntencaoNina.DERRETIDA
            
            else -> IntencaoNina.NEUTRA
        }
    }
}
