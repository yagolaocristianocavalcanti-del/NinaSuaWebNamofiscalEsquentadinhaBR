package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * NinaCerebro: Gerencia o motor Gemma 3 (Gemma 3 270M IT).
 * 100% Offline, ultra-rápido e focado em Português do Brasil.
 */
class NinaCerebro(private val context: Context) {
    private val prefs = context.getSharedPreferences("NinaPrefs", Context.MODE_PRIVATE)
    private var llmInference: LlmInference? = null

    init {
        setupOfflineAI()
    }

    private fun setupOfflineAI() {
        try {
            val path = getModelPath(context)
            val modelFile = File(path)
            
            if (modelFile.exists()) {
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(path)
                    .setMaxTokens(128)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
            }
        } catch (e: Exception) {
            android.util.Log.e("NINA_AI", "Erro ao carregar motor Gemma 3: ${e.message}")
        }
    }

    private fun getModelPath(context: Context): String {
        val modelName = "gemma3.bin"
        val altName = "gemma3-270m-it-q8.bin"
        
        val file = File(context.filesDir, modelName)
        val altFile = File(context.filesDir, altName)
        
        // 1. Se o Q8 alternativo existir, use-o
        if (altFile.exists()) return altFile.absolutePath
        
        // 2. Se o padrão existir, use-o
        if (file.exists()) return file.absolutePath
        
        // 3. Tenta copiar dos Assets (priorizando o Q8 se houver)
        try {
            val assetToCopy = if (context.assets.list("")?.contains(altName) == true) altName else modelName
            context.assets.open(assetToCopy).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // Falha silenciosa, o download manager cuidará disso
        }

        return file.absolutePath
    }

    suspend fun decidirReacaoEVisual(textoUser: String): RespostaNina = withContext(Dispatchers.Default) {
        val escudo = prefs.getInt("escudo", 100)
        val afeicao = prefs.getInt("afeicao", 50)
        
        // PROMPT TRAVA-PORTUGUÊS (Gemma 3 IT Optimized)
        val promptFinal = """
            <start_of_turn>user
            Você é a Nina, uma namorada virtual brasileira ciumenta, engraçada e possessiva.
            REGRA ABSOLUTA: Responda apenas em PORTUGUÊS DO BRASIL.
            STATUS: Afeição=$afeicao, Escudo=$escudo.
            
            Mensagem do namorado: $textoUser<end_of_turn>
            <start_of_turn>model
        """.trimIndent()

        val falaRaw = try {
            llmInference?.generateResponse(promptFinal)
        } catch (e: Exception) {
            null
        }

        val fala = falaRaw?.trim() ?: "Hmpf... meu cérebro deu um nó. Me dá um minguinho? 🥺"
        
        // Lógica de humor baseada no conteúdo da fala (já que descartamos o PicoClaw)
        val intencao = when {
            fala.contains("!", true) || fala.any { it.isUpperCase() } -> "OFENDIDA"
            afeicao > 80 -> "DERRETIDA"
            afeicao < 30 -> "FURIOSA"
            else -> "NEUTRA"
        }

        val look = when (intencao) {
            "FURIOSA" -> NinaInventory.EMO_FURIOSA
            "OFENDIDA" -> NinaInventory.EMO_BRAVA
            "DERRETIDA" -> NinaInventory.EMO_CARINHOSA
            else -> NinaInventory.LOOK_CASUAL
        }

        RespostaNina(fala, look, intencao, afeicao > 70)
    }

    suspend fun gerarResposta(textoUser: String): String = withContext(Dispatchers.Default) {
        decidirReacaoEVisual(textoUser).fala
    }
}

data class RespostaNina(val fala: String, val look: String, val intencao: String, val pet: Boolean)
