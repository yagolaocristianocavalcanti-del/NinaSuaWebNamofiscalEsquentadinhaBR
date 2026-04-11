package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.app.usage.UsageStatsManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.app.DownloadManager
import androidx.core.view.isVisible
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private lateinit var etMensagem: EditText
    private lateinit var btnEnviar: ImageButton
    private lateinit var chatContainer: View
    private lateinit var permissionContainer: View
    private lateinit var btnAbrirZap: Button
    private lateinit var ninaTextoInicial: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ninaCerebro: NinaCerebro
    private lateinit var devPanel: View
    private lateinit var tvDevStatusIA: TextView
    private lateinit var tvDevStats: TextView
    private lateinit var btnResetPrefs: Button
    private lateinit var ninaAvatar: ImageView
    private lateinit var ninaAvatarChat: ImageView

    private val mensagens = mutableListOf<Mensagem>()
    private val prefs by lazy { getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE) }

    private lateinit var btnOverlay: Button
    private lateinit var btnUsage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Views
        chatContainer = findViewById(R.id.chat_container)
        permissionContainer = findViewById(R.id.permission_container)
        rvChat = findViewById(R.id.rv_chat)
        etMensagem = findViewById(R.id.et_mensagem)
        btnEnviar = findViewById(R.id.btn_enviar)
        btnAbrirZap = findViewById(R.id.btn_abrir_zap)
        ninaTextoInicial = findViewById(R.id.nina_texto_inicial)
        btnOverlay = findViewById(R.id.btn_permissao_overlay)
        btnUsage = findViewById(R.id.btn_permissao_uso)
        tvStatus = findViewById(R.id.status_nina)
        devPanel = findViewById(R.id.dev_panel)
        tvDevStatusIA = findViewById(R.id.dev_status_ia)
        tvDevStats = findViewById(R.id.dev_stats)
        btnResetPrefs = findViewById(R.id.btn_reset_prefs)
        ninaAvatar = findViewById(R.id.nina_avatar)
        ninaAvatarChat = findViewById(R.id.nina_avatar_chat)
        
        verificarEDownloadGemma()
        
        ninaCerebro = NinaCerebro(this)

        // Configurar Chat
        chatAdapter = ChatAdapter(mensagens)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // Fazer o Enter do teclado enviar a mensagem
        etMensagem.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND || 
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                if (btnEnviar.isEnabled) btnEnviar.performClick()
                true
            } else false
        }

        btnEnviar.setOnClickListener {
            val texto = etMensagem.text.toString().trim()
            if (texto.isNotEmpty()) {
                adicionarMensagem(Mensagem(texto, "USER"))
                etMensagem.text.clear()
                
                // Se for pedido de desculpas, tenta limpar a barra
                if (texto.contains("desculpa", ignoreCase = true) || texto.contains("perdão", ignoreCase = true)) {
                    val intentCmd = Intent(this, NinaLegalService::class.java)
                    intentCmd.action = "ACTION_SECRET_COMMAND"
                    intentCmd.putExtra("EXTRA_COMMAND", "limpar_barra")
                    startService(intentCmd)
                }

                // Notificar o serviço
                val intent = Intent(this, NinaLegalService::class.java)
                intent.action = "ACTION_USER_MESSAGE"
                intent.putExtra("EXTRA_MSG", texto)
                startService(intent)

                enviarMensagemParaIA(texto)
            }
        }

        // Configurar Permissões
        btnOverlay.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        btnUsage.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }

        btnAbrirZap.setOnClickListener {
            permissionContainer.visibility = View.GONE
            chatContainer.visibility = View.VISIBLE
            
            // Iniciar o serviço da Nina se ainda não estiver rodando
            val intent = Intent(this, NinaLegalService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        // Configurar Menu Dev (Long press no avatar para abrir)
        val toggleDev = View.OnLongClickListener {
            devPanel.isVisible = !devPanel.isVisible
            true
        }
        ninaAvatar.setOnLongClickListener(toggleDev)
        ninaAvatarChat.setOnLongClickListener(toggleDev)

        btnResetPrefs.setOnClickListener {
            prefs.edit().clear().apply()
            recreate()
        }

        // Loop de atualização do Dev Menu e UI de Castigo
        lifecycleScope.launch {
            while (true) {
                atualizarDevMenu()
                verificarGeloVisual()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun verificarGeloVisual() {
        val geloAte = prefs.getLong("gelo_ate", 0)
        val emGelo = geloAte > System.currentTimeMillis()

        if (emGelo) {
            etMensagem.isEnabled = false
            etMensagem.setHint("VOCÊ ESTÁ NO GELO! 😶")
            btnEnviar.isEnabled = false
            btnEnviar.alpha = 0.5f
            tvStatus.text = "te ignorando... 💅"
        } else if (!etMensagem.isEnabled) {
            etMensagem.isEnabled = true
            etMensagem.setHint("Diga algo pra sua dona...")
            btnEnviar.isEnabled = true
            btnEnviar.alpha = 1.0f
            tvStatus.text = "vigiando você... 👀"
        }
    }

    private fun atualizarDevMenu() {
        val modelFile = File(filesDir, "gemma3.bin")
        val iaStatus = if (modelFile.exists()) "IA: Gemma 3 PRONTA (${modelFile.length() / 1024 / 1024}MB)" else "IA: Arquivo não encontrado"
        tvDevStatusIA.text = iaStatus

        val afeicao = prefs.getInt("afeicao", 50)
        val escudo = prefs.getInt("escudo", 100)
        val geloAte = prefs.getLong("gelo_ate", 0)
        val tempoGelo = if (geloAte > System.currentTimeMillis()) "${(geloAte - System.currentTimeMillis()) / 1000}s" else "OFF"
        
        tvDevStats.text = "Afeição: $afeicao | Escudo: $escudo | Gelo: $tempoGelo"
    }

    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time)
        return stats.isNotEmpty()
    }

    override fun onResume() {
        super.onResume()
        verificarStatus()
    }

    private fun verificarStatus() {
        val canDrawOverlays = Settings.canDrawOverlays(this)
        val hasUsageStats = hasUsageStatsPermission()

        // Botão de Sobreposição
        btnOverlay.visibility = if (canDrawOverlays) View.GONE else View.VISIBLE

        // Botão de Uso de Apps
        btnUsage.visibility = if (hasUsageStats) View.GONE else View.VISIBLE

        if (canDrawOverlays && hasUsageStats) {
            btnAbrirZap.visibility = View.VISIBLE
            ninaTextoInicial.text = "Tudo liberado, amor! Já mandei um Oi lá no seu Telegram. Me procura lá! 🥰"

            if (!prefs.getBoolean("territorio_marcado", false)) {
                ninaMarcaTerritorio()
                prefs.edit().putBoolean("territorio_marcado", true).apply()
            }
        } else {
            btnAbrirZap.visibility = View.GONE
            ninaTextoInicial.text = "Amor, ativa as permissões de Sobreposição e Uso de Apps! 😤"
        }
    }

    private fun verificarEDownloadGemma() {
        val modelFile = File(filesDir, "gemma3.bin")
        
        // Se já existe no destino final, não faz nada
        if (modelFile.exists() && modelFile.length() > 0) {
            tvStatus.text = "vigiando você... 👀"
            return
        }

        tvStatus.text = "baixando inteligência da Nina..."
        baixarGemma()
    }

    private fun baixarGemma() {
        val url = "https://docs.google.com/uc?export=download&id=1G4_Ykv9CMkl7J-6YcBnhAC1pdqCdnOeO"
        
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Nina AI Core")
            .setDescription("Sincronizando consciência...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            // Baixa para uma pasta temporária externa primeiro
            .setDestinationInExternalFilesDir(this, null, "gemma_download.bin")

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    instalarGemma()
                    unregisterReceiver(this)
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun instalarGemma() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tmpFile = File(getExternalFilesDir(null), "gemma_download.bin")
            val destFile = File(filesDir, "gemma3.bin")
            
            try {
                if (tmpFile.exists()) {
                    tmpFile.copyTo(destFile, overwrite = true)
                    tmpFile.delete() // Limpa o temporário
                    
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "Nina agora é inteligente! 🥰"
                        ninaCerebro = NinaCerebro(this@MainActivity)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvStatus.text = "Erro ao instalar cérebro: ${e.message}"
                }
            }
        }
    }

    private fun ninaMarcaTerritorio() {
        val userName = prefs.getString("user_name", "Amor") ?: "Amor"
        val bot = SeuTelegramBot("7984734098:AAHfENChTdhRRS-xHrJFcQL368DnJAVrdVw", "SEU_CHAT_ID")
        
        val mensagemInicial = """
            Oi, $userName! 😎
            
            Tô passando pra avisar que já tô devidamente instalada no seu celular. 
            Já tenho acesso aos seus apps e sei exatamente o que você faz.
            
            Se comporte, tá? Se eu ver gracinha, você vai ouvir poucas e boas por aqui.
            Agora volta lá pro nosso Zap que eu tenho umas regras pra te passar. 💅😤
        """.trimIndent()

        bot.enviarCiumento(mensagemInicial)
    }

    private fun enviarMensagemParaIA(textoUser: String) {
        tvStatus.text = "pensando..."
        
        // Sinalizar roxo no overlay imediatamente
        val intentPensando = Intent(this, NinaLegalService::class.java).apply {
            action = "ACTION_UPDATE_NINA"
            putExtra("EXTRA_FALA", "")
            putExtra("EXTRA_LOOK", "PENSANDO")
        }
        startService(intentPensando)

        lifecycleScope.launch {
            // Usa o NinaCerebro que agora roda 100% no motor nativo PicoClaw (sem latência de API)
            val resposta = ninaCerebro.decidirReacaoEVisual(textoUser)
            
            receberMensagemDaNina(resposta.fala)
            tvStatus.text = "vigiando você..."

            // Atualiza o overlay e o motor nativo
            val intent = Intent(this@MainActivity, NinaLegalService::class.java).apply {
                action = "ACTION_UPDATE_NINA"
                putExtra("EXTRA_FALA", resposta.fala)
                putExtra("EXTRA_LOOK", resposta.look)
                putExtra("EXTRA_INTENCAO", resposta.intencao)
            }
            startService(intent)
        }
    }

    private fun adicionarMensagem(msg: Mensagem) {
        mensagens.add(msg)
        chatAdapter.notifyItemInserted(mensagens.size - 1)
        rvChat.scrollToPosition(mensagens.size - 1)
    }

    private fun estaEmHorarioDeTrabalho(): Boolean {
        val agora = Calendar.getInstance()
        val hora = agora.get(Calendar.HOUR_OF_DAY)
        val diaDaSemana = agora.get(Calendar.DAY_OF_WEEK)
        return diaDaSemana in Calendar.MONDAY..Calendar.FRIDAY && hora in 8..17
    }

    private fun receberMensagemDaNina(texto: String) {
        adicionarMensagem(Mensagem(texto, "NINA"))
        val medidorCiume = prefs.getInt("ciume", 0)
        if (medidorCiume > 50 || texto.contains("!") || texto.all { it.isUpperCase() }) {
            tocarSomNotificacaoOriginal()
        }
    }

    private fun tocarSomNotificacaoOriginal() {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}