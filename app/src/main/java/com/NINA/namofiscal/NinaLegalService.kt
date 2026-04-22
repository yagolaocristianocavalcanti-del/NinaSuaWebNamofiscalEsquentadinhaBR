package com.nina.namofiscal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat

class NinaLegalService : Service() {

    private var afeicao: Int = 50
    private var ciume: Int = 30
    private var emGelo: Boolean = false
    private var modoVigilanciaTelegram: Boolean = false
    private var ultimaInteracaoNina: Long = 0
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var overlayImage: ImageView? = null
    private var overlayBubble: TextView? = null

    companion object {
        const val ACTION_NINA_STATE = "com.nina.namofiscal.ACTION_NINA_STATE"
        const val EXTRA_TEXTO = "extra_texto"
        const val EXTRA_HUMOR = "extra_humor"
        const val EXTRA_AFEICAO = "extra_afeicao"
        const val EXTRA_CIUME = "extra_ciume"

        private const val CHANNEL_ID = "NinaForegroundServiceChannel"
        private const val NOTIFICATION_ID = 1
        private var instance: NinaLegalService? = null
        fun getInstance() = instance
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        NinaEconomy.ensureMonthlyCycle(this)
        mostrarOverlaySePermitido()
        Log.d("NINA_SERVICE", "Nina está viva e vigiando! 💅🚩")
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val nome = getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE).getString("nome_usuario", "namorado")
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nina está de olho... 👁️")
            .setContentText("Não pense em fazer besteira, $nome! 🔪")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Nina Vigilância", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    // ===================== MÉTODOS QUE A NINA CMD PRECISA =====================

    fun getAfeicao(): Int = if (isModoTeste()) 100 else afeicao
    
    fun registrarInteracao() { ultimaInteracaoNina = System.currentTimeMillis() }
    
    fun ultimaMensagemFoiComNina(): Boolean = (System.currentTimeMillis() - ultimaInteracaoNina) < 180000

    fun mudarHumor(texto: String, humor: String) {
        Log.d("NINA_LIVES", "Nina diz ($humor): $texto")
        atualizarOverlay(texto, humor)
        publicarEstado(texto, humor)
    }

    fun subirCiume(pontos: Int) { 
        if (!isModoTeste()) ciume = (ciume + pontos).coerceIn(0, 100)
        publicarEstado("", NinaInventory.EMO_IRRITADA)
    }

    fun subirCarinho(pontos: Int) {
        afeicao = (afeicao + pontos).coerceIn(0, 100)
        publicarEstado("", NinaInventory.EMO_CARINHOSA)
    }

    fun diminuirCarinho(pontos: Int) {
        afeicao = (afeicao - pontos).coerceIn(0, 100)
        publicarEstado("", NinaInventory.EMO_IRRITADA)
    }

    fun aplicarGelo(tempoMinutos: Int) {
        if (isModoTeste()) return
        emGelo = true
        Log.w("NINA_GELO", "Nina de gelo por $tempoMinutos min!")
    }

    fun voltarParaHome() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    fun temReuniao(): Boolean = false

    fun ativarModoVigilanciaTelegram() { modoVigilanciaTelegram = true }
    fun desativarModoVigilancia() { modoVigilanciaTelegram = false }
    fun estaNoModoVigilanciaTelegram(): Boolean = modoVigilanciaTelegram

    private fun mostrarOverlaySePermitido() {
        if (!Settings.canDrawOverlays(this) || overlayView != null) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.nina_overlay, null)
        overlayImage = view.findViewById(R.id.img_nina_overlay)
        overlayBubble = view.findViewById(R.id.tv_nina_bubble)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 18
            y = 120
        }

        try {
            windowManager?.addView(view, params)
            overlayView = view
            atualizarOverlay("", NinaInventory.EMO_NEUTRA)
        } catch (e: Exception) {
            Log.e("NINA_OVERLAY", "Falha ao abrir overlay: ${e.message}")
        }
    }

    private fun atualizarOverlay(texto: String, humor: String) {
        mostrarOverlaySePermitido()
        val saidaAtual = NinaSchedule.getActiveOuting(this)
        val visualStatus = when (saidaAtual?.companion) {
            NinaOutingCompanion.AMIGAS -> NinaVisualStatuses.get(NinaStatusKey.SAINDO_COM_AMIGAS)
            NinaOutingCompanion.AMIGO -> NinaVisualStatuses.get(NinaStatusKey.SAINDO_COM_AMIGO)
            else -> NinaVisualStatuses.fromHumor(humor, texto)
        }
        overlayImage?.setImageResource(visualStatus.imageRes)
        overlayBubble?.apply {
            if (texto.isBlank()) {
                visibility = View.GONE
            } else {
                text = texto
                visibility = View.VISIBLE
            }
        }
    }

    private fun removerOverlay() {
        val view = overlayView ?: return
        try {
            windowManager?.removeView(view)
        } catch (e: Exception) {
            Log.e("NINA_OVERLAY", "Falha ao remover overlay: ${e.message}")
        } finally {
            overlayView = null
            overlayImage = null
            overlayBubble = null
        }
    }

    private fun publicarEstado(texto: String, humor: String) {
        val intent = Intent(ACTION_NINA_STATE).apply {
            setPackage(packageName)
            putExtra(EXTRA_TEXTO, texto)
            putExtra(EXTRA_HUMOR, humor)
            putExtra(EXTRA_AFEICAO, getAfeicao())
            putExtra(EXTRA_CIUME, ciume)
        }
        sendBroadcast(intent)
    }

    private fun isModoTeste(): Boolean {
        return getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE).getBoolean("disponibilidade_total", false)
    }

    override fun onDestroy() {
        removerOverlay()
        if (instance === this) instance = null
        super.onDestroy()
    }
}
