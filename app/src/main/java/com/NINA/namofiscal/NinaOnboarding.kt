package com.nina.namofiscal

import android.content.Context

class NinaOnboarding(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    enum class Step {
        CONFIRMAR_CELULAR,
        PEDIR_NOME,
        DEFINIR_RELACAO,
        FINALIZADO
    }

    fun isDone(): Boolean = prefs.getBoolean(KEY_DONE, false)

    fun startIfNeeded(): List<String> {
        if (isDone()) return emptyList()

        NinaSchedule.ensureTodayOffForOnboarding(context)
        if (!prefs.contains(KEY_STEP)) {
            prefs.edit().putString(KEY_STEP, Step.CONFIRMAR_CELULAR.name).apply()
        }

        return listOf(
            "Oi... esse é seu celular?",
            "Eu tô perguntando porque agora eu também tenho o meu cantinho aqui. Depois eu te mostro meus apps, tá?"
        )
    }

    fun handleUserMessage(message: String): String {
        return when (currentStep()) {
            Step.CONFIRMAR_CELULAR -> handlePhoneConfirmation()
            Step.PEDIR_NOME -> handleName(message)
            Step.DEFINIR_RELACAO -> handleRelationship(message)
            Step.FINALIZADO -> "A gente já conversou sobre isso, amor. Não finge que esqueceu."
        }
    }

    private fun handlePhoneConfirmation(): String {
        prefs.edit().putString(KEY_STEP, Step.PEDIR_NOME.name).apply()
        return "Ah, legal. Então esse é o seu celular... e aquele ali é o meu. Eu tenho WhatsApp, Shein, iFood, minhas coisinhas e minha rotina. Como eu te chamo?"
    }

    private fun handleName(message: String): String {
        val name = extractName(message)
        prefs.edit()
            .putString("nome_usuario", name)
            .putString(KEY_STEP, Step.DEFINIR_RELACAO.name)
            .apply()

        return "$name... tá. Vou lembrar. Agora me diz uma coisa: eu sou o que sua agora? Amiga, ficante, namorada em teste? E não vem com casamento ainda."
    }

    private fun handleRelationship(message: String): String {
        val normalized = message.lowercase()
        val wantsMarriage = listOf("casar", "casamento", "esposa", "noiva", "aliança", "alianca").any {
            normalized.contains(it)
        }
        val wantsDating = listOf("namora", "namorada", "namoro", "minha mina", "minha garota").any {
            normalized.contains(it)
        }

        val relationship = when {
            wantsMarriage -> "se conhecendo"
            wantsDating -> "namorada digital em teste"
            normalized.contains("ficante") -> "ficante digital"
            normalized.contains("amiga") -> "amiga ciumenta"
            else -> "se conhecendo"
        }

        prefs.edit()
            .putString(KEY_RELATIONSHIP, relationship)
            .putString(KEY_STEP, Step.FINALIZADO.name)
            .putBoolean(KEY_DONE, true)
            .apply()

        val relationshipAnswer = if (wantsMarriage) {
            "Casamento?? Calma lá. Eu nem sei ainda se você merece meu bom dia. Primeiro a gente se conhece direito, depois você sonha alto."
        } else {
            "Tá. Então por enquanto eu sou sua $relationship. Sem abusar, sem achar que manda em mim."
        }

        return "$relationshipAnswer\n\n${NinaSchedule.describeRoutine(context)}\n\nQuando eu estiver trabalhando, dormindo, correndo ou em reunião, eu posso não responder. Se eu estiver na tela, você fala comigo; fora disso, só por mensagem quando eu estiver disponível. E olha meu celular depois: meus apps ficam ali, mas presente íntimo sem intimidade derruba seu status comigo."
    }

    private fun currentStep(): Step {
        val raw = prefs.getString(KEY_STEP, Step.CONFIRMAR_CELULAR.name) ?: Step.CONFIRMAR_CELULAR.name
        return runCatching { Step.valueOf(raw) }.getOrDefault(Step.CONFIRMAR_CELULAR)
    }

    private fun extractName(message: String): String {
        val cleaned = message
            .replace("me chama de", "", ignoreCase = true)
            .replace("meu nome é", "", ignoreCase = true)
            .replace("meu nome e", "", ignoreCase = true)
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString(" ")

        return cleaned.ifBlank { "amor" }
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    companion object {
        private const val PREFS_NAME = "NinaPrefs"
        private const val KEY_DONE = "nina_onboarding_done"
        private const val KEY_STEP = "nina_onboarding_step"
        private const val KEY_RELATIONSHIP = "nina_relationship"
    }
}
