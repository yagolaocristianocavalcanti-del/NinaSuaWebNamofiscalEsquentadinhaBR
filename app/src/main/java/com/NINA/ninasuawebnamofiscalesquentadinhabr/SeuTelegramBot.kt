package com.NINA.ninasuawebnamofiscalesquentadinhabr

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException

class SeuTelegramBot(private val token: String, private val chatId: String) {
    private val client = OkHttpClient()
    private val baseUrl = "https://api.telegram.org/bot$token/"
    
    fun enviarCiumento(mensagem: String) {
        enviarMensagemNoZapFake(mensagem)
    }

    fun enviarMensagemNoZapFake(mensagem: String) {
        val url = baseUrl.toHttpUrl().newBuilder()
            .addPathSegment("sendMessage")
            .addQueryParameter("chat_id", chatId)
            .addQueryParameter("text", mensagem)
            .build()
        
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }

    fun enviarAudio(nomeArquivo: String) {
        // Por enquanto, avisa no Telegram que um áudio foi "enviado" no Zap
        enviarCiumento("🎤 Nina acabou de te mandar um áudio no Zap: $nomeArquivo")
    }
}
