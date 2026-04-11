package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object NinaInventory {
    // IDs das Roupas
    const val LOOK_CASUAL = "nina_casual_pink"
    const val LOOK_TRABALHO = "nina_office_style"
    const val LOOK_CORRIDA = "nina_fitness_fit"
    const val LOOK_GALA = "nina_zara_luxury"
    const val LOOK_PIJAMA = "nina_pijama_soft"
    const val LOOK_MOLETOM = "nina_moletom_brava"
    const val LOOK_BIQUINI = "nina_biquini_shy"
    const val LOOK_SKINCARE = "nina_skincare_mask"

    // IDs das Emoções (Expressões)
    const val EMO_FURIOSA = "nina_furiosa"
    const val EMO_BRAVA = "nina_moletom_brava"
    const val EMO_CARINHOSA = "nina_carinhosa"
    const val EMO_VERGONHA = "nina_vermelha_shame"

    // Fallback: Cria um drawable colorido se a imagem não existir
    fun getDrawableId(context: Context, lookId: String): Int {
        val resId = context.resources.getIdentifier(lookId, "drawable", context.packageName)
        return if (resId != 0) resId else android.R.drawable.ic_menu_gallery
    }
}
