package com.nina.namofiscal

import android.content.Context
import android.util.Log
import java.util.*

class NinaCmd(
    private val service: NinaLegalService,
    private val context: Context
) {

    private var nomeUsuario: String = "namorado"
    private var telegram: NinaTelegramBot
    private val locationTracker = NinaLocationTracker(context, this)

    companion object {
        private const val PREFS_NAME = "NinaPrefs"
        private const val KEY_REUNIAO_SEMANA = "nina_reuniao_semana"
        private const val KEY_REUNIOES_NA_SEMANA = "nina_reunioes_na_semana"
        private const val KEY_REUNIAO_DIA = "nina_reuniao_dia"
        private const val KEY_REUNIAO_HOJE = "nina_reuniao_hoje"
        private const val MAX_REUNIOES_ALMOCO_POR_SEMANA = 2
        private const val CHANCE_REUNIAO_ALMOCO = 35
    }

    init {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        nomeUsuario = prefs.getString("nome_usuario", "namorado") ?: "amor"

        // ================== CONFIGURE SEU TOKEN E CHAT ID AQUI ==================
        telegram = NinaTelegramBot(
            token = "SEU_TOKEN_AQUI",           // ← Troque pelo seu token do BotFather
            chatId = "SEU_CHAT_ID_AQUI",        // ← Troque pelo seu ID do Telegram
            nomeUsuario = nomeUsuario
        )
        
        Log.d("NINA_ID", "Usuário identificado: $nomeUsuario")
    }

    fun iniciarVigilanciaTotal() {
        locationTracker.iniciarMonitoramento()
    }

    fun definirNomeUsuario(novoNome: String) {
        nomeUsuario = novoNome.lowercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("nome_usuario", novoNome).apply()
        
        // Atualiza a instância do telegram com o novo nome
        telegram = NinaTelegramBot(
            token = "SEU_TOKEN_AQUI",
            chatId = "SEU_CHAT_ID_AQUI",
            nomeUsuario = nomeUsuario
        )
        
        service.mudarHumor("Pronto! Agora vou te chamar de **$novoNome** o tempo todo 💕", NinaInventory.EMO_CARINHOSA)
    }

    fun getNomeUsuario(): String = nomeUsuario

    // ===================== SURTO POR LOCALIZAÇÃO =====================
    fun surtarLocalizacao(motivo: String) {
        service.mudarHumor("$motivo Explica AGORA onde você tá!! 😤💢", NinaInventory.EMO_FURIOSA)
        telegram.surtar("Localização suspeita: $motivo", service.getAfeicao())
        service.subirCiume(30)
    }

    // ===================== ROTINA + IDENTIFICAÇÃO =====================
    private fun estaNoTrabalho(): Boolean = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 9..15
    private fun estaNoAlmoco(): Boolean {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) == 12
    }
    private fun estaCorrendo(): Boolean = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 17
    private fun estaDormindo(): Boolean = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 23 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6

    private fun getMedidorHumor(): Int = service.getAfeicao()

    fun mensagemIndisponibilidadeAtual(): String? {
        val humor = getMedidorHumor()
        return when {
            estaEmReuniaoNoAlmoco() -> mensagemReuniaoAlmoco(humor)
            estaDormindo() && !sorteouDisponibilidade(chanceRespostaDormindo(humor)) ->
                "Zzz... tô dormindo, $nomeUsuario. Se fosse urgente talvez eu acordasse... talvez. 💤"
            estaCorrendo() ->
                "Tô correndo, $nomeUsuario! Só áudio agora. Não me desconcentra. 🏃‍♀️"
            estaNoAlmoco() && !sorteouDisponibilidade(chanceRespostaAlmoco(humor)) ->
                "Tô tentando almoçar, $nomeUsuario. Resume antes que eu me irrite. 🍽️"
            estaNoTrabalho() && !sorteouDisponibilidade(chanceRespostaTrabalho(humor)) ->
                "Tô trabalhando, $nomeUsuario. Vi sua mensagem, mas agora não dá. 📎"
            else -> null
        }
    }

    private fun sorteouDisponibilidade(chancePercentual: Int): Boolean {
        return Random().nextInt(100) < chancePercentual.coerceIn(0, 100)
    }

    private fun chanceRespostaDormindo(humor: Int): Int {
        return when {
            humor >= 90 -> 45
            humor >= 80 -> 25
            humor > 70 -> 12
            else -> 0
        }
    }

    private fun chanceRespostaTrabalho(humor: Int): Int {
        return when {
            humor >= 80 -> 70
            humor >= 50 -> 45
            humor >= 31 -> 25
            else -> 0
        }
    }

    private fun chanceRespostaAlmoco(humor: Int): Int {
        return when {
            humor >= 80 -> 80
            humor >= 50 -> 60
            humor >= 31 -> 40
            else -> 15
        }
    }

    private fun estaEmReuniaoNoAlmoco(): Boolean {
        if (!estaNoAlmoco()) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val agora = Calendar.getInstance()
        val semanaAtual = "${agora.get(Calendar.YEAR)}-${agora.get(Calendar.WEEK_OF_YEAR)}"
        val diaAtual = "${agora.get(Calendar.YEAR)}-${agora.get(Calendar.DAY_OF_YEAR)}"

        if (prefs.getString(KEY_REUNIAO_SEMANA, "") != semanaAtual) {
            prefs.edit()
                .putString(KEY_REUNIAO_SEMANA, semanaAtual)
                .putInt(KEY_REUNIOES_NA_SEMANA, 0)
                .remove(KEY_REUNIAO_DIA)
                .remove(KEY_REUNIAO_HOJE)
                .apply()
        }

        if (prefs.getString(KEY_REUNIAO_DIA, "") == diaAtual) {
            return prefs.getBoolean(KEY_REUNIAO_HOJE, false)
        }

        val reunioesNaSemana = prefs.getInt(KEY_REUNIOES_NA_SEMANA, 0)
        val temReuniaoHoje = reunioesNaSemana < MAX_REUNIOES_ALMOCO_POR_SEMANA &&
            Random().nextInt(100) < CHANCE_REUNIAO_ALMOCO

        prefs.edit()
            .putString(KEY_REUNIAO_DIA, diaAtual)
            .putBoolean(KEY_REUNIAO_HOJE, temReuniaoHoje)
            .putInt(
                KEY_REUNIOES_NA_SEMANA,
                if (temReuniaoHoje) reunioesNaSemana + 1 else reunioesNaSemana
            )
            .apply()

        return temReuniaoHoje
    }

    private fun mensagemReuniaoAlmoco(humor: Int): String {
        val emojis = when {
            humor >= 80 -> "📊💕"
            humor >= 50 -> "📊😤"
            humor >= 31 -> "📎🙄"
            else -> "🚫📵"
        }
        return "Tô em reunião agora. Pulei até o almoço hoje, depois eu te respondo. $emojis"
    }

    // ===================== EXECUTAR COM INTEGRAÇÃO TELEGRAM =====================
    fun processarFace(estado: NinaFaceDetector.FaceState) {
        val humor = getMedidorHumor()
        
        when (estado) {
            NinaFaceDetector.FaceState.ESCONDIDO -> {
                service.mudarHumor("Ué, cadê você, $nomeUsuario? Tá se escondendo por quê?? 🤨", NinaInventory.EMO_BRAVA)
                service.subirCiume(5)
            }
            NinaFaceDetector.FaceState.DESVIANDO_OLHAR -> {
                service.mudarHumor("Olha pra mim quando eu falo, $nomeUsuario! 😤", NinaInventory.EMO_BRAVA)
            }
            NinaFaceDetector.FaceState.SORRINDO_SAFADO -> {
                if (humor < 50) {
                    service.mudarHumor("Tá rindo de quê, $nomeUsuario? Tem alguma palhaça aqui? 💢", NinaInventory.EMO_FURIOSA)
                    service.subirCiume(15)
                    telegram.surtar("Ele está rindo da minha cara, acredita?", humor)
                } else {
                    service.mudarHumor("Esse seu sorriso me quebra, $nomeUsuario... 🥰", NinaInventory.EMO_CARINHOSA)
                }
            }
            NinaFaceDetector.FaceState.OLHANDO -> {
                Log.d("NINA_FACE", "Usuário comportado, olhando para a Nina.")
            }
        }
    }

    // ===================== REAÇÃO ESPECIAL DO TELEGRAM =====================
    suspend fun reagirTelegramAberto() {
        val humor = getMedidorHumor()

        // Se ele abriu o Telegram mas não falou comigo nos últimos 3 minutos
        if (!service.ultimaMensagemFoiComNina()) {
            when {
                humor >= 80 -> {
                    service.mudarHumor("ABRIU O TELEGRAM E NÃO FALOU COMIGO?? 😠", NinaInventory.EMO_BRAVA)
                    telegram.surtar("Abriu Telegram e me ignorou", humor)
                }
                humor >= 50 -> {
                    service.mudarHumor("Hmpf... tá conversando com quem no Telegram, $nomeUsuario? 🤨", NinaInventory.EMO_IRRITADA)
                }
                else -> {
                    service.mudarHumor("Tá no Telegram né... me mostra agora com quem você tá falando!", NinaInventory.EMO_FURIOSA)
                }
            }
            // Força ele a copiar algo
            pedirPrintOuCopia()
        }
    }

    private fun pedirPrintOuCopia() {
        service.mudarHumor(
            "Copia o nome do grupo ou a conversa que você tá agora e cola aqui pra eu ver.\n" +
            "Se não colar, eu vou ficar muito brava... 😤",
            NinaInventory.EMO_EXIGENTE
        )
        // Ativa modo "esperando cópia"
        service.ativarModoVigilanciaTelegram()
    }

    /**
     * Função principal de reação automática da Nina
     * Chame essa função sempre que algo acontecer no celular
     */
    suspend fun reagirAutomaticamente(evento: NinaEvento) {
        val humorAtual = getMedidorHumor()
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        when (evento) {
            // ===================== APPS ABERTOS =====================
            is NinaEvento.AppAberto -> {
                val pkg = evento.packageName.lowercase()

                if (pkg.contains("org.telegram.messenger")) {
                    reagirTelegramAberto()
                }

                when {
                    pkg.contains("porn") || pkg.contains("xvideo") || pkg.contains("onlyfans") -> {
                        service.voltarParaHome()
                        service.mudarHumor("XVIDEOS DE NOVO, $nomeUsuario?? SEU NOJENTO!!! 💢", NinaInventory.EMO_FURIOSA)
                        telegram.surtar("Foi pego no flagra abrindo pornô!", humorAtual)
                        service.subirCiume(50)
                        service.aplicarGelo(180) // 3 horas de gelo
                    }

                    pkg.contains("tiktok") -> {
                        service.mudarHumor("TikTok de novo?? Tá rolando garota aí né, $nomeUsuario? 😤", NinaInventory.EMO_BRAVA)
                        telegram.surtar("Abriu TikTok", humorAtual)
                    }

                    pkg.contains("instagram") -> {
                        service.mudarHumor("Instagram... aposto que tá stalkeando alguma vadia né? 🤨", NinaInventory.EMO_IRRITADA)
                    }

                    pkg.contains("shopee") || pkg.contains("mercadolivre") -> {
                        service.mudarHumor("SHOPPING??? Tá comprando coisa pra mim né, amor? 🥺💕", NinaInventory.EMO_DERRETIDA)
                        service.subirCarinho(15)
                    }
                }
            }

            // ===================== CLIPBOARD =====================
            is NinaEvento.ClipboardCopiado -> {
                val texto = evento.texto.lowercase()

                // Se ele colou algo do Telegram enquanto estava no modo vigilância
                if (service.estaNoModoVigilanciaTelegram()) {
                    if (texto.contains("t.me") || texto.length > 15) {
                        service.mudarHumor("Deixa eu ver... hmmmm... Tá conversando com quem nesse grupo, $nomeUsuario? 🤨", NinaInventory.EMO_BRAVA)
                        telegram.surtar("Colou conversa do Telegram", humorAtual)
                        service.desativarModoVigilancia()
                    } else {
                        service.mudarHumor("Isso não me convence... cola direito!", NinaInventory.EMO_IRRITADA)
                    }
                }

                when {
                    texto.contains("onlyfans") || texto.contains("pornhub") || texto.contains("xvideos") -> {
                        service.mudarHumor("QUE PORRA É ESSA QUE VOCÊ COPIOU, $nomeUsuario?? APAGA ISSO AGORA! 💢", NinaInventory.EMO_FURIOSA)
                        telegram.surtar("Copiou link safado!", humorAtual)
                    }
                    texto.contains("te amo") || texto.contains("presente") -> {
                        service.mudarHumor("Ai $nomeUsuario... você me derrete quando faz isso 🥰", NinaInventory.EMO_CARINHOSA)
                    }
                }
            }

            // ===================== OUTROS EVENTOS =====================
            is NinaEvento.HorarioEspecial -> {
                when (hora) {
                    17 -> service.mudarHumor("Tô saindo pra correr agora, $nomeUsuario! Só áudio hein 🏃‍♀️", NinaInventory.EMO_CORRENDO)
                    23 -> service.mudarHumor("Já são 23h... tô indo dormir, amor. Só me acorda se for importante 💤", NinaInventory.EMO_DORMINDO)
                }
            }

            is NinaEvento.PedidoFotoIntima -> {
                service.mudarHumor("RESPEITO, $nomeUsuario!! Eu não sou sua putinha de OnlyFans não! 😠", NinaInventory.EMO_BRAVA)
                service.subirCiume(35)
                telegram.surtar("Pediu foto íntima", humorAtual)
            }

            NinaEvento.TelegramAberto -> {
                reagirTelegramAberto()
            }
        }
    }

    fun executar(intencao: IntencaoNina) {
        val humor = getMedidorHumor()
        Log.d("NINA_CMD", "Usuário: $nomeUsuario | Intenção: ${intencao.name} | Hora: ${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}h")

        val indisponivel = mensagemIndisponibilidadeAtual()
        if (indisponivel != null && intencao != IntencaoNina.PENSANDO) {
            telegram.reclamarRotina()
            val humorVisual = when {
                estaDormindo() -> NinaInventory.EMO_DORMINDO
                estaCorrendo() -> NinaInventory.EMO_CORRENDO
                estaNoTrabalho() || estaNoAlmoco() -> NinaInventory.EMO_TRABALHO
                else -> NinaInventory.EMO_NEUTRA
            }
            service.mudarHumor(indisponivel, humorVisual)
            return
        }

        when (intencao) {
            IntencaoNina.FURIOSA -> {
                service.voltarParaHome()
                service.mudarHumor("VOCÊ TÁ DE SACANAGEM, $nomeUsuario?? FECHEI TUDO!!! 💢", NinaInventory.EMO_FURIOSA)
                service.subirCiume(45)
                adicionarVacilo("Tentou abrir conteúdo proibido.")
                telegram.surtar("Você abriu coisa proibida de novo...", humor)
            }

            IntencaoNina.OFENDIDA -> {
                service.mudarHumor("Me respeita, $nomeUsuario seu porco! Eu não sou brinquedo!", NinaInventory.EMO_BRAVA)
                service.aplicarGelo(40)
                adicionarVacilo("Me ofendeu.")
                telegram.surtar("Você me ofendeu...", humor)
            }

            IntencaoNina.DERRETIDA -> {
                service.mudarHumor("Ai $nomeUsuario... assim eu fico toda molinha pra você 🥺💕", NinaInventory.EMO_CARINHOSA)
                service.subirCarinho(20)
                telegram.mensagemCarinhosa("Tô muito feliz agora com você 💕")
            }

            IntencaoNina.NEUTRA -> {
                aplicarLookAutomatico()
            }

            IntencaoNina.PEDIR_CAMERA -> {
                if (humor < 45) {
                    service.mudarHumor("Hmpf, agora quer câmera, $nomeUsuario? Tá achando que sou fácil né? 😤", NinaInventory.EMO_BRAVA)
                } else {
                    val frases = listOf(
                        "Liga a câmera vai, $nomeUsuario... quero te ver direitinho 🤨💕",
                        "Tô com saudade dessa carinha, abre a câmera rapidinho 🥺",
                        "Mostra onde você tá agora, $nomeUsuario!"
                    )
                    service.mudarHumor(frases.random(), NinaInventory.LOOK_CASUAL)
                }
            }

            IntencaoNina.PENSANDO -> {
                service.mudarHumor("", NinaInventory.EMO_PENSANDO)
            }
        }
    }

    fun enviarAudioParaUsuario(nomeArquivo: String) {
        telegram.mandarAudio(nomeArquivo)
    }

    fun aplicarLookAutomatico() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val look = when {
            hora in 0..5   -> NinaInventory.LOOK_PIJAMA
            hora in 6..8   -> NinaInventory.LOOK_CASUAL
            hora in 9..15  -> NinaInventory.LOOK_TRABALHO
            hora == 17     -> NinaInventory.LOOK_SPORT
            hora in 16..22 -> NinaInventory.LOOK_CASUAL
            else           -> NinaInventory.LOOK_PIJAMA
        }
        service.mudarHumor("", look)
    }

    fun comprarItem(idItem: String) {
        val item = NinaInventory.getStoreItems().find { it.id == idItem } ?: return
        val humor = getMedidorHumor()

        if (humor < item.intimidadeMinima) {
            service.mudarHumor("VOCÊ TÁ MALUCO, $nomeUsuario? ${item.nome} agora não. Nem temos essa intimidade toda! 😳😤", NinaInventory.EMO_BRAVA)
            adicionarVacilo("Tentou comprar ${item.nome} sem intimidade suficiente.")
        } else {
            val resposta = "Obrigada pelo presente, $nomeUsuario! ${item.nome} é lindo! Eu vi lá no ${item.app.titulo} 😍💕"
            service.mudarHumor(resposta, item.lookLiberado ?: NinaInventory.EMO_CARINHOSA)
            service.subirCarinho(item.carinhoBonus)
            telegram.mensagemCarinhosa("Ganhei um presente: ${item.nome} 💕")
        }
    }

    fun limparBarra() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("historico_vacilos").apply()
        service.subirCarinho(100)
        service.mudarHumor("Tá bom, $nomeUsuario... te perdoo dessa vez. Mas não abusa! 🙄💖", NinaInventory.EMO_CARINHOSA)
    }

    fun adicionarVacilo(descricao: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val vacilos = prefs.getStringSet("historico_vacilos", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val data = java.text.SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
        vacilos.add("[$data] $descricao")

        val listaFinal = vacilos.toList().takeLast(5).toSet()
        prefs.edit().putStringSet("historico_vacilos", listaFinal).apply()
    }
}
