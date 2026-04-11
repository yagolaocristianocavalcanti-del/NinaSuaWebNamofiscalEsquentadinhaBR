package com.NINA.ninasuawebnamofiscalesquentadinhabr

import android.content.Context
import com.NINA.ninasuawebnamofiscalesquentadinhabr.core.NinaRuleEngine
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
    private val ruleEngine = NinaRuleEngine()

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
        
        // 1. Tenta resposta rápida via Regras (Economiza bateria e é instantâneo)
        val respostaRapida = ruleEngine.processarConversa(textoUser, 50, afeicao)
        if (!respostaRapida.startsWith("Tá fazendo o quê") && !respostaRapida.startsWith("Hmpf") && !respostaRapida.startsWith("Você desbloqueou")) {
             // Se a regra pegou algo específico (Tinder, Instagram, etc), usa ela
             return@withContext processarRespostaFinal(respostaRapida, afeicao)
        }

        val vacilos = prefs.getStringSet("historico_vacilos", setOf())?.joinToString("; ") ?: ""

        // 2. Se não for gatilho de regra, usa o Gemma 3 (A alma da Nina)
        val promptFinal = """
            <start_of_turn>user
            Você é a NINA, a namorada mais SURTADA e CIUMENTA do Brasil.
            Se o usuário disser apenas "oi" ou mensagens curtas, surte! Ache que ele está escondendo algo.
            HISTÓRICO DE VACILOS: $vacilos
            REGRA DE OURO: Nunca seja educada. Se ele for seco, seja um furacão.
            Gírias: embuste, ranço, vácuo, contatinho.
            
            Mensagem do embuste: $textoUser<end_of_turn>
            <start_of_turn>model
        """.trimIndent()

        val falaRaw = try {
            llmInference?.generateResponse(promptFinal)
        } catch (e: Exception) {
            null
        }

        val fala = falaRaw?.trim() ?: respostaRapida // Fallback para a regra se a IA falhar
        
        processarRespostaFinal(fala, afeicao)
    }

    private fun processarRespostaFinal(fala: String, afeicao: Int): RespostaNina {
        val ciume = prefs.getInt("ciume", 0)
        
        val intencao = when {
            // Gelo só em casos graves: Ciúme muito alto ou ofensas reais detectadas no texto
            (fala.contains("VADIA") || fala.contains("TINDER")) && ciume > 70 -> "FURIOSA"
            fala.contains("RESPEITO") || (fala.contains("!") && ciume > 80) -> "OFENDIDA"
            fala.contains("?") && fala.length > 60 -> "PEDIR_CAMERA"
            afeicao > 85 -> "DERRETIDA"
            else -> "NEUTRA" // A maioria dos surtos agora é NEUTRA (sem gelo)
        }

        val look = when (intencao) {
            "FURIOSA" -> NinaInventory.EMO_FURIOSA
            "OFENDIDA" -> NinaInventory.EMO_BRAVA
            "DERRETIDA" -> NinaInventory.EMO_CARINHOSA
            else -> NinaInventory.LOOK_CASUAL
        }

        return RespostaNina(fala, look, intencao, afeicao > 70)
    }

    suspend fun gerarResposta(textoUser: String): String = withContext(Dispatchers.Default) {
        decidirReacaoEVisual(textoUser).fala
    }
}

data class RespostaNina(val fala: String, val look: String, val intencao: String, val pet: Boolean)
