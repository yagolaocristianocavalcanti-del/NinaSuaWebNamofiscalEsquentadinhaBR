package com.NINA.ninasuawebnamofiscalesquentadinhabr.core

import android.util.Log

class NinaCore {
    private var useNative = false

    init {
        try {
            System.loadLibrary("picoclaw_nina")
            useNative = true
            Log.d("NINA_CORE", "Motor PicoClaw carregado com sucesso! 🚀")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("NINA_CORE", "Biblioteca nativa não encontrada. Usando modo de compatibilidade Kotlin. 🛡️")
            useNative = false
        }
    }

    // --- Métodos Nativos ---
    private external fun nativeInitNina(afeicao: Int, escudo: Int)
    private external fun nativeProcessarConversa(input: String): String
    private external fun nativeObterLookDoDia(): String
    private external fun nativeRegistrarAtividade(app: String, tempo: Long)
    private external fun nativeObterHumorAtual(): Int
    private external fun nativeVerificarEscudo(): Int
    private external fun nativeProcessarCompraNativa(idItem: String, preco: Int): Boolean
    private external fun nativeObterReacaoMimo(idItem: String): String

    // --- Ponte Segura (Evita UnsatisfiedLinkError) ---

    fun initNina(afeicao: Int, escudo: Int) {
        if (useNative) try { nativeInitNina(afeicao, escudo) } catch (e: Throwable) { useNative = false }
    }

    fun processarConversa(input: String): String {
        return if (useNative) {
            try { nativeProcessarConversa(input) } catch (e: Throwable) { "Hmpf... meu cérebro deu um nó. 🤨" }
        } else {
            "Estou te ouvindo (Modo Seguro), mas responda com carinho! ❤️"
        }
    }

    fun obterLookDoDia(): String {
        return if (useNative) try { nativeObterLookDoDia() } catch (e: Throwable) { "nina_casual_pink" } else "nina_casual_pink"
    }

    fun registrarAtividade(app: String, tempo: Long) {
        if (useNative) try { nativeRegistrarAtividade(app, tempo) } catch (e: Throwable) {}
    }

    fun obterHumorAtual(): Int {
        return if (useNative) try { nativeObterHumorAtual() } catch (e: Throwable) { 50 } else 50
    }

    fun verificarEscudo(): Int {
        return if (useNative) try { nativeVerificarEscudo() } catch (e: Throwable) { 100 } else 100
    }

    fun processarCompraNativa(idItem: String, preco: Int): Boolean {
        return if (useNative) try { nativeProcessarCompraNativa(idItem, preco) } catch (e: Throwable) { true } else true
    }

    fun obterReacaoMimo(idItem: String): String {
        return if (useNative) try { nativeObterReacaoMimo(idItem) } catch (e: Throwable) { "Obrigada pelo mimo! 🥰" } else "Obrigada pelo mimo! 🥰"
    }
}
