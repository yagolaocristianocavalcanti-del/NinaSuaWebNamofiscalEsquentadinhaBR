package com.nina.namofiscal.model

data class Ticket(
    val id: String,
    val vehiclePlate: String,
    val entryTime: Long,
    val status: TicketStatus = TicketStatus.PENDENTE,
    val paymentTime: Long? = null,
    val amountPaid: Double? = null
)

enum class TicketStatus {
    PENDENTE,
    PAGO,
    CANCELADO,
    EXTRAVIADO
}
