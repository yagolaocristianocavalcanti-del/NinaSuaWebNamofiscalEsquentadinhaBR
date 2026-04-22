package com.nina.namofiscal

import android.content.Context
import android.graphics.Color

object NinaOverlayLooks {
    fun imageFor(context: Context? = null, humor: String, texto: String = ""): Int {
        val outing = context?.let { NinaSchedule.getActiveOuting(it) }
        return when {
            context?.let { NinaSchedule.isFalseAlarmAbsenceToday(it) } == true ->
                R.drawable.nina_overlay_sad
            texto.contains("sentida", ignoreCase = true) ||
                texto.contains("triste", ignoreCase = true) ||
                texto.contains("chorar", ignoreCase = true) -> R.drawable.nina_overlay_sad
            outing?.companion == NinaOutingCompanion.AMIGO -> R.drawable.nina_overlay_neutral
            outing?.companion == NinaOutingCompanion.AMIGAS -> R.drawable.nina_overlay_happy
            texto.contains("correndo", ignoreCase = true) ||
                humor == NinaInventory.EMO_CORRENDO ||
                humor == NinaInventory.LOOK_SPORT -> R.drawable.nina_overlay_running
            texto.contains("trabalh", ignoreCase = true) ||
                texto.contains("reunião", ignoreCase = true) ||
                texto.contains("reuniao", ignoreCase = true) ||
                humor == NinaInventory.EMO_TRABALHO ||
                humor == NinaInventory.LOOK_TRABALHO -> R.drawable.nina_overlay_work
            texto.contains("dorm", ignoreCase = true) ||
                humor == NinaInventory.EMO_DORMINDO ||
                humor == NinaInventory.LOOK_PIJAMA -> R.drawable.nina_pijama
            humor == NinaInventory.EMO_BRAVA ||
                humor == NinaInventory.EMO_IRRITADA ||
                humor == NinaInventory.EMO_EXIGENTE ||
                humor == NinaInventory.EMO_FURIOSA -> R.drawable.nina_overlay_angry
            humor == NinaInventory.EMO_CARINHOSA ||
                humor == NinaInventory.EMO_DERRETIDA -> R.drawable.nina_overlay_happy
            else -> R.drawable.nina_overlay_neutral
        }
    }
}

data class NinaPhoneAtmosphere(
    val wallpaper: Int,
    val surface: Int,
    val header: Int,
    val accent: Int,
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
        subtleText = Color.rgb(255, 214, 232),
        chatBackground = Color.rgb(238, 229, 233)
    )

    private val doce = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(255, 203, 224),
        surface = Color.rgb(255, 249, 252),
        header = Color.rgb(233, 67, 134),
        accent = Color.rgb(255, 111, 174),
        subtleText = Color.rgb(255, 230, 240),
        chatBackground = Color.rgb(251, 232, 240)
    )

    private val quente = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(255, 198, 203),
        surface = Color.rgb(255, 246, 247),
        header = Color.rgb(174, 45, 71),
        accent = Color.rgb(218, 65, 91),
        subtleText = Color.rgb(255, 220, 225),
        chatBackground = Color.rgb(247, 226, 226)
    )

    private val fervendo = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(255, 178, 181),
        surface = Color.rgb(255, 242, 243),
        header = Color.rgb(123, 24, 43),
        accent = Color.rgb(198, 40, 40),
        subtleText = Color.rgb(255, 211, 215),
        chatBackground = Color.rgb(244, 220, 220)
    )

    private val recolhida = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(224, 227, 248),
        surface = Color.rgb(248, 249, 255),
        header = Color.rgb(105, 111, 166),
        accent = Color.rgb(145, 154, 210),
        subtleText = Color.rgb(225, 230, 255),
        chatBackground = Color.rgb(236, 239, 250)
    )

    private val sonolenta = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(231, 219, 242),
        surface = Color.rgb(251, 247, 255),
        header = Color.rgb(93, 77, 126),
        accent = Color.rgb(137, 111, 172),
        subtleText = Color.rgb(229, 220, 245),
        chatBackground = Color.rgb(237, 231, 246)
    )

    private val concentrada = NinaPhoneAtmosphere(
        wallpaper = Color.rgb(244, 209, 225),
        surface = Color.rgb(255, 248, 252),
        header = Color.rgb(126, 73, 143),
        accent = Color.rgb(151, 91, 165),
        subtleText = Color.rgb(232, 211, 240),
        chatBackground = Color.rgb(239, 232, 242)
    )
}
