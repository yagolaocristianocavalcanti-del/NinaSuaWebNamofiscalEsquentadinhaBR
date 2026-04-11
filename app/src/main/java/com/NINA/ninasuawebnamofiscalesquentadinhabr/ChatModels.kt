package com.NINA.ninasuawebnamofiscalesquentadinhabr

data class Mensagem(
    val texto: String,
    val remetente: String, // "NINA" ou "USER"
    val horario: Long = System.currentTimeMillis()
)
