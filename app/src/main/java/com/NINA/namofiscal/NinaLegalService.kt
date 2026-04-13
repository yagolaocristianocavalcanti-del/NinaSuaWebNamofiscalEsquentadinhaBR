package com.nina.namofiscal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class NinaLegalService : Service() {

    private var afeicao: Int = 50
    private var ciume: Int = 30
    private var emGelo: Boolean = false
    private var modoVigilanciaTelegram: Boolean = false
    private var ultimaInteracaoNina: Long = 0

    companion object {
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
        // Futura implementação de Overlay/TTS
    }

    fun subirCiume(pontos: Int) { 
        if (!isModoTeste()) ciume = (ciume + pontos).coerceIn(0, 100) 
    }

    fun subirCarinho(pontos: Int) { afeicao = (afeicao + pontos).coerceIn(0, 100) }

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

    private fun isModoTeste(): Boolean {
        return getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE).getBoolean("disponibilidade_total", false)
    }
}
