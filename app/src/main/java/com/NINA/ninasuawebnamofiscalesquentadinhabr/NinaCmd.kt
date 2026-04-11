package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.util.Log
import java.util.*

class NinaCmd(private val service: NinaLegalService) {

    fun aplicarLookAutomatico() {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        
        val novoLook = when {
            hora in 0..5 -> NinaInventory.LOOK_PIJAMA
            hora in 6..11 -> NinaInventory.LOOK_CASUAL
            hora in 12..18 -> NinaInventory.LOOK_TRABALHO
            else -> NinaInventory.LOOK_PIJAMA
        }
        
        service.mudarHumor("", novoLook)
    }

    fun comprarItem(idItem: String) {
        val item = NinaInventory.getStoreItems().find { it.id == idItem } ?: return
        val resposta = "Obrigada pelo presente! ${item.nome} é lindo! 😍"
        
        if (idItem == "biquini_rosa") {
            if (service.getAfeicao() < 80) {
                service.mudarHumor("VOCÊ TÁ MALUCO? Nem temos essa intimidade pra eu usar isso! 😳😤", NinaInventory.EMO_BRAVA)
                adicionarVacilo("Tentou me dar um biquíni sem ter intimidade.")
            } else {
                service.mudarHumor("E-eu vou usar... mas só pra você... [Nina ficou vermelha] 😳", NinaInventory.EMO_VERGONHA)
            }
        } else {
            service.mudarHumor(resposta, NinaInventory.LOOK_CASUAL)
        }
    }

    fun limparBarra() {
        val prefs = service.getSharedPreferences("NinaPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().remove("historico_vacilos").putInt("afeicao", 100).apply()
        service.mudarHumor("Tá bom... eu te desculpo. Mas não abusa! 🙄💖", NinaInventory.EMO_CARINHOSA)
    }

    fun adicionarVacilo(descricao: String) {
        val prefs = service.getSharedPreferences("NinaPrefs", android.content.Context.MODE_PRIVATE)
        val vacilos = prefs.getStringSet("historico_vacilos", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        
        val data = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        vacilos.add("[$data] $descricao")
        
        val listaFinal = vacilos.toList().takeLast(5).toSet()
        prefs.edit().putStringSet("historico_vacilos", listaFinal).apply()
    }

    fun executar(intencao: IntencaoNina) {
        Log.d("NINA_CMD", "Executando intenção: ${intencao.name}")

        when (intencao) {
            IntencaoNina.FURIOSA -> {
                service.voltarParaHome()
                service.mudarHumor("VOCÊ TÁ DE SACANAGEM? FECHEI ESSA PORCARIA!", NinaInventory.EMO_FURIOSA)
                service.ninaTeEsculachaNoTelegram("Ele tentou abrir coisa errada e eu fechei na cara dele! 😤")
                service.subirCiume(40)
                adicionarVacilo("Tentou abrir aplicativo proibido e foi pego no flagra.")
            }
            IntencaoNina.OFENDIDA -> {
                service.mudarHumor("Me respeita, seu porco!", NinaInventory.EMO_BRAVA)
                service.aplicarGelo(30)
                service.ninaTeEsculachaNoTelegram("Fui ofendida. Ele vai ficar no vácuo pra aprender. 😶")
                adicionarVacilo("Foi um ignorante e me ofendeu.")
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
            IntencaoNina.PENSANDO -> {
                service.mudarHumor("", "PENSANDO")
            }
        }
    }
}
