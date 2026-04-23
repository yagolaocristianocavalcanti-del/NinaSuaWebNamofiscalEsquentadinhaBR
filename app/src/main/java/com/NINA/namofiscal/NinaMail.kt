package com.nina.namofiscal

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class NinaMailDelivery(
    val item: NinaItem,
    val accepted: Boolean,
    val message: String,
    val humor: String,
    val carinhoDelta: Int = 0,
    val ciumeDelta: Int = 0,
    val vacilo: String? = null
)

object NinaMail {
    private const val PREFS_NAME = "NinaPrefs"
    private const val KEY_PENDING_PACKAGES = "nina_mail_pending_packages"
    private const val DELIVERY_HOUR = 15

    fun enqueuePurchase(context: Context, item: NinaItem): String {
        val packages = readPending(context).toMutableList()
        val deliveryDay = nextDeliveryDay(context)
        packages.add(
            JSONObject()
                .put("itemId", item.id)
                .put("deliveryDay", deliveryDay)
                .put("createdAt", System.currentTimeMillis())
        )
        savePending(context, packages)
        return "Correio da Nina: chega ${deliveryLabel(context, deliveryDay)} depois do trabalho."
    }

    fun deliverReady(context: Context, affection: Int, nomeUsuario: String): List<NinaMailDelivery> {
        val now = NinaTime.now(context)
        if (now.get(Calendar.HOUR_OF_DAY) < DELIVERY_HOUR) return emptyList()

        val today = dayKey(now)
        val pending = readPending(context)
        val ready = pending.filter { it.optString("deliveryDay") <= today }
        if (ready.isEmpty()) return emptyList()

        val readyKeys = ready.map { packageKey(it) }.toSet()
        savePending(context, pending.filterNot { packageKey(it) in readyKeys })

        return ready.mapNotNull { pack ->
            val item = NinaInventory.getStoreItems().firstOrNull { it.id == pack.optString("itemId") }
                ?: return@mapNotNull null
            deliveryForItem(context, item, affection, nomeUsuario)
        }
    }

    fun pendingSummary(context: Context): String {
        val pending = readPending(context)
        if (pending.isEmpty()) return "Correio vazio."
        return pending
            .takeLast(4)
            .joinToString("\n") { pack ->
                val item = NinaInventory.getStoreItems().firstOrNull { it.id == pack.optString("itemId") }
                val nome = item?.nome ?: "Pacote misterioso"
                "${item?.let { NinaInventory.emojiFor(it) } ?: "📦"} $nome | ${deliveryLabel(context, pack.optString("deliveryDay"))}"
            }
    }

    private fun deliveryForItem(
        context: Context,
        item: NinaItem,
        affection: Int,
        nomeUsuario: String
    ): NinaMailDelivery {
        val intimateWithoutAffection = item.isIntimo && affection < 85
        val lacksAffection = affection < item.intimidadeMinima
        return if (intimateWithoutAffection || lacksAffection) {
            val reason = if (intimateWithoutAffection) {
                "Tentou enviar item íntimo sem intimidade muito alta (${affection}/85)."
            } else {
                "Intimidade insuficiente (${affection}/${item.intimidadeMinima})."
            }
            NinaGiftHistory.registrar(
                context = context,
                item = item,
                decision = GiftDecision.REJEITADO,
                reason = reason,
                affectionAtMoment = affection
            )
            NinaMailDelivery(
                item = item,
                accepted = false,
                message = "Cheguei do trabalho e vi o pacote... ${NinaInventory.emojiFor(item)} ${item.nome}? Sério, $nomeUsuario? Ainda não temos intimidade pra isso. Eu fiquei desconfortável. 😳😤",
                humor = NinaInventory.EMO_BRAVA,
                carinhoDelta = if (intimateWithoutAffection) -15 else 0,
                ciumeDelta = if (intimateWithoutAffection) 25 else 10,
                vacilo = "Enviou ${item.nome} sem intimidade suficiente."
            )
        } else {
            NinaGiftHistory.registrar(
                context = context,
                item = item,
                decision = GiftDecision.ACEITO,
                reason = "A Nina abriu o pacote no correio depois do trabalho.",
                affectionAtMoment = affection
            )
            NinaMailDelivery(
                item = item,
                accepted = true,
                message = "Cheguei do trabalho e tinha pacote no meu correio... ${NinaInventory.emojiFor(item)} ${item.nome}. Tá, isso foi fofo. Obrigada, $nomeUsuario. 💕",
                humor = item.lookLiberado ?: NinaInventory.EMO_CARINHOSA,
                carinhoDelta = item.carinhoBonus
            )
        }
    }

    private fun nextDeliveryDay(context: Context): String {
        val now = NinaTime.now(context)
        val delivery = now.clone() as Calendar
        if (now.get(Calendar.HOUR_OF_DAY) >= DELIVERY_HOUR || NinaSchedule.isDayOffToday(context)) {
            do {
                delivery.add(Calendar.DAY_OF_YEAR, 1)
            } while (isNonWorkDay(context, delivery))
        }
        return dayKey(delivery)
    }

    private fun isNonWorkDay(context: Context, calendar: Calendar): Boolean {
        val current = NinaTime.now(context)
        val sameDay = calendar.get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)
        if (sameDay) return NinaSchedule.isDayOffToday(context)
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
    }

    private fun deliveryLabel(context: Context, deliveryDay: String): String {
        val today = dayKey(NinaTime.now(context))
        return if (deliveryDay == today) {
            "hoje às 15h"
        } else {
            "no próximo dia de trabalho às 15h"
        }
    }

    private fun dayKey(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_YEAR).toString().padStart(3, '0')
        return "${calendar.get(Calendar.YEAR)}-$day"
    }

    private fun packageKey(pack: JSONObject): String {
        return "${pack.optString("itemId")}|${pack.optLong("createdAt")}|${pack.optString("deliveryDay")}"
    }

    private fun readPending(context: Context): List<JSONObject> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_PACKAGES, null)
            ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.getJSONObject(index) }
        }.getOrElse { emptyList() }
    }

    private fun savePending(context: Context, packages: List<JSONObject>) {
        val array = JSONArray()
        packages.forEach { array.put(it) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_PACKAGES, array.toString())
            .apply()
    }
}
