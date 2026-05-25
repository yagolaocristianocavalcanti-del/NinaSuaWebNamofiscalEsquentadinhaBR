package com.nina.namofiscal

import com.nina.namofiscal.model.Ticket
import com.nina.namofiscal.model.TicketStatus
import com.nina.namofiscal.model.Vehicle

object BackendMock {

    private val tickets = mutableMapOf<String, Ticket>()
    private val vehicles = mutableListOf<Vehicle>()

    fun registerEntry(vehicle: Vehicle): Ticket {
        val ticketId = "TKT-${System.currentTimeMillis() % 10000}"
        val ticket = Ticket(
            id = ticketId,
            vehiclePlate = vehicle.plate,
            entryTime = vehicle.entryTime
        )
        tickets[ticketId] = ticket
        vehicles.add(vehicle)
        return ticket
    }

    fun getTicket(id: String): Ticket? {
        return tickets[id]
    }

    fun validatePayment(id: String): Boolean {
        val ticket = tickets[id] ?: return false
        tickets[id] = ticket.copy(status = TicketStatus.PAGO, paymentTime = System.currentTimeMillis())
        return true
    }
}
