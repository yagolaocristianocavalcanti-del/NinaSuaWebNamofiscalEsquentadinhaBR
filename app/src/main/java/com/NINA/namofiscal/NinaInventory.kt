package com.nina.namofiscal

import com.nina.namofiscal.R

data class NinaItem(val id: String, val nome: String, val preco: Int, val intimidadeMinima: Int)

object NinaInventory {
    // Emoções e Looks (Strings para facilitar logs e lógica)
    const val EMO_NEUTRA = "neutra"
    const val EMO_BRAVA = "brava"
    const val EMO_FURIOSA = "furiosa"
    const val EMO_CARINHOSA = "carinhosa"
    const val EMO_DORMINDO = "dormindo"
    const val EMO_CORRENDO = "correndo"
    const val EMO_TRABALHO = "trabalho"
    const val EMO_PENSANDO = "pensando"
    const val EMO_IRRITADA = "irritada"
    const val EMO_EXIGENTE = "exigente"
    const val EMO_DERRETIDA = "derretida"

    const val LOOK_CASUAL = "casual"
    const val LOOK_PIJAMA = "pijama"
    const val LOOK_TRABALHO = "trabalho_look"
    const val LOOK_SPORT = "sport"
    const val LOOK_BIQUINI = "biquini"

    // Mapeamento de Imagens
    fun getResourceForHumor(humor: String): Int {
        return when (humor) {
            EMO_BRAVA, EMO_IRRITADA, EMO_EXIGENTE -> R.drawable.nina_brava
            EMO_FURIOSA -> R.drawable.nina_brava // Poderia ser uma ainda pior
            LOOK_PIJAMA, EMO_DORMINDO -> R.drawable.nina_pijama
            else -> R.drawable.nina_seria
        }
    }

    fun getStoreItems() = listOf(
        NinaItem("chocolate", "Caixa de Bombom", 30, 0),
        NinaItem("flores", "Buquê de Flores", 150, 20),
        NinaItem("biquini_rosa", "Biquíni Rosa", 500, 80),
        NinaItem("alianca", "Aliança de Compromisso", 2000, 95)
    )
}
