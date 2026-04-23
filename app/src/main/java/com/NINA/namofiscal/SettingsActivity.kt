package com.nina.namofiscal

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.nina.namofiscal.databinding.ActivitySettingsBinding
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val brainPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult

        val copiedPath = copyBrainModelToInternalStorage(uri)
        if (copiedPath == null) {
            Toast.makeText(this, "Não consegui copiar esse cérebro. Tenta outro arquivo.", Toast.LENGTH_LONG).show()
            return@registerForActivityResult
        }

        binding.etModelPath.setText(copiedPath)
        getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("model_path", copiedPath)
            .apply()
        Toast.makeText(this, "Cérebro selecionado. Agora salva pra Nina usar.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE)

        // Carregar valores atuais
        binding.etModelPath.setText(prefs.getString("model_path", "/sdcard/Download/gemma3.bin"))
        binding.etUserName.setText(prefs.getString("nome_usuario", "user"))
        binding.cbCiumesExtremo.isChecked = prefs.getBoolean("ciume_extremo", false)
        binding.cbDisponibilidadeTotal.isChecked = prefs.getBoolean("disponibilidade_total", false)

        binding.btnSelectModel.setOnClickListener {
            brainPicker.launch(arrayOf("*/*"))
        }

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

    private fun copyBrainModelToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = sanitizeModelFileName(getDisplayName(uri) ?: "nina_brain.task")
            val brainDir = File(filesDir, "nina_brain").apply { mkdirs() }
            val target = File(brainDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            target.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun getDisplayName(uri: Uri): String? {
        return contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else {
                    null
                }
            }
    }

    private fun sanitizeModelFileName(name: String): String {
        val cleanName = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return cleanName.ifBlank { "nina_brain.task" }
    }
}
