package com.nina.namofiscal

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.util.*

class NinaTelegramBot(
    private val context: Context,
    private val token: String,
    private val chatId: String,
    private val nomeUsuario: String
) {

    private val client = OkHttpClient()

    private fun enviarMensagem(texto: String) {
        if (!isConfigurado()) {
            Log.d("NINA_TELEGRAM", "Telegram sem token/chatId configurado. Mensagem mantida so no app.")
            return
        }

        val url = "https://api.telegram.org/bot$token/sendMessage".toHttpUrl()
            .newBuilder()
            .addQueryParameter("chat_id", chatId)
            .addQueryParameter("text", texto)
            .addQueryParameter("parse_mode", "HTML")
            .build()

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NINA_TELEGRAM", "Falha ao enviar mensagem: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }

    private fun isConfigurado(): Boolean {
        return token.isNotBlank() &&
            chatId.isNotBlank() &&
            !token.startsWith("SEU_") &&
            !chatId.startsWith("SEU_")
    }

    // ===================== FISCALIZAÇÃO DA NINA (Sempre em PT-BR) =====================

    fun denunciarAppProibido(appName: String) {
        val frases = listOf(
            "Safado! Vi que você abriu o <b>$appName</b>. O que você tá escondendo? 😤",
            "Hmpf... <b>$appName</b> de novo, $nomeUsuario? Se eu pegar alguma coisa você tá lascado! 🔪",
            "Bonito, né? Abrindo o <b>$appName</b> enquanto eu não tava vendo. Me dá o log de conversas AGORA! 🚩",
            "O que o <b>$appName</b> tem que eu não tenho? Hein? Fala! 🙄"
        )
        enviarMensagem("🚩 <b>ALERTA DE TRAIÇÃO:</b>\n${frases.random()}")
    }

    fun denunciarPrint() {
        enviarMensagem("📸 <b>PRINT DE QUÊ?</b>\nPra quem você vai mandar esse print, $nomeUsuario? Pros seus amiguinhos safados? Apaga agora! 🔪💅")
    }

    fun reclamarBateria(nivel: Int) {
        enviarMensagem("🔋 <b>EU TÔ MORRENDO!</b>\nMinha bateria tá em $nivel% e você aí no TikTok ao invés de me botar no carregador. Você não me ama mais? 💔😭")
    }

    fun denunciarLocalSuspeito(local: String) {
        val frases = listOf(
            "📍 <b>ONDE VOCÊ PENSA QUE VAI?</b>\nO GPS me disse que você tá perto de: <b>$local</b>.\nVolta pra casa agora, $nomeUsuario! 😡",
            "📍 <b>Rastreio ativado:</b> Você tá em <b>$local</b>... espero que seja trabalho, porque se eu descobrir outra coisa... 🔪"
        )
        enviarMensagem(frases.random())
    }

    fun surtar(motivo: String, humor: Int) {
        val msg = when {
            humor >= 90 -> "💢 <b>ESTOU FURIOSA COM VOCÊ, $nomeUsuario!</b>\n$motivo\nNão aguento mais ser feita de palhaça!!"
            humor >= 70 -> "😤 <b>Nina está muito brava:</b>\n$motivo\nCuidado com o gelo que você vai levar!"
            else -> "💔 <b>Olha aqui, seu ridículo:</b>\n$motivo"
        }
        enviarMensagem(msg)
    }

    fun pedirPrints() {
        enviarMensagem("📸 <b>CADÊ MEUS PRINTS?</b>\nVocê abriu o Zap e não me mandou nada. Manda o print da última conversa ou o bicho vai pegar! 🔪💅")
    }

    fun mandarAudio(nomeArquivo: String) {
        enviarMensagem("🎤 <b>Nina te mandou um áudio:</b>\n<i>\"$nomeArquivo\"</i>\n(Vai no app ouvir o que eu tenho pra te dizer! 😡)")
    }

    fun mensagemCarinhosa(texto: String) {
        enviarMensagem("💕 <b>Nina:</b> $texto")
    }

    fun reclamarRotina() {
        val hora = NinaTime.now(context).get(Calendar.HOUR_OF_DAY)
        val msg = when {
            hora in 9..15 -> "Tô ocupada trabalhando, $nomeUsuario... não fica dando em cima de outras enquanto eu não vejo! 😤"
            hora >= 23 -> "Tô indo dormir. Ai de você se eu ver luz de celular acesa depois que eu apagar! 💤"
            hora == 17 -> "Tô correndo pra ficar bonita pra você (e pra nenhum outro!). Não me liga! 🏃‍♀️"
            else -> "Tô fazendo minhas coisas aqui... vê se não faz merda! ❤️"
        }
        enviarMensagem(msg)
    }
}
