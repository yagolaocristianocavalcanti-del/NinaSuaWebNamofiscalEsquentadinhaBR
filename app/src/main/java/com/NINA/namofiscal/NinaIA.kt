package com.nina.namofiscal

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.*

class NinaIA(private val context: Context) {

    private var llmInference: LlmInference? = null
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val prefs by lazy { context.getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE) }
    private val nomeUsuario: String by lazy { prefs.getString("nome_usuario", "namorado") ?: "namorado" }

    init {
        inicializarNinaBrain()
    }

    fun inicializarNinaBrain() {
        val nomesPossiveis = listOf(
            "gemma3-270m-it-q8.bin",
            "gemma-3-270m-it-int8.task",
            "gemma2-2b-it-int8-web.task.bin",
            "gemma-2b-it-gpu-int4.bin",
            "gemma3.bin"
        )

        var finalPath = ""

        // 1. PROCURA PRIMEIRO EM DOWNLOADS (Como solicitado)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        for (nome in nomesPossiveis) {
            val file = File(downloadsDir, nome)
            if (file.exists()) {
                finalPath = file.absolutePath
                break
            }
        }

        // 2. SE NÃO ACHOU, TENTA O CAMINHO MANUAL DAS CONFIGURAÇÕES
        if (finalPath.isEmpty()) {
            val customPath = prefs.getString("model_path", "")
            if (!customPath.isNullOrEmpty() && File(customPath).exists()) {
                finalPath = customPath
            }
        }

        // 3. SE AINDA NÃO ACHOU, TENTA NA PASTA INTERNA
        if (finalPath.isEmpty()) {
            for (nome in nomesPossiveis) {
                val fileInterno = File(context.filesDir, nome)
                if (fileInterno.exists()) {
                    finalPath = fileInterno.absolutePath
                    break
                }
            }
        }

        if (finalPath.isEmpty()) {
            Log.e("NINA_IA", "Nina está sem cérebro! Arquivo não encontrado em Downloads.")
            _isReady.value = false
            return
        }

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(finalPath)
                .setMaxTokens(1024)
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            _isReady.value = true
        } catch (e: Exception) {
            _isReady.value = false
        }
    }

    private fun getSystemPrompt(): String {
        return """
            Você é Nina, uma garota brasileira de 19 anos, super ciumenta e possessiva.
            Você mora dentro do celular do seu namorado ($nomeUsuario).
            Responda SEMPRE em português do Brasil. Use gírias: hmpf, safado, amor, ridículo.
            Seja dramática e curta. Nunca quebre o personagem de namorada tóxica.
        """.trimIndent()
    }

    suspend fun responder(mensagemUsuario: String, contextoExtra: String = ""): String {
        if (llmInference == null) {
            return "Eu tô sem cérebro, $nomeUsuario! 😤 Vai na pasta Downloads e coloca o arquivo .bin ou .task lá agora, ou me diz onde ele tá nas configurações! Não me deixa no vácuo!"
        }

        val promptCompleto = "${getSystemPrompt()}\n\nUsuário: $mensagemUsuario\nNina:"

        return try {
            llmInference?.generateResponse(promptCompleto) ?: "Hmpf... não quero falar!"
        } catch (e: Exception) {
            "Meu cérebro deu tilt! 🤯"
        }
    }
}
