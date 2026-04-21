package com.nina.namofiscal

import com.nina.namofiscal.R

enum class NinaStoreApp(
    val titulo: String,
    val descricao: String
) {
    SHEIN("Shein", "roupas, looks e laços para a Nina escolher quando fizer sentido"),
    IFOOD("iFood", "comida, docinhos e agrados de humor imediato"),
    BOTICARIO("Boticário", "perfume, maquiagem e autocuidado"),
    PETLOVE("Petlove", "coisas fofas de pets que deixam ela molinha"),
    DOCERIA("Doceria", "chocolate, bolo e açúcar para comprar perdão"),
    FLORICULTURA("Flores", "flores e presentes românticos")
}

data class NinaItem(
    val id: String,
    val nome: String,
    val preco: Int,
    val intimidadeMinima: Int,
    val app: NinaStoreApp,
    val carinhoBonus: Int,
    val lookLiberado: String? = null
)

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

    fun getStoreApps(): List<NinaStoreApp> = NinaStoreApp.entries

    fun getStoreItems(app: NinaStoreApp? = null): List<NinaItem> = getStoreItems()
        .filter { app == null || it.app == app }

    private fun getStoreItems() = listOf(
        NinaItem("vestido_rosa", "Vestido Rosa", 220, 20, NinaStoreApp.SHEIN, 15, LOOK_CASUAL),
        NinaItem("laco_gigante", "Laço Rosa Gigante", 80, 0, NinaStoreApp.SHEIN, 10, LOOK_CASUAL),
        NinaItem("pijama_fofo", "Pijama Fofinho", 180, 35, NinaStoreApp.SHEIN, 18, LOOK_PIJAMA),
        NinaItem("biquini_rosa", "Biquíni Rosa", 500, 80, NinaStoreApp.SHEIN, 25, LOOK_BIQUINI),

        NinaItem("sushi", "Combo de Sushi", 95, 10, NinaStoreApp.IFOOD, 10),
        NinaItem("hamburguer", "Hambúrguer Artesanal", 45, 0, NinaStoreApp.IFOOD, 6),
        NinaItem("acai", "Açaí com Leite Condensado", 28, 0, NinaStoreApp.IFOOD, 8),

        NinaItem("perfume", "Perfume Doce", 180, 30, NinaStoreApp.BOTICARIO, 14),
        NinaItem("batom", "Batom Rosa", 55, 15, NinaStoreApp.BOTICARIO, 10),
        NinaItem("kit_skincare", "Kit Skincare", 260, 45, NinaStoreApp.BOTICARIO, 20),

        NinaItem("ursinho_pet", "Ursinho de Cachorrinho", 70, 0, NinaStoreApp.PETLOVE, 12),
        NinaItem("gatinho_pelucia", "Pelúcia de Gatinho", 90, 10, NinaStoreApp.PETLOVE, 14),

        NinaItem("chocolate", "Caixa de Bombom", 30, 0, NinaStoreApp.DOCERIA, 8),
        NinaItem("bolo_morango", "Bolo de Morango", 75, 5, NinaStoreApp.DOCERIA, 12),

        NinaItem("flores", "Buquê de Flores", 150, 20, NinaStoreApp.FLORICULTURA, 18),
        NinaItem("alianca", "Aliança de Compromisso", 2000, 95, NinaStoreApp.FLORICULTURA, 35)
    )
}
