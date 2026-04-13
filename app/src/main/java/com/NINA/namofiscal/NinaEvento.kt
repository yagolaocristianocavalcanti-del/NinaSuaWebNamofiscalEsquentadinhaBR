package com.nina.namofiscal

sealed class NinaEvento {
    data class AppAberto(val packageName: String) : NinaEvento()
    data class ClipboardCopiado(val texto: String) : NinaEvento()
    object TelegramAberto : NinaEvento()
    object HorarioEspecial : NinaEvento()
    object PedidoFotoIntima : NinaEvento()
}
