package com.nina.namofiscal

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nina.namofiscal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // MODO TELA CHEIA (Tirar tarja preta)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        checkPermissions()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
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

        binding.btnAbrirMercado.setOnClickListener {
            val nome = getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE).getString("nome_usuario", "yago")
            Toast.makeText(this, "Nina: 'Pode ir tirando o escorpião do bolso, $nome! Quero mimos! 💅🛍️'", Toast.LENGTH_LONG).show()
        }

        // Botão de Configurações (Engrenagem no Chat)
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
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
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
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
}
