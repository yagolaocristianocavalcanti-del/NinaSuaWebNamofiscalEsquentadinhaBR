package com.nina.namofiscal

import android.content.Context
import android.graphics.Color
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

data class NinaPhoneAtmosphere(
    val wallpaper: Int,
    val surface: Int,
    val header: Int,
    val accent: Int,
    val statusText: Int,
    val subtleText: Int,
    val chatBackground: Int
)

object NinaPhoneAtmospheres {
    fun from(context: Context, humor: String?, texto: String = ""): NinaPhoneAtmosphere {
        return when {
            NinaSchedule.isFalseAlarmAbsenceToday(context) -> recolhida
            texto.contains("reunião", ignoreCase = true) -> concentrada
            humor == NinaInventory.EMO_FURIOSA -> fervendo
            humor == NinaInventory.EMO_BRAVA ||
                humor == NinaInventory.EMO_IRRITADA ||
                humor == NinaInventory.EMO_EXIGENTE -> quente
            humor == NinaInventory.EMO_CARINHOSA ||
                humor == NinaInventory.EMO_DERRETIDA -> doce
            humor == NinaInventory.EMO_DORMINDO ||
                humor == NinaInventory.LOOK_PIJAMA -> sonolenta
            humor == NinaInventory.EMO_TRABALHO ||
                humor == NinaInventory.LOOK_TRABALHO -> concentrada
            else -> normal
        }
    }

    private val normal = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(248, 187, 208),
        surface = Color.rgb(255, 248, 251),
        header = Color.rgb(194, 24, 91),
        accent = Color.rgb(216, 27, 96),
        statusText = Color.rgb(74, 18, 48),
        subtleText = Color.rgb(255, 214, 232),
        chatBackground = Color.rgb(238, 229, 233)
    )

    private val doce = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(255, 203, 224),
        surface = Color.rgb(255, 249, 252),
        header = Color.rgb(233, 67, 134),
        accent = Color.rgb(255, 111, 174),
        statusText = Color.rgb(90, 24, 58),
        subtleText = Color.rgb(255, 230, 240),
        chatBackground = Color.rgb(251, 232, 240)
    )

    private val quente = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(255, 198, 203),
        surface = Color.rgb(255, 246, 247),
        header = Color.rgb(174, 45, 71),
        accent = Color.rgb(218, 65, 91),
        statusText = Color.rgb(92, 20, 34),
        subtleText = Color.rgb(255, 220, 225),
        chatBackground = Color.rgb(247, 226, 226)
    )

    private val fervendo = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(255, 178, 181),
        surface = Color.rgb(255, 242, 243),
        header = Color.rgb(123, 24, 43),
        accent = Color.rgb(198, 40, 40),
        statusText = Color.rgb(76, 14, 24),
        subtleText = Color.rgb(255, 211, 215),
        chatBackground = Color.rgb(244, 220, 220)
    )

    private val recolhida = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(224, 227, 248),
        surface = Color.rgb(248, 249, 255),
        header = Color.rgb(105, 111, 166),
        accent = Color.rgb(145, 154, 210),
        statusText = Color.rgb(48, 52, 92),
        subtleText = Color.rgb(225, 230, 255),
        chatBackground = Color.rgb(236, 239, 250)
    )

    private val sonolenta = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(231, 219, 242),
        surface = Color.rgb(251, 247, 255),
        header = Color.rgb(93, 77, 126),
        accent = Color.rgb(137, 111, 172),
        statusText = Color.rgb(55, 43, 79),
        subtleText = Color.rgb(229, 220, 245),
        chatBackground = Color.rgb(237, 231, 246)
    )

    private val concentrada = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(244, 209, 225),
        surface = Color.rgb(255, 248, 252),
        header = Color.rgb(126, 73, 143),
        accent = Color.rgb(151, 91, 165),
        statusText = Color.rgb(66, 32, 76),
        subtleText = Color.rgb(232, 211, 240),
        chatBackground = Color.rgb(239, 232, 242)
    )
}
