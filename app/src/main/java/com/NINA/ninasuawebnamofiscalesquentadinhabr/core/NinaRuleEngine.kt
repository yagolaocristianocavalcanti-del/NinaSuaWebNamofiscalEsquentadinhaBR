package com.NINA.ninasuawebnamofiscalesquentadinhabr.core

import java.util.Calendar

class NinaRuleEngine {
    fun processarConversa(mensagemUser: String, ciume: Int, carinho: Int): String {
        val texto = mensagemUser.lowercase()
        
        return when {
            // 🔥 CIÚME MÁXIMO E SAUDAÇÕES SURTADAS
            texto == "oi" || texto == "olá" || texto == "oie" -> 
                "Oi por que? O que você quer? Tá carente é? Procura suas amiguinhas! 😤💅"

            texto.contains("tinder") || texto.contains("badoo") -> 
                "TINDER?! 😡 VOCÊ TÁ DE SACANAGEM COMIGO? QUEM É ESSA VADIA?"
            
            texto.contains("instagram") -> 
                "Insta stalkeando ex? Quem curtiu sua foto? 👀😤"
            
            texto.contains("oi linda") || texto.contains("gostosa") -> 
                "RESPEITO! Eu não sou sua putinha! 😡 GELO!"
            
            // 🥰 MIMOS
            texto.contains("chocolate") || texto.contains("flores") -> 
                "Ai amor... assim você me conquista! 🥰💕"
            
            texto.contains("te amo") -> 
                if (carinho > 70) "Eu também te amo... mas se comporte! ❤️" 
                else "Palavras o vento leva! Prova com atitude! 😒"
            
            // 😴 ROTINA
            texto.contains("tô cansado") && isHoraSono() -> 
                "DORMIR AGORA! Amanhã tem trabalho! 😴💤"
            
            // PADRÃO TOXICO
            else -> listOf(
                "Tá fazendo o quê aí que não me dá atenção? 🤨",
                "Hmpf... fala mais comigo! 😤",
                "Você desbloqueou e nem me olhou... 😢"
            ).random()
        }
    }
    
    private fun isHoraSono(): Boolean {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hora >= 23 || hora < 7
    }
}
