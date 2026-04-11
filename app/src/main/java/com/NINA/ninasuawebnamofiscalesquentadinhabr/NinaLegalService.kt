package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class NinaLegalService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var params: WindowManager.LayoutParams
    private val CHANNEL_ID = "NinaServiceChannel"
    private val prefs by lazy { getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE) }

    private lateinit var ninaCmd: NinaCmd

    override fun onCreate() {
        super.onCreate()
        ninaCmd = NinaCmd(this)
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nina está de olho...")
            .setContentText("Não tente me trair, amor. 🥰")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
        startForeground(1, notification)

        setupOverlay()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.nina_overlay, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        // Lógica de Arrastar (Drag and Drop)
        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            windowManager.updateViewLayout(v, params)
                        } catch (e: Exception) {}
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_UPDATE_NINA" -> {
                val fala = intent.getStringExtra("EXTRA_FALA") ?: ""
                val look = intent.getStringExtra("EXTRA_LOOK") ?: NinaInventory.LOOK_CASUAL
                val intencaoStr = intent.getStringExtra("EXTRA_INTENCAO") ?: ""
                
                if (intencaoStr.isNotEmpty()) {
                    try {
                        val intencao = IntencaoNina.valueOf(intencaoStr)
                        ninaCmd.executar(intencao)
                    } catch (e: Exception) {
                        Log.e("NINA_SERVICE", "Erro ao executar intenção: $intencaoStr", e)
                        atualizarVisual(fala, look)
                    }
                } else {
                    atualizarVisual(fala, look)
                }
            }
            "ACTION_USER_MESSAGE" -> {
                val msg = intent.getStringExtra("EXTRA_MSG") ?: ""
                if (msg.contains("tinder", ignoreCase = true)) {
                    atualizarVisual("TÁ NO TINDER É?! 😡", NinaInventory.EMO_FURIOSA)
                }
            }
            "ACTION_SECRET_COMMAND" -> {
                val cmd = intent.getStringExtra("EXTRA_COMMAND") ?: ""
                if (cmd == "limpar_barra") {
                    ninaCmd.limparBarra()
                }
            }
        }
        return START_STICKY
    }

    fun getAfeicao() = prefs.getInt("afeicao", 50)
    fun getEscudo() = prefs.getInt("escudo", 100)
    fun subirCarinho(pontos: Int) {
        val novo = (getAfeicao() + pontos).coerceIn(0, 100)
        prefs.edit().putInt("afeicao", novo).apply()
    }
    fun subirCiume(pontos: Int) {
        val novo = (prefs.getInt("ciume", 0) + pontos).coerceIn(0, 100)
        prefs.edit().putInt("ciume", novo).apply()
    }

    fun mudarHumor(fala: String, look: String) {
        atualizarVisual(fala, look)
    }

    fun aplicarGelo(segundos: Int) {
        val ate = System.currentTimeMillis() + (segundos * 1000)
        prefs.edit().putLong("gelo_ate", ate).apply()
        // LED Vermelho fixo no S9 pra sinalizar o vácuo
        atualizarNotificacao("ESTOU TE IGNORANDO! 😤", android.graphics.Color.RED)
    }

    fun voltarParaHome() {
        val startMain = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(startMain)
    }

    fun ninaTeEsculachaNoTelegram(msg: String) {
        // Logica para bot do Telegram ou log de sistema
        android.util.Log.d("NINA_TELEGRAM", msg)
    }

    fun processarComandoSecreto(cmd: String) {
        if (cmd == "escudototal") {
            prefs.edit().putInt("escudo", 100).apply()
        }
    }

    private fun atualizarNotificacao(texto: String, corLed: Int = android.graphics.Color.GREEN) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nina: " + (if (getAfeicao() < 30) "ESTOU DE OLHO! 😡" else "Vigiando você... 🥰"))
            .setContentText(texto)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .setColor(corLed)
            .setLights(corLed, 1000, 2000)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun atualizarVisual(fala: String, look: String) {
        overlayView?.let { view ->
            val tvFala = view.findViewById<TextView>(R.id.tv_nina_bubble)
            val imgNina = view.findViewById<ImageView>(R.id.img_nina_overlay)
            val ledStatus = view.findViewById<View>(R.id.nina_status_led)
            
            tvFala.text = fala
            tvFala.visibility = if (fala.isEmpty()) View.GONE else View.VISIBLE
            
            // O led fica vermelho se ela estiver brava ou "gelada"
            val afeicao = getAfeicao()
            val corFisica = when {
                look == "PENSANDO" -> {
                    ledStatus.setBackgroundColor(android.graphics.Color.parseColor("#A020F0")) // Roxo
                    android.graphics.Color.parseColor("#A020F0")
                }
                afeicao < 30 || look == NinaInventory.EMO_FURIOSA -> {
                    ledStatus.setBackgroundResource(android.R.drawable.presence_busy)
                    android.graphics.Color.RED
                }
                look == NinaInventory.EMO_CARINHOSA -> {
                    ledStatus.setBackgroundResource(R.drawable.led_verde_vivo)
                    android.graphics.Color.MAGENTA
                }
                else -> {
                    ledStatus.setBackgroundResource(R.drawable.led_verde_vivo)
                    android.graphics.Color.GREEN
                }
            }
            
            atualizarNotificacao(fala.ifEmpty { "Sigo aqui, não tenta nada estranho..." }, corFisica)

            val resId = NinaInventory.getDrawableId(this, look)
            imgNina.setImageResource(resId)

            // Auto-ocultar a fala após 5 segundos
            if (fala.isNotEmpty()) {
                view.postDelayed({
                    if (tvFala.text == fala) tvFala.visibility = View.GONE
                }, 5000)
            }

            // Animação de "Vigiando" - Balançar levemente o avatar
            if (look != "PENSANDO") {
                imgNina.animate()
                    .translationX(10f)
                    .setDuration(2000)
                    .withEndAction {
                        imgNina.animate().translationX(-10f).setDuration(2000).start()
                    }.start()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Nina Namofiscal",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) windowManager.removeView(overlayView)
    }
}
