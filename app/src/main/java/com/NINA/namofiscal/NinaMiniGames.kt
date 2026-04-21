package com.nina.namofiscal

enum class MiniGameStatus {
    EM_BREVE,
    PLANEJADO
}

data class NinaMiniGame(
    val id: String,
    val nome: String,
    val status: MiniGameStatus,
    val descricao: String,
    val recompensa: String,
    val implementationNotes: String
)

object NinaMiniGames {
    fun getFeaturedSoon(): NinaMiniGame = getAll().first { it.id == "entrega_ifood" }

    fun getAll(): List<NinaMiniGame> = listOf(
        NinaMiniGame(
            id = "entrega_ifood",
            nome = "Entrega iFood",
            status = MiniGameStatus.EM_BREVE,
            descricao = "Ajude a entrega chegar antes da Nina ficar irritada de fome.",
            recompensa = "Dinheiro do usuario e chance de melhorar o humor da Nina se ela aceitar o mimo.",
            implementationNotes = "Primeiro minigame a implementar: corrida simples contra o tempo, obstaculos leves e bonus se chegar no horario do almoco ou jantar."
        ),
        NinaMiniGame(
            id = "anti_scroll",
            nome = "Anti-scroll",
            status = MiniGameStatus.PLANEJADO,
            descricao = "Feche tentacoes de redes sociais antes que elas encham a tela.",
            recompensa = "Dinheiro do usuario por resistir ao impulso e bonus por streak sem app proibido.",
            implementationNotes = "Jogo de reflexo com cartas/icons de apps caindo. Apps permitidos nao devem ser bloqueados para evitar penalidade."
        ),
        NinaMiniGame(
            id = "defesa_nina",
            nome = "Defesa da Nina",
            status = MiniGameStatus.PLANEJADO,
            descricao = "Bloqueie redes sociais e apps proibidos tentando invadir a rotina da Nina.",
            recompensa = "Moedas e reducao de ciume quando o usuario protege a rotina dela.",
            implementationNotes = "Tower-defense simples em 2D. Cada onda representa uma tentacao digital diferente."
        ),
        NinaMiniGame(
            id = "trabalho_nina",
            nome = "Trabalho da Nina",
            status = MiniGameStatus.PLANEJADO,
            descricao = "Organize tarefas enquanto ela trabalha. Se ajudar bem, ela pode ganhar bonus.",
            recompensa = "Bonus no salario da Nina, mas ela decide como gastar o proprio dinheiro.",
            implementationNotes = "Puzzle rapido de organizar demandas. Nao deve permitir controlar a rotina dela, apenas ajudar quando ela aceitar ajuda."
        ),
        NinaMiniGame(
            id = "caca_vacilos",
            nome = "Caca aos Vacilos",
            status = MiniGameStatus.PLANEJADO,
            descricao = "Elimine palavras, links e apps de vacilo antes que virem problema.",
            recompensa = "Moedas, limpeza parcial de vacilos e pequenas recuperacoes de afeicao.",
            implementationNotes = "Jogo de mira/reflexo. Deve evitar conteudo explicito visual; usar textos e icones abstratos."
        )
    )
}
