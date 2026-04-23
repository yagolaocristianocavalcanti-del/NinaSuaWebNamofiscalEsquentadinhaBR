package com.nina.namofiscal

import android.app.AlertDialog
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
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

            atualizarClimaDaNina(texto, humor)
            if (texto.isBlank()) return

            if (binding.chatContainer.visibility == View.VISIBLE) {
                adicionarMensagemNina(texto)
            }
            // Removido o Toast para que a fala apareça apenas no balão flutuante (overlay)
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
        atualizarClimaDaNina("", NinaInventory.EMO_NEUTRA)
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
            aplicarEfeitoPulse(it)
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        // Permissão de Uso
        binding.btnPermissaoUso.setOnClickListener {
            aplicarEfeitoPulse(it)
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        // Botão "TUDO PRONTO" (Ativa a Home)
        binding.btnAtivarNina.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarHomeNina()
            iniciarServicoNina()
        }

        // Abrir o WhatsApp da Nina (dentro da Home dela)
        binding.btnAbrirZap.setOnClickListener {
            aplicarEfeitoPulse(it)
            binding.ninaHomeContainer.visibility = View.GONE
            binding.agendaContainer.visibility = View.GONE
            binding.bankContainer.visibility = View.GONE
            binding.chatContainer.visibility = View.VISIBLE
        }

        binding.btnHomeSettings.setOnClickListener {
            aplicarEfeitoPulse(it)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnAbrirShein.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarAppDaNina(NinaStoreApp.SHEIN)
        }

        binding.btnAbrirIfood.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarAppDaNina(NinaStoreApp.IFOOD)
        }

        binding.btnAbrirBoticario.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarAppDaNina(NinaStoreApp.BOTICARIO)
        }

        binding.btnAbrirPetlove.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarAppDaNina(NinaStoreApp.PETLOVE)
        }

        binding.btnAbrirDoceria.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarAppDaNina(NinaStoreApp.DOCERIA)
        }

        binding.btnAbrirFlores.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarAppDaNina(NinaStoreApp.FLORICULTURA)
        }

        binding.btnAbrirEntregaIfood.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarMiniGameEmBreve()
        }

        binding.btnAbrirAgenda.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarAgendaDaNina()
        }

        binding.btnAbrirBanco.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarBancoDaNina()
        }

        binding.btnFecharAgenda.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarHomeNina()
        }

        binding.btnFecharBanco.setOnClickListener {
            aplicarEfeitoPulse(it)
            mostrarHomeNina()
        }

        // Botão de Configurações (Engrenagem no Chat)
        binding.btnSettings.setOnClickListener {
            aplicarEfeitoPulse(it)
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun mostrarAppDaNina(app: NinaStoreApp) {
        processarCorreioDaNina()
        NinaEconomy.ensureMonthlyCycle(applicationContext)
        val itens = NinaInventory.getStoreItems(app)
        val labels = itens.map { item ->
            buildString {
                append("${NinaInventory.emojiFor(item)} ${item.nome}\n")
                append(NinaEconomy.storePriceLabel(item.preco))
                if (item.intimidadeMinima > 0) {
                    append("\nAfinidade minima: ${item.intimidadeMinima}")
                }
                if (item.isIntimo) {
                    append(" | intimo")
                }
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("${NinaInventory.emojiFor(app)} ${app.titulo}")
            .setMessage(
                "${app.descricao}\n\n" +
                    "Toque em um item para comprar e mandar ao correio da Nina.\n" +
                    "${NinaMail.pendingSummary(applicationContext)}"
            )
            .setItems(labels) { _, index ->
                comprarItemDaLoja(itens[index])
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

    private fun comprarItemDaLoja(item: NinaItem) {
        val service = NinaLegalService.getInstance()
        if (service == null) {
            iniciarServicoNina()
            Toast.makeText(this, "Ative a Nina primeiro pra mandar pacote ao correio dela.", Toast.LENGTH_LONG).show()
            return
        }
        NinaCmd(service, applicationContext).comprarItem(item.id)
        Toast.makeText(
            this,
            "${NinaInventory.emojiFor(item)} ${item.nome} enviado ao correio da Nina.",
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

    private fun atualizarClimaDaNina(texto: String, humor: String?) {
        aplicarAtmosferaDaNina(texto, humor)
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
            aplicarAtmosferaDaNina(resposta, NinaInventory.EMO_PENSANDO)
        }
    }

    private fun aplicarAtmosferaDaNina(texto: String = "", humor: String? = null) {
        val atmosfera = NinaPhoneAtmospheres.from(applicationContext, humor, texto)
        binding.root.setBackgroundColor(atmosfera.wallpaper)
        binding.permissionContainer.setBackgroundColor(atmosfera.surface)
        binding.ninaHomeContainer.setBackgroundColor(atmosfera.wallpaper)
        binding.agendaContainer.setBackgroundColor(atmosfera.surface)
        binding.bankContainer.setBackgroundColor(atmosfera.surface)
        binding.chatContainer.setBackgroundColor(atmosfera.chatBackground)
        binding.headerZap.setBackgroundColor(atmosfera.header)
        binding.headerAgenda.setBackgroundColor(atmosfera.header)
        binding.headerBank.setBackgroundColor(atmosfera.header)
        binding.btnEnviar.backgroundTintList = ColorStateList.valueOf(atmosfera.accent)
        binding.tvAgendaSubtitle.setTextColor(atmosfera.subtleText)
        binding.tvBankSubtitle.setTextColor(atmosfera.subtleText)
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
        processarCorreioDaNina()
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
            aplicarAtmosferaDaNina()
            processarCorreioDaNina()
        }
        checkPermissions()
    }

    private fun processarCorreioDaNina() {
        val service = NinaLegalService.getInstance() ?: return
        val entregas = NinaCmd(service, applicationContext).processarCorreioDaNina()
        if (binding.chatContainer.visibility == View.VISIBLE) {
            entregas.forEach { adicionarMensagemNina(it.message) }
        }
    }

    private fun aplicarEfeitoPulse(view: View) {
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.icon_pulse)
        view.startAnimation(animation)
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
