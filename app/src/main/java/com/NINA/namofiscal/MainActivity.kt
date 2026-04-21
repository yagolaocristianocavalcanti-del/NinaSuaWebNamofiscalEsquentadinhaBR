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

        setupChat()
        setupButtons()
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

    private fun atualizarStatusNina(texto: String, humor: String?, afeicao: Int, ciume: Int) {
        val humorAtual = humor ?: NinaInventory.EMO_NEUTRA
        binding.imgNinaHome.setImageResource(NinaInventory.getResourceForHumor(humorAtual))

        val statusBase = when (humorAtual) {
            NinaInventory.EMO_DORMINDO, NinaInventory.LOOK_PIJAMA -> "Dormindo"
            NinaInventory.EMO_CORRENDO, NinaInventory.LOOK_SPORT -> "Correndo"
            NinaInventory.EMO_TRABALHO, NinaInventory.LOOK_TRABALHO -> "Trabalhando"
            NinaInventory.EMO_PENSANDO -> "Pensando"
            NinaInventory.EMO_BRAVA, NinaInventory.EMO_IRRITADA, NinaInventory.EMO_EXIGENTE -> "Irritada"
            NinaInventory.EMO_FURIOSA -> "Furiosa"
            NinaInventory.EMO_CARINHOSA, NinaInventory.EMO_DERRETIDA -> "Carinhosa"
            else -> "Em casa"
        }

        val detalhe = when {
            texto.contains("reunião", ignoreCase = true) -> "em reunião"
            texto.isNotBlank() -> texto.take(42)
            else -> "afeição $afeicao | ciúme $ciume"
        }

        binding.tvNinaStatus.text = "Nina: $statusBase - $detalhe"
    }

    private fun setupChat() {
        chatAdapter = NinaChatAdapter(mensagens)
        binding.rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = chatAdapter

        adicionarMensagemNina("Oi, amor. Eu ja tava aqui te esperando... abre o jogo: o que voce veio fazer no celular? 😤")

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
                val indisponivel = NinaLegalService.getInstance()?.let { service ->
                    NinaCmd(service, applicationContext).mensagemIndisponibilidadeAtual()
                }

                indisponivel ?: run {
                    val ia = ninaIA ?: NinaIA(applicationContext).also { ninaIA = it }
                    ia.responder(texto)
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
        binding.permissionContainer.visibility = View.GONE
        binding.ninaHomeContainer.visibility = View.VISIBLE
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
