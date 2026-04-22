package com.nina.namofiscal

import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nina.namofiscal.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatAdapter: NinaChatAdapter
    private val mensagens = mutableListOf<ChatMessage>()
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ninaIA: NinaIA? = null
    private var receiverRegistrado = false

    private val ninaStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val texto = intent?.getStringExtra(NinaLegalService.EXTRA_TEXTO).orEmpty()
            val humor = intent?.getStringExtra(NinaLegalService.EXTRA_HUMOR)
            val afeicao = intent?.getIntExtra(NinaLegalService.EXTRA_AFEICAO, 50) ?: 50
            val ciume = intent?.getIntExtra(NinaLegalService.EXTRA_CIUME, 30) ?: 30

            atualizarStatusNina(texto, humor, afeicao, ciume)
            if (texto.isBlank()) return

            if (binding.chatContainer.visibility == View.VISIBLE) {
                adicionarMensagemNina(texto)
            } else {
                Toast.makeText(this@MainActivity, "Nina: $texto", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aplicarTelaCheia()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NinaEconomy.ensureMonthlyCycle(applicationContext)

        setupChat()
        setupButtons()
        atualizarRelogioDaNina()
        atualizarStatusNina("", NinaInventory.EMO_NEUTRA, 50, 30)
        checkPermissions()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            aplicarTelaCheia()
        }
    }

    private fun aplicarTelaCheia() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setupButtons() {
        // Permissão de Sobreposição
        binding.btnPermissaoOverlay.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        // Permissão de Uso
        binding.btnPermissaoUso.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        // Botão "TUDO PRONTO" (Ativa a Home)
        binding.btnAtivarNina.setOnClickListener {
            mostrarHomeNina()
            iniciarServicoNina()
        }

        // Abrir o WhatsApp da Nina (dentro da Home dela)
        binding.btnAbrirZap.setOnClickListener {
            binding.ninaHomeContainer.visibility = View.GONE
            binding.agendaContainer.visibility = View.GONE
            binding.bankContainer.visibility = View.GONE
            binding.chatContainer.visibility = View.VISIBLE
        }

        binding.btnHomeSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnAbrirShein.setOnClickListener {
            mostrarAppDaNina(NinaStoreApp.SHEIN)
        }

        binding.btnAbrirIfood.setOnClickListener {
            mostrarAppDaNina(NinaStoreApp.IFOOD)
        }

        binding.btnAbrirBoticario.setOnClickListener {
            mostrarAppDaNina(NinaStoreApp.BOTICARIO)
        }

        binding.btnAbrirPetlove.setOnClickListener {
            mostrarAppDaNina(NinaStoreApp.PETLOVE)
        }

        binding.btnAbrirDoceria.setOnClickListener {
            mostrarAppDaNina(NinaStoreApp.DOCERIA)
        }

        binding.btnAbrirFlores.setOnClickListener {
            mostrarAppDaNina(NinaStoreApp.FLORICULTURA)
        }

        binding.btnAbrirEntregaIfood.setOnClickListener {
            mostrarMiniGameEmBreve()
        }

        binding.btnAbrirAgenda.setOnClickListener {
            mostrarAgendaDaNina()
        }

        binding.btnAbrirBanco.setOnClickListener {
            mostrarBancoDaNina()
        }

        binding.btnFecharAgenda.setOnClickListener {
            mostrarHomeNina()
        }

        binding.btnFecharBanco.setOnClickListener {
            mostrarHomeNina()
        }

        // Botão de Configurações (Engrenagem no Chat)
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun mostrarAppDaNina(app: NinaStoreApp) {
        val nome = getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE).getString("nome_usuario", "yago")
        val itens = NinaInventory.getStoreItems(app)
            .joinToString(", ") { it.nome }
        Toast.makeText(
            this,
            "Nina abriu ${app.titulo}: $itens. Vai mimar ou vai so olhar, $nome?",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun mostrarMiniGameEmBreve() {
        val miniGame = NinaMiniGames.getFeaturedSoon()
        Toast.makeText(
            this,
            "${miniGame.nome} - Em breve. ${miniGame.descricao}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun mostrarAgendaDaNina() {
        atualizarRelogioDaNina()
        atualizarAgendaDaNina()
        binding.permissionContainer.visibility = View.GONE
        binding.ninaHomeContainer.visibility = View.GONE
        binding.chatContainer.visibility = View.GONE
        binding.bankContainer.visibility = View.GONE
        binding.agendaContainer.visibility = View.VISIBLE
    }

    private fun mostrarBancoDaNina() {
        val access = NinaBankPrivacy.registerOpen(applicationContext)
        NinaLegalService.getInstance()?.mudarHumor(access.complaint, NinaInventory.EMO_IRRITADA)
        atualizarBancoDaNina(access)
        binding.permissionContainer.visibility = View.GONE
        binding.ninaHomeContainer.visibility = View.GONE
        binding.chatContainer.visibility = View.GONE
        binding.agendaContainer.visibility = View.GONE
        binding.bankContainer.visibility = View.VISIBLE
    }

    private fun atualizarBancoDaNina(access: NinaBankAccess? = null) {
        val snapshot = NinaEconomy.ensureMonthlyCycle(applicationContext)
        val hideBalance = access?.shouldHideBalance ?: NinaBankPrivacy.isBalanceHidden(applicationContext)
        val visibleBills = snapshot.bills.filterNot { it.isSecret }
        val billText = visibleBills.joinToString("\n") { bill ->
            val cadence = if (bill.cadence == BillCadence.TRIMESTRAL) " trimestral" else ""
            "${bill.nome}: ${NinaEconomy.formatMoney(bill.money)}$cadence"
        }
        val statementText = NinaEconomy.getLedger(applicationContext)
            .takeLast(8)
            .joinToString("\n") { entry ->
                val signal = if (entry.type == NinaLedgerType.ENTRADA) "+" else "-"
                "$signal ${NinaEconomy.formatMoney(entry.money)} | ${entry.label}"
            }
            .ifBlank { "Sem movimentos visiveis ainda." }
        val visibleBillsTotal = visibleBills.sumOf { it.money }

        binding.tvBankSubtitle.text = "Telefone da Nina | ${NinaTime.phoneClock(applicationContext)}"
        binding.tvBankWarning.text = access?.complaint ?: "Hmpf. Mexe pouco no meu banco."
        binding.tvBankBalance.text = if (hideBalance) {
            "Saldo: escondido"
        } else {
            "Saldo: ${NinaEconomy.formatMoney(snapshot.balanceMoney)}"
        }
        binding.tvBankSalary.text =
            "Salario mensal: ${NinaEconomy.formatMoney(snapshot.monthlySalaryMoney)}\n" +
            "Salario-base: ${NinaEconomy.formatMoney(snapshot.baseSalaryMoney)}\n" +
            "Sobra prevista: ${NinaEconomy.formatMoney(snapshot.disposableMoney)}"
        binding.tvBankBills.text =
            "Contas visiveis: ${NinaEconomy.formatMoney(visibleBillsTotal)}\n$billText"
        binding.tvBankStatement.text = "Extrato do mes\n$statementText"
        binding.tvBankIndex.text =
            "Indice: 1 Big Mac = ${snapshot.bigMacPriceMoney} dinheiros\n" +
            "Compra real: 1 Big Mac real = ${NinaEconomy.getUserRealMoneyIndex().gameBigsPerRealBigMac} Bigs no game"
    }

    private fun atualizarAgendaDaNina() {
        val dias = NinaSchedule.getPublicWeek(applicationContext)
        val views = listOf(
            binding.tvAgendaDom,
            binding.tvAgendaSeg,
            binding.tvAgendaTer,
            binding.tvAgendaQua,
            binding.tvAgendaQui,
            binding.tvAgendaSex,
            binding.tvAgendaSab
        )
        val folgaExtra = dias.firstOrNull { it.summary.contains("Folga extra", ignoreCase = true) }?.title ?: "?"

        binding.tvAgendaSubtitle.text = "Semana atual | ${NinaTime.phoneClock(applicationContext)} | folga extra: $folgaExtra"
        views.zip(dias).forEach { (view, day) ->
            val prefix = if (day.isToday) "HOJE | " else ""
            view.text = "$prefix${day.title}\n${day.summary}"
        }
    }

    private fun atualizarRelogioDaNina() {
        binding.tvHomeClock.text = NinaTime.phoneClock(applicationContext)
    }

    private fun atualizarStatusNina(texto: String, humor: String?, afeicao: Int, ciume: Int) {
        val saidaAtual = NinaSchedule.getActiveOuting(applicationContext)
        val visualStatus = when (saidaAtual?.companion) {
            NinaOutingCompanion.AMIGAS -> NinaVisualStatuses.get(NinaStatusKey.SAINDO_COM_AMIGAS)
            NinaOutingCompanion.AMIGO -> NinaVisualStatuses.get(NinaStatusKey.SAINDO_COM_AMIGO)
            else -> NinaVisualStatuses.fromHumor(humor, texto)
        }
        binding.imgNinaHome.setImageResource(visualStatus.imageRes)

        val detalhe = when {
            texto.contains("reunião", ignoreCase = true) -> "em reunião"
            saidaAtual != null && texto.isBlank() -> "fora das ${saidaAtual.startHour}h às ${saidaAtual.endHour}h | afeição $afeicao | ciúme $ciume"
            texto.isNotBlank() -> texto.take(42)
            else -> "${visualStatus.detail} | afeição $afeicao | ciúme $ciume"
        }

        binding.tvNinaStatus.text = "Nina: ${visualStatus.label} - $detalhe"
    }

    private fun setupChat() {
        chatAdapter = NinaChatAdapter(mensagens)
        binding.rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = chatAdapter

        val onboardingMessages = NinaOnboarding(applicationContext).startIfNeeded()
        if (onboardingMessages.isEmpty()) {
            adicionarMensagemNina("Oi, amor. Eu ja tava aqui te esperando... abre o jogo: o que voce veio fazer no celular? 😤")
        } else {
            onboardingMessages.forEach { adicionarMensagemNina(it) }
        }

        binding.btnEnviar.setOnClickListener {
            enviarMensagemParaNina()
        }
    }

    private fun enviarMensagemParaNina() {
        val texto = binding.etMensagem.text.toString().trim()
        if (texto.isEmpty()) return

        binding.etMensagem.setText("")

        val mensagemUsuario = ChatMessage(
            text = texto,
            isFromNina = false,
            status = MessageStatus.ENTREGUE
        )
        val indiceUsuario = adicionarMensagem(mensagemUsuario)
        NinaLegalService.getInstance()?.registrarInteracao()

        activityScope.launch {
            mensagemUsuario.status = MessageStatus.LIDA
            chatAdapter.notifyItemChanged(indiceUsuario)

            val indicePensando = adicionarMensagem(
                ChatMessage(
                    text = "Hmpf... pensando aqui. Nao me apressa.",
                    isFromNina = true
                )
            )

            val resposta = withContext(Dispatchers.IO) {
                val onboarding = NinaOnboarding(applicationContext)
                if (!onboarding.isDone()) {
                    onboarding.handleUserMessage(texto)
                } else {
                    val ajudaEmergencial = NinaEconomy.handleEmergencyMessage(applicationContext, texto)
                    if (ajudaEmergencial != null) {
                        return@withContext ajudaEmergencial
                    }

                    val indisponivel = NinaLegalService.getInstance()?.let { service ->
                        NinaCmd(service, applicationContext).mensagemIndisponibilidadeAtual()
                    }

                    indisponivel ?: run {
                        val ia = ninaIA ?: NinaIA(applicationContext).also { ninaIA = it }
                        val contextoExtra = listOf(
                            NinaTime.describeScale(),
                            NinaSchedule.getMessageContext(applicationContext),
                            NinaEconomy.getPromptContext(applicationContext)
                        ).filter { it.isNotBlank() }.joinToString("\n")
                        ia.responder(texto, contextoExtra)
                    }
                }
            }

            mensagens.removeAt(indicePensando)
            chatAdapter.notifyItemRemoved(indicePensando)
            adicionarMensagemNina(resposta)
        }
    }

    private fun adicionarMensagemNina(texto: String) {
        adicionarMensagem(ChatMessage(text = texto, isFromNina = true))
    }

    private fun adicionarMensagem(mensagem: ChatMessage): Int {
        mensagens.add(mensagem)
        val indice = mensagens.lastIndex
        chatAdapter.notifyItemInserted(indice)
        binding.rvChat.scrollToPosition(indice)
        return indice
    }

    private fun checkPermissions() {
        val overlayOk = Settings.canDrawOverlays(this)
        val usageOk = hasUsageStatsPermission()

        if (overlayOk && usageOk) {
            // Se já tem permissão, esconde a tela de config e mostra a Home Rosinha
            binding.permissionContainer.visibility = View.GONE
            binding.ninaHomeContainer.visibility = View.VISIBLE
            iniciarServicoNina()
        } else {
            // Se não tem, mostra os botões de permissão
            binding.permissionContainer.visibility = View.VISIBLE
            binding.ninaHomeContainer.visibility = View.GONE
            binding.btnAtivarNina.visibility = if (overlayOk || usageOk) View.VISIBLE else View.GONE
        }
    }

    private fun mostrarHomeNina() {
        atualizarRelogioDaNina()
        binding.permissionContainer.visibility = View.GONE
        binding.ninaHomeContainer.visibility = View.VISIBLE
        binding.chatContainer.visibility = View.GONE
        binding.agendaContainer.visibility = View.GONE
        binding.bankContainer.visibility = View.GONE
    }

    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 24 * 60 * 60 * 1000,
            now
        )
        return stats.isNotEmpty()
    }

    private fun iniciarServicoNina() {
        val intent = Intent(this, NinaLegalService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) {
            atualizarRelogioDaNina()
        }
        checkPermissions()
    }

    override fun onStart() {
        super.onStart()
        if (!receiverRegistrado) {
            val filter = IntentFilter(NinaLegalService.ACTION_NINA_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(ninaStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(ninaStateReceiver, filter)
            }
            receiverRegistrado = true
        }
    }

    override fun onStop() {
        if (receiverRegistrado) {
            unregisterReceiver(ninaStateReceiver)
            receiverRegistrado = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}
