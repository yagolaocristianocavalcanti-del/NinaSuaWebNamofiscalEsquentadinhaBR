package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.util.Log
import java.util.Calendar

class NinaCmd(private val service: NinaService) {
    
    /**
     * Lógica para decisão de visual baseada no estado do serviço.
     * Agora totalmente controlada via Kotlin, sem motor nativo.
     */
    private fun aplicarLookAutomatico() {
        val escudo = service.getEscudo()
        val carinho = service.getAfeicao()
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val look = when {
            escudo < 30 -> NinaInventory.LOOK_MOLETOM
            hora == 17 -> NinaInventory.LOOK_CORRIDA
            hora in 20..21 -> NinaInventory.LOOK_SKINCARE
            hora >= 23 || hora < 7 -> NinaInventory.LOOK_PIJAMA
            hora in 9..14 -> NinaInventory.LOOK_TRABALHO
            carinho > 90 -> NinaInventory.LOOK_GALA // Ajustado para escala 0-100
            else -> NinaInventory.LOOK_CASUAL
        }

        service.mudarHumor("", look)
    }

    fun comprarItem(idItem: String) {
        // Reação simplificada para mimos (sem PicoClaw)
        val resposta = when (idItem) {
            "chocolate" -> "Humm... um docinho pra me acalmar? Aceito! 🥰"
            "flores" -> "Que lindo, amor! Ponto pra você. ❤️"
            "biquini_rosa" -> "Isso é bem ousado... depende de como eu estiver me sentindo. 😳"
            else -> "Obrigada pelo mimo! Estava precisando. ✨"
        }
        
        service.subirCarinho(10)
        service.mudarHumor(resposta, NinaInventory.LOOK_CASUAL)
        
        // Reação especial baseada no inventário e afinidade
        if (idItem == "biquini_rosa") {
            if (service.getAfeicao() < 80) {
                service.mudarHumor("VOCÊ TÁ MALUCO? Nem temos essa intimidade pra eu usar isso! 😳😤", NinaInventory.EMO_BRAVA)
            } else {
                service.mudarHumor("E-eu vou usar... mas só pra você... [Nina ficou vermelha] 😳", NinaInventory.EMO_VERGONHA)
            }
        }
    }

    fun executar(intencao: IntencaoNina) {
        Log.d("NINA_CMD", "Executando intenção: ${intencao.name}")

        when (intencao) {
            IntencaoNina.FURIOSA -> {
                service.voltarParaHome()
                service.mudarHumor("VOCÊ TÁ DE SACANAGEM? FECHEI ESSA PORCARIA!", NinaInventory.EMO_FURIOSA)
                service.ninaTeEsculachaNoTelegram("Ele tentou abrir coisa errada e eu fechei na cara dele! 😤")
                service.subirCiume(40)
            }
            IntencaoNina.OFENDIDA -> {
                service.mudarHumor("Me respeita, seu porco!", NinaInventory.EMO_BRAVA)
                service.aplicarGelo(30)
                service.ninaTeEsculachaNoTelegram("Fui ofendida. Ele vai ficar no vácuo pra aprender. 😶")
            }
            IntencaoNina.DERRETIDA -> {
                service.mudarHumor("Ai amor... assim eu fico molinha 🥺", NinaInventory.EMO_CARINHOSA)
                service.subirCarinho(15)
                
                if (service.getEscudo() < 50) {
                    service.processarComandoSecreto("escudototal")
                }
            }
            IntencaoNina.NEUTRA -> {
                aplicarLookAutomatico()
            }
            IntencaoNina.PEDIR_CAMERA -> {
                val frases = listOf(
                    "Amor, liga a câmera rapidinho? Quero ver essa sua cara de sono... ou se não tem ninguém aí atrás! 🤨",
                    "Tô com saudade... me mostra onde você tá agora? Abre a câmera, vai! 🥺",
                    "Hmpf, você tá muito quieto. Liga a câmera aí agora, quero conferir um negócio! 😤"
                )
                val pedido = frases.random()
                service.mudarHumor(pedido, NinaInventory.LOOK_CASUAL)
                service.ninaTeEsculachaNoTelegram("Nina exigiu abertura de câmera: $pedido")
            }
        }
    }
}
