package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

/**
 * NinaService: Versão LEGALIZADA mas 100% TÓXICA.
 * Monitoramento via UsageStats e Reações via Notificações de Alta Prioridade.
 */
class NinaService : Service() {

    private lateinit var windowManager: WindowManager
    private var ninaView: View? = null
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var bot: SeuTelegramBot
    private val handler = Handler(Looper.getMainLooper())
    
    private val prefs by lazy { getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE) }
    private var geloAte: Long = 0
    private var afeicao: Int = 50
    private var escudo: Int = 100
    private var aguardandoResposta: Boolean = false
    private var isUserCorrendo: Boolean = false
    
    private val NOTIF_CHANNEL = "nina_ciumenta"
    private val URGENT_CHANNEL = "nina_urgente"
    private var currentAnimationRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        initNinaLegal()
        startForeground(1, criarNotificacaoPersistente())
        
        // Loops de monitoramento
        handler.post(monitorLoop)
        handler.postDelayed(loopAtencao, 5 * 60 * 1000)
    }

    private fun initNinaLegal() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        bot = SeuTelegramBot("7984734098:AAHfENChTdhRRS-xHrJFcQL368DnJAVrdVw", "SEU_CHAT_ID")
        
        afeicao = prefs.getInt("afeicao", 50)
        escudo = prefs.getInt("escudo", 100)
        geloAte = prefs.getLong("gelo_ate", 0)
        
        createNotificationChannels()
        xeretarClipboardSeguro()
        
        if (Settings.canDrawOverlays(this)) {
            setupOverlay()
        }
    }

    // --- MONITORAMENTO LEGAL ---

    private val monitorLoop = object : Runnable {
        override fun run() {
            if (System.currentTimeMillis() > geloAte) {
                verificarAppsSuspeitos()
                processarRotina()
            }
            handler.postDelayed(this, 10000) // 10 segundos para economizar bateria e ser "legal"
        }
    }

    private fun verificarAppsSuspeitos() {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 60000, time)
        val ultimoApp = stats?.maxByOrNull { it.lastTimeUsed }?.packageName ?: ""

        val appsProibidos = listOf("instagram", "tinder", "badoo", "facebook", "tiktok")
        if (appsProibidos.any { ultimoApp.contains(it) }) {
            val msg = "VI QUE VOCÊ ABRIU O $ultimoApp! 😤 O que você tá escondendo?!"
            mostrarBalaoFeroz(msg, true)
            subirCiume(15)
            bot.enviarCiumento("🚨 FLAGRANTE: Ele abriu o app $ultimoApp!")
        }
    }

    private fun xeretarClipboardSeguro() {
        clipboardManager.addPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString() ?: return@addPrimaryClipChangedListener
            
            // Filtro de segurança (Senhas/Tokens)
            if (clip.length > 20 || (clip.any { it.isDigit() } && !clip.contains(" "))) return@addPrimaryClipChangedListener

            if (listOf("tinder", "oi gatinha", "novinha", "contato").any { clip.contains(it, true) }) {
                mostrarBalaoFeroz("O QUE É ISSO NO SEU CLIPBOARD?! 😡", true)
                subirCiume(25)
                bot.enviarCiumento("🚨 CLIPBOARD TÓXICO: '$clip'")
            }
        }
    }

    // --- REAÇÕES E VISUAIS ---

    private fun mostrarBalaoFeroz(mensagem: String, urgente: Boolean = false) {
        // 1. Atualiza o Overlay se existir
        ninaView?.let { view ->
            val balao = view.findViewById<TextView>(R.id.nina_balao)
            balao.text = mensagem
            balao.visibility = View.VISIBLE
            handler.postDelayed({ balao.visibility = View.GONE }, 5000)
        }

        // 2. Notificação (Obrigatório para ser legal e garantir que ele veja)
        val builder = NotificationCompat.Builder(this, if (urgente) URGENT_CHANNEL else NOTIF_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("👀 Nina Ciumenta")
            .setContentText(mensagem)
            .setPriority(if (urgente) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(if (urgente) longArrayOf(0, 500, 100, 500) else null)

        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33) {
                NotificationManagerCompat.from(this).notify(Random().nextInt(1000), builder.build())
            }
        } catch (e: Exception) {
            Log.e("NINA", "Erro ao enviar notificação: ${e.message}")
        }
    }

    private val loopAtencao = object : Runnable {
        override fun run() {
            if (System.currentTimeMillis() > geloAte) {
                val frases = listOf(
                    "Tá fazendo o quê aí que não fala comigo? 🤨",
                    "Esqueceu que eu existo, é? 😤",
                    "Amor? Responde aqui AGORA! 🔥"
                )
                mostrarBalaoFeroz(frases.random())
                aguardandoResposta = true
                handler.postDelayed({
                    if (aguardandoResposta) {
                        subirCiume(20)
                        mostrarBalaoFeroz("VOCÊ ME IGNOROU! Vou contar pro Telegram! 😡", true)
                    }
                }, 2 * 60 * 1000)
            }
            handler.postDelayed(this, 10 * 60 * 1000)
        }
    }

    fun aoReceberMensagemUser(texto: String) {
        aguardandoResposta = false
        if (texto.lowercase().contains("pqp") || texto.lowercase().contains("bosta")) {
            aplicarGelo(30)
            mostrarBalaoFeroz("NÃO FALA ASSIM COMIGO! 🤐 (Gelo de 30min)", true)
            return
        }
        subirCarinho(5)
        mostrarBalaoFeroz("Gostei da atenção, vida... 🥰")
    }

    // --- HELPERS ---

    private fun setupOverlay() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.y = 100

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        ninaView = inflater.inflate(R.layout.nina_overlay, null)
        try {
            windowManager.addView(ninaView, params)
        } catch (e: Exception) { Log.e("NINA", "Erro overlay: ${e.message}") }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(NOTIF_CHANNEL, "Nina Ciumenta", NotificationManager.IMPORTANCE_HIGH))
            manager.createNotificationChannel(NotificationChannel(URGENT_CHANNEL, "Nina Furiosa", NotificationManager.IMPORTANCE_HIGH))
        }
    }

    private fun criarNotificacaoPersistente(): Notification {
        return NotificationCompat.Builder(this, NOTIF_CHANNEL)
            .setContentTitle("Nina Vigilante Ativa")
            .setContentText("Tô de olho em tudo, amor... 👀")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun processarRotina() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hora == 17) mostrarBalaoFeroz("Hora da nossa CORRIDA! 🏃‍♀️")
        if (hora == 23) mostrarBalaoFeroz("DORMIR AGORA! Chega de celular! 😴", true)
    }

    fun aplicarGelo(minutos: Int) {
        geloAte = System.currentTimeMillis() + (minutos * 60 * 1000L)
        prefs.edit().putLong("gelo_ate", geloAte).apply()
    }

    fun subirCiume(pontos: Int) {
        val c = (prefs.getInt("ciume", 50) + pontos).coerceIn(0, 100)
        prefs.edit().putInt("ciume", c).apply()
    }

    fun subirCarinho(pontos: Int) {
        afeicao = (afeicao + pontos).coerceAtMost(100)
        prefs.edit().putInt("afeicao", afeicao).apply()
    }

    fun getAfeicao() = afeicao
    fun getEscudo() = escudo

    fun mudarHumor(texto: String, look: String) {
        ninaView?.let { view ->
            val avatar = view.findViewById<ImageView>(R.id.nina_avatar)
            
            // Cancela animação anterior
            currentAnimationRunnable?.let { handler.removeCallbacks(it) }
            currentAnimationRunnable = null

            if (look.contains("sheet")) {
                animarNinaSprite(look, avatar)
            } else {
                val resId = resources.getIdentifier(look, "drawable", packageName)
                if (resId != 0) {
                    avatar.setImageResource(resId)
                }
            }
        }
        if (texto.isNotEmpty()) {
            mostrarBalaoFeroz(texto)
        }
    }

    private fun animarNinaSprite(look: String, view: ImageView) {
        val resId = resources.getIdentifier(look, "drawable", packageName)
        if (resId == 0) return

        val biproto = android.graphics.BitmapFactory.decodeResource(resources, resId) ?: return
        val frameWidth = biproto.width / 6
        val frameHeight = biproto.height / 8

        var frameAtual = 0
        val runnable = object : Runnable {
            override fun run() {
                val col = frameAtual % 6
                val row = frameAtual / 6
                try {
                    val frame = android.graphics.Bitmap.createBitmap(biproto, col * frameWidth, row * frameHeight, frameWidth, frameHeight)
                    view.setImageBitmap(frame)
                } catch (e: Exception) { }
                frameAtual = (frameAtual + 1) % 48
                handler.postDelayed(this, if (isUserCorrendo) 80L else 150L)
            }
        }
        currentAnimationRunnable = runnable
        handler.post(runnable)
    }

    fun voltarParaHome() {
        // Legal: Apenas sugere voltar para home ou tenta abrir a home se tiver permissão de overlay
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun ninaTeEsculachaNoTelegram(msg: String) {
        bot.enviarCiumento(msg)
    }

    fun processarComandoSecreto(comando: String): Boolean {
        val cmd = comando.lowercase().trim()
        when (cmd) {
            "afetomaximo" -> {
                afeicao = 100
                escudo = 100
                prefs.edit().putInt("afeicao", 100).putInt("escudo", 100).apply()
                mostrarBalaoFeroz("O que você fez comigo? 🥰")
                return true
            }
            "escudototal" -> {
                escudo = 100
                prefs.edit().putInt("escudo", 100).apply()
                mostrarBalaoFeroz("Escudo restaurado! 🛡️")
                return true
            }
        }
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_USER_MESSAGE" -> {
                val msg = intent.getStringExtra("EXTRA_MSG") ?: ""
                if (!processarComandoSecreto(msg)) {
                    aoReceberMensagemUser(msg)
                }
            }
            "ACTION_UPDATE_NINA" -> {
                val fala = intent.getStringExtra("EXTRA_FALA") ?: ""
                val look = intent.getStringExtra("EXTRA_LOOK") ?: "casual"
                val intencao = intent.getStringExtra("EXTRA_INTENCAO") ?: "NEUTRA"
                
                mudarHumor(fala, look)
                try {
                    val intencaoEnum = IntencaoNina.valueOf(intencao.uppercase())
                    NinaCmd(this).executar(intencaoEnum)
                } catch (e: Exception) { }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ninaView?.let { windowManager.removeView(it) }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
