package com.nina.namofiscal

import androidx.annotation.DrawableRes

enum class NinaStatusKey {
    EM_CASA,
    DE_FOLGA,
    TRABALHANDO,
    EM_REUNIAO,
    ALMOCANDO,
    SAINDO_COM_AMIGAS,
    SAINDO_COM_AMIGO,
    CORRENDO,
    DORMINDO,
    PENSANDO,
    CARINHOSA,
    IRRITADA,
    FURIOSA,
    LOOK_CASUAL,
    LOOK_PIJAMA,
    LOOK_TRABALHO,
    LOOK_SPORT,
    LOOK_BIQUINI
}

data class NinaVisualStatus(
    val key: NinaStatusKey,
    val label: String,
    val detail: String,
    @DrawableRes val imageRes: Int
)

object NinaVisualStatuses {
    fun fromHumor(humor: String?, texto: String = ""): NinaVisualStatus {
        val key = when {
            texto.contains("folga", ignoreCase = true) -> NinaStatusKey.DE_FOLGA
            texto.contains("reunião", ignoreCase = true) -> NinaStatusKey.EM_REUNIAO
            texto.contains("almo", ignoreCase = true) -> NinaStatusKey.ALMOCANDO
            texto.contains("meninas", ignoreCase = true) ||
                texto.contains("amigas", ignoreCase = true) -> NinaStatusKey.SAINDO_COM_AMIGAS
            texto.contains("com um amigo", ignoreCase = true) -> NinaStatusKey.SAINDO_COM_AMIGO
            humor == NinaInventory.EMO_DORMINDO -> NinaStatusKey.DORMINDO
            humor == NinaInventory.EMO_CORRENDO -> NinaStatusKey.CORRENDO
            humor == NinaInventory.EMO_TRABALHO -> NinaStatusKey.TRABALHANDO
            humor == NinaInventory.EMO_PENSANDO -> NinaStatusKey.PENSANDO
            humor == NinaInventory.EMO_FURIOSA -> NinaStatusKey.FURIOSA
            humor == NinaInventory.EMO_BRAVA ||
                humor == NinaInventory.EMO_IRRITADA ||
                humor == NinaInventory.EMO_EXIGENTE -> NinaStatusKey.IRRITADA
            humor == NinaInventory.EMO_CARINHOSA ||
                humor == NinaInventory.EMO_DERRETIDA -> NinaStatusKey.CARINHOSA
            humor == NinaInventory.LOOK_PIJAMA -> NinaStatusKey.LOOK_PIJAMA
            humor == NinaInventory.LOOK_TRABALHO -> NinaStatusKey.LOOK_TRABALHO
            humor == NinaInventory.LOOK_SPORT -> NinaStatusKey.LOOK_SPORT
            humor == NinaInventory.LOOK_BIQUINI -> NinaStatusKey.LOOK_BIQUINI
            humor == NinaInventory.LOOK_CASUAL -> NinaStatusKey.LOOK_CASUAL
            else -> NinaStatusKey.EM_CASA
        }

        return get(key)
    }

    fun get(key: NinaStatusKey): NinaVisualStatus {
        return when (key) {
            NinaStatusKey.EM_CASA -> status(key, "Em casa", "Disponível quando ela quiser.", R.drawable.nina_seria)
            NinaStatusKey.DE_FOLGA -> status(key, "De folga", "Hoje ela decide o ritmo dela.", R.drawable.nina_seria)
            NinaStatusKey.TRABALHANDO -> status(key, "Trabalhando", "Pode responder, mas não é garantido.", R.drawable.nina_seria)
            NinaStatusKey.EM_REUNIAO -> status(key, "Em reunião", "Ela não responde além do aviso padrão.", R.drawable.nina_seria)
            NinaStatusKey.ALMOCANDO -> status(key, "Almoçando", "Responde curto quando está de bom humor.", R.drawable.nina_seria)
            NinaStatusKey.SAINDO_COM_AMIGAS -> status(key, "Com amigas", "Responde menos porque saiu na folga.", R.drawable.nina_seria)
            NinaStatusKey.SAINDO_COM_AMIGO -> status(key, "Com amigo", "Responde desconfiada e fica se explicando.", R.drawable.nina_seria)
            NinaStatusKey.CORRENDO -> status(key, "Correndo", "Sem conversa normal durante a corrida.", R.drawable.nina_seria)
            NinaStatusKey.DORMINDO -> status(key, "Dormindo", "Só acorda com muita afinidade.", R.drawable.nina_pijama)
            NinaStatusKey.PENSANDO -> status(key, "Pensando", "A Nina está processando a resposta.", R.drawable.nina_seria)
            NinaStatusKey.CARINHOSA -> status(key, "Carinhosa", "Mais doce, mas ainda mandona.", R.drawable.nina_seria)
            NinaStatusKey.IRRITADA -> status(key, "Irritada", "Melhor não provocar.", R.drawable.nina_brava)
            NinaStatusKey.FURIOSA -> status(key, "Furiosa", "Risco alto de gelo e vacilo.", R.drawable.nina_brava)
            NinaStatusKey.LOOK_CASUAL -> status(key, "Look casual", "Roupa comum para casa e fim do dia.", R.drawable.nina_seria)
            NinaStatusKey.LOOK_PIJAMA -> status(key, "Pijama", "Roupa de dormir ou madrugada.", R.drawable.nina_pijama)
            NinaStatusKey.LOOK_TRABALHO -> status(key, "Look trabalho", "Roupa apropriada para expediente.", R.drawable.nina_seria)
            NinaStatusKey.LOOK_SPORT -> status(key, "Look sport", "Roupa de corrida/treino.", R.drawable.nina_seria)
            NinaStatusKey.LOOK_BIQUINI -> status(key, "Biquíni", "Só em contexto que a Nina aceitar.", R.drawable.nina_seria)
        }
    }

    private fun status(
        key: NinaStatusKey,
        label: String,
        detail: String,
        @DrawableRes imageRes: Int
    ): NinaVisualStatus {
        return NinaVisualStatus(key, label, detail, imageRes)
    }
}
