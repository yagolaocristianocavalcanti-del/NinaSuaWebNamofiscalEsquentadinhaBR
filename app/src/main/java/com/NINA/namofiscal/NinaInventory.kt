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
    val lookLiberado: String? = null,
    val isIntimo: Boolean = false
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
        return NinaOverlayLooks.imageFor(humor = humor)
    }

    fun getStoreApps(): List<NinaStoreApp> = NinaStoreApp.entries

    fun getStoreItems(app: NinaStoreApp? = null): List<NinaItem> = getStoreItems()
        .filter { app == null || it.app == app }

    fun emojiFor(app: NinaStoreApp): String {
        return when (app) {
            NinaStoreApp.SHEIN -> "🛍️"
            NinaStoreApp.IFOOD -> "🍔"
            NinaStoreApp.BOTICARIO -> "💄"
            NinaStoreApp.PETLOVE -> "🐾"
            NinaStoreApp.DOCERIA -> "🍫"
            NinaStoreApp.FLORICULTURA -> "🌹"
        }
    }

    fun emojiFor(item: NinaItem): String {
        val id = item.id
        return when {
            item.isIntimo -> "🎀"
            id.contains("hamburguer") -> "🍔"
            id.contains("pizza") -> "🍕"
            id.contains("batata") -> "🍟"
            id.contains("sorvete") || id.contains("acai") || id.contains("milkshake") -> "🍨"
            id.contains("bolo") || id.contains("cupcake") || id.contains("brownie") -> "🧁"
            id.contains("chocolate") || id.contains("bombom") || id.contains("brigadeiro") -> "🍫"
            id.contains("flor") || id.contains("rosa") || id.contains("girass") || id.contains("orquidea") -> "🌸"
            id.contains("pet") || id.contains("gatin") || id.contains("racao") -> "🐾"
            id.contains("perfume") || id.contains("gloss") || id.contains("batom") -> "💄"
            item.app == NinaStoreApp.SHEIN -> "👗"
            item.app == NinaStoreApp.IFOOD -> "🍽️"
            item.app == NinaStoreApp.BOTICARIO -> "🧴"
            item.app == NinaStoreApp.PETLOVE -> "🐶"
            item.app == NinaStoreApp.DOCERIA -> "🍬"
            item.app == NinaStoreApp.FLORICULTURA -> "💐"
            else -> "🎁"
        }
    }

    private fun getStoreItems() = listOf(
        NinaItem("blusa_rosa", "Blusa Rosa", 90, 0, NinaStoreApp.SHEIN, 8, LOOK_CASUAL),
        NinaItem("blusa_preta", "Blusa Preta", 95, 0, NinaStoreApp.SHEIN, 7, LOOK_CASUAL),
        NinaItem("saia_plissada", "Saia Plissada", 140, 15, NinaStoreApp.SHEIN, 12, LOOK_CASUAL),
        NinaItem("calca_jeans", "Calça Jeans", 160, 10, NinaStoreApp.SHEIN, 10, LOOK_CASUAL),
        NinaItem("shorts_rosa", "Shorts Rosa", 120, 20, NinaStoreApp.SHEIN, 12, LOOK_CASUAL),
        NinaItem("cardigan_fofo", "Cardigan Fofinho", 170, 25, NinaStoreApp.SHEIN, 15, LOOK_CASUAL),
        NinaItem("moletom_grande", "Moletom Grande", 210, 30, NinaStoreApp.SHEIN, 16, LOOK_CASUAL),
        NinaItem("tenis_branco", "Tênis Branco", 260, 20, NinaStoreApp.SHEIN, 12, LOOK_CASUAL),
        NinaItem("bolsa_pequena", "Bolsa Pequena", 180, 25, NinaStoreApp.SHEIN, 14, LOOK_CASUAL),
        NinaItem("meia_fofa", "Meia Fofa", 35, 0, NinaStoreApp.SHEIN, 5, LOOK_CASUAL),
        NinaItem("calcinha_basica", "Calcinha Básica", 45, 55, NinaStoreApp.SHEIN, 10, isIntimo = true),
        NinaItem("sutian_basico", "Sutiã Básico", 85, 60, NinaStoreApp.SHEIN, 12, isIntimo = true),
        NinaItem("conjunto_lingerie", "Conjunto de Lingerie", 180, 85, NinaStoreApp.SHEIN, 20, isIntimo = true),
        NinaItem("camisola", "Camisola", 150, 75, NinaStoreApp.SHEIN, 18, LOOK_PIJAMA, isIntimo = true),
        NinaItem("vestido_rosa", "Vestido Rosa", 220, 20, NinaStoreApp.SHEIN, 15, LOOK_CASUAL),
        NinaItem("laco_gigante", "Laço Rosa Gigante", 80, 0, NinaStoreApp.SHEIN, 10, LOOK_CASUAL),
        NinaItem("pijama_fofo", "Pijama Fofinho", 180, 35, NinaStoreApp.SHEIN, 18, LOOK_PIJAMA),
        NinaItem("biquini_rosa", "Biquíni Rosa", 500, 80, NinaStoreApp.SHEIN, 25, LOOK_BIQUINI),

        NinaItem("coxinha", "Coxinha", 12, 0, NinaStoreApp.IFOOD, 4),
        NinaItem("pastel_queijo", "Pastel de Queijo", 18, 0, NinaStoreApp.IFOOD, 5),
        NinaItem("batata_frita", "Batata Frita", 25, 0, NinaStoreApp.IFOOD, 6),
        NinaItem("pizza_broto", "Pizza Broto", 36, 0, NinaStoreApp.IFOOD, 7),
        NinaItem("pizza_grande", "Pizza Grande", 75, 10, NinaStoreApp.IFOOD, 10),
        NinaItem("hamburguer_simples", "Hambúrguer Simples", 30, 0, NinaStoreApp.IFOOD, 6),
        NinaItem("hamburguer_duplo", "Hambúrguer Duplo", 48, 5, NinaStoreApp.IFOOD, 8),
        NinaItem("combo_hamburguer", "Combo Hambúrguer", 62, 10, NinaStoreApp.IFOOD, 10),
        NinaItem("milkshake", "Milkshake", 24, 0, NinaStoreApp.IFOOD, 7),
        NinaItem("cafe_gelado", "Café Gelado", 18, 0, NinaStoreApp.IFOOD, 5),
        NinaItem("lasanha", "Lasanha", 42, 10, NinaStoreApp.IFOOD, 9),
        NinaItem("yakisoba", "Yakisoba", 39, 10, NinaStoreApp.IFOOD, 8),
        NinaItem("temaki", "Temaki", 32, 10, NinaStoreApp.IFOOD, 7),
        NinaItem("sorvete", "Sorvete", 28, 0, NinaStoreApp.IFOOD, 7),
        NinaItem("marmita_fit", "Marmita Fit", 34, 0, NinaStoreApp.IFOOD, 5),
        NinaItem("salada_chique", "Salada Chique", 38, 15, NinaStoreApp.IFOOD, 6),
        NinaItem("sushi", "Combo de Sushi", 95, 10, NinaStoreApp.IFOOD, 10),
        NinaItem("hamburguer", "Hambúrguer Artesanal", 45, 0, NinaStoreApp.IFOOD, 6),
        NinaItem("acai", "Açaí com Leite Condensado", 28, 0, NinaStoreApp.IFOOD, 8),

        NinaItem("creme_maos", "Creme de Mãos", 35, 0, NinaStoreApp.BOTICARIO, 6),
        NinaItem("hidratante", "Hidratante", 65, 5, NinaStoreApp.BOTICARIO, 8),
        NinaItem("gloss", "Gloss Rosa", 38, 10, NinaStoreApp.BOTICARIO, 8),
        NinaItem("rimel", "Rímel", 48, 15, NinaStoreApp.BOTICARIO, 9),
        NinaItem("delineador", "Delineador", 42, 15, NinaStoreApp.BOTICARIO, 8),
        NinaItem("blush", "Blush", 55, 20, NinaStoreApp.BOTICARIO, 10),
        NinaItem("base", "Base", 70, 20, NinaStoreApp.BOTICARIO, 10),
        NinaItem("paleta_sombras", "Paleta de Sombras", 120, 35, NinaStoreApp.BOTICARIO, 14),
        NinaItem("oleo_corporal", "Óleo Corporal", 85, 35, NinaStoreApp.BOTICARIO, 12),
        NinaItem("sabonete_perfumado", "Sabonete Perfumado", 22, 0, NinaStoreApp.BOTICARIO, 5),
        NinaItem("kit_banho", "Kit Banho", 140, 25, NinaStoreApp.BOTICARIO, 15),
        NinaItem("perfume", "Perfume Doce", 180, 30, NinaStoreApp.BOTICARIO, 14),
        NinaItem("batom", "Batom Rosa", 55, 15, NinaStoreApp.BOTICARIO, 10),
        NinaItem("kit_skincare", "Kit Skincare", 260, 45, NinaStoreApp.BOTICARIO, 20),

        NinaItem("racao_premium", "Ração Premium", 120, 0, NinaStoreApp.PETLOVE, 8),
        NinaItem("coleira_rosa", "Coleira Rosa", 45, 0, NinaStoreApp.PETLOVE, 7),
        NinaItem("caminha_pet", "Caminha Pet", 180, 15, NinaStoreApp.PETLOVE, 12),
        NinaItem("brinquedo_pet", "Brinquedo de Pet", 35, 0, NinaStoreApp.PETLOVE, 7),
        NinaItem("casinha_pet", "Casinha Pet", 350, 30, NinaStoreApp.PETLOVE, 18),
        NinaItem("lacinho_pet", "Lacinho de Pet", 18, 0, NinaStoreApp.PETLOVE, 5),
        NinaItem("petisco_pet", "Petisco Pet", 25, 0, NinaStoreApp.PETLOVE, 5),
        NinaItem("ursinho_pet", "Ursinho de Cachorrinho", 70, 0, NinaStoreApp.PETLOVE, 12),
        NinaItem("gatinho_pelucia", "Pelúcia de Gatinho", 90, 10, NinaStoreApp.PETLOVE, 14),

        NinaItem("brigadeiro", "Brigadeiro", 18, 0, NinaStoreApp.DOCERIA, 5),
        NinaItem("beijinho", "Beijinho", 18, 0, NinaStoreApp.DOCERIA, 5),
        NinaItem("brownie", "Brownie", 22, 0, NinaStoreApp.DOCERIA, 6),
        NinaItem("cupcake", "Cupcake", 20, 0, NinaStoreApp.DOCERIA, 6),
        NinaItem("torta_limao", "Torta de Limão", 55, 5, NinaStoreApp.DOCERIA, 9),
        NinaItem("pudim", "Pudim", 45, 0, NinaStoreApp.DOCERIA, 8),
        NinaItem("bolo_chocolate", "Bolo de Chocolate", 70, 5, NinaStoreApp.DOCERIA, 11),
        NinaItem("donuts", "Caixa de Donuts", 50, 0, NinaStoreApp.DOCERIA, 9),
        NinaItem("macarons", "Macarons", 95, 20, NinaStoreApp.DOCERIA, 13),
        NinaItem("cesta_doces", "Cesta de Doces", 160, 30, NinaStoreApp.DOCERIA, 18),
        NinaItem("chocolate", "Caixa de Bombom", 30, 0, NinaStoreApp.DOCERIA, 8),
        NinaItem("bolo_morango", "Bolo de Morango", 75, 5, NinaStoreApp.DOCERIA, 12),

        NinaItem("rosa_unica", "Rosa Única", 35, 0, NinaStoreApp.FLORICULTURA, 7),
        NinaItem("girassois", "Girassóis", 90, 10, NinaStoreApp.FLORICULTURA, 12),
        NinaItem("orquidea", "Orquídea", 130, 20, NinaStoreApp.FLORICULTURA, 15),
        NinaItem("carta_romantica", "Carta Romântica", 20, 0, NinaStoreApp.FLORICULTURA, 9),
        NinaItem("caixa_surpresa", "Caixa Surpresa", 180, 30, NinaStoreApp.FLORICULTURA, 18),
        NinaItem("colar_coracao", "Colar de Coração", 260, 55, NinaStoreApp.FLORICULTURA, 22),
        NinaItem("pulseira", "Pulseira Delicada", 190, 45, NinaStoreApp.FLORICULTURA, 18),
        NinaItem("anel_simples", "Anel Simples", 320, 70, NinaStoreApp.FLORICULTURA, 25),
        NinaItem("flores", "Buquê de Flores", 150, 20, NinaStoreApp.FLORICULTURA, 18),
        NinaItem("alianca", "Aliança de Compromisso", 2000, 95, NinaStoreApp.FLORICULTURA, 35)
    )
}
