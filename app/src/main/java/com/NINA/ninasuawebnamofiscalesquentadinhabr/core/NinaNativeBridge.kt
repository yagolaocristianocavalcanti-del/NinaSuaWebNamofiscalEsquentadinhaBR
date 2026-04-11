package com.NINA.ninasuawebnamofiscalesquentadinhabr.core

class NinaNativeBridge {
    init {
        try {
            // Carrega o binário otimizado do PicoClaw baixado de picoclaw.io
            System.loadLibrary("picoclaw_core")
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("NINA_NATIVE", "Erro ao carregar libpicoclaw_core.so: ${e.message}")
        }
    }

    /**
     * Analisa o pitch da voz em tempo real.
     * Retorna a frequência em Hz (ex: >200Hz geralmente é feminina).
     */
    external fun analisarFrequenciaVoz(audioData: ByteArray): Float

    /**
     * Processa comandos rápidos com baixo consumo de RAM (<10MB).
     * Ideal para gatilhos imediatos antes de chamar a IA pesada.
     */
    external fun processarComandoRapido(input: String): Int
}
