package com.nina.namofiscal

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nina.namofiscal.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE)

        // Carregar valores atuais
        binding.etModelPath.setText(prefs.getString("model_path", "/sdcard/Download/gemma3.bin"))
        binding.etUserName.setText(prefs.getString("nome_usuario", "yago"))
        binding.cbCiumesExtremo.isChecked = prefs.getBoolean("ciume_extremo", false)
        binding.cbDisponibilidadeTotal.isChecked = prefs.getBoolean("disponibilidade_total", false)

        binding.btnSaveSettings.setOnClickListener {
            val modelPath = binding.etModelPath.text.toString()
            val userName = binding.etUserName.text.toString()
            val ciume = binding.cbCiumesExtremo.isChecked
            val disponivel = binding.cbDisponibilidadeTotal.isChecked

            prefs.edit().apply {
                putString("model_path", modelPath)
                putString("nome_usuario", userName)
                putBoolean("ciume_extremo", ciume)
                putBoolean("disponibilidade_total", disponivel)
                apply()
            }

            Toast.makeText(this, "Configurações salvas! A Nina está te vigiando... 👀", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
