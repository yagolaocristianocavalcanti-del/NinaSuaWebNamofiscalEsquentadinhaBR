package com.nina.namofiscal

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class GiftDecision {
    ACEITO,
    REJEITADO
}

data class NinaGiftHistoryEntry(
    val itemId: String,
    val itemName: String,
    val appName: String,
    val price: Int,
    val decision: GiftDecision,
    val reason: String,
    val affectionAtMoment: Int,
    val timestamp: Long
)

object NinaGiftHistory {
    private const val PREFS_NAME = "NinaPrefs"
    private const val KEY_GIFT_HISTORY = "nina_gift_history"
    private const val MAX_HISTORY_ITEMS = 50

    fun registrar(
        context: Context,
        item: NinaItem,
        decision: GiftDecision,
        reason: String,
        affectionAtMoment: Int
    ) {
        val atual = getHistory(context).toMutableList()
        atual.add(
            NinaGiftHistoryEntry(
                itemId = item.id,
                itemName = item.nome,
                appName = item.app.titulo,
                price = item.preco,
                decision = decision,
                reason = reason,
                affectionAtMoment = affectionAtMoment,
                timestamp = System.currentTimeMillis()
            )
        )

        val finalList = atual.takeLast(MAX_HISTORY_ITEMS)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GIFT_HISTORY, encode(finalList))
            .apply()
    }

    fun getHistory(context: Context): List<NinaGiftHistoryEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GIFT_HISTORY, null)
            ?: return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val obj = array.getJSONObject(index)
                NinaGiftHistoryEntry(
                    itemId = obj.getString("itemId"),
                    itemName = obj.getString("itemName"),
                    appName = obj.getString("appName"),
                    price = obj.getInt("price"),
                    decision = GiftDecision.valueOf(obj.getString("decision")),
                    reason = obj.getString("reason"),
                    affectionAtMoment = obj.getInt("affectionAtMoment"),
                    timestamp = obj.getLong("timestamp")
                )
            }
        }.getOrElse { emptyList() }
    }

    fun getAccepted(context: Context): List<NinaGiftHistoryEntry> {
        return getHistory(context).filter { it.decision == GiftDecision.ACEITO }
    }

    fun getRejected(context: Context): List<NinaGiftHistoryEntry> {
        return getHistory(context).filter { it.decision == GiftDecision.REJEITADO }
    }

    fun getLastSummary(context: Context): String {
        val last = getHistory(context).lastOrNull()
            ?: return "A Nina ainda não recebeu presentes."

        return when (last.decision) {
            GiftDecision.ACEITO -> "Último presente aceito: ${last.itemName} (${last.appName})."
            GiftDecision.REJEITADO -> "Último presente rejeitado: ${last.itemName}. Motivo: ${last.reason}"
        }
    }

    private fun encode(entries: List<NinaGiftHistoryEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("itemId", entry.itemId)
                    .put("itemName", entry.itemName)
                    .put("appName", entry.appName)
                    .put("price", entry.price)
                    .put("decision", entry.decision.name)
                    .put("reason", entry.reason)
                    .put("affectionAtMoment", entry.affectionAtMoment)
                    .put("timestamp", entry.timestamp)
            )
        }
        return array.toString()
    }
}
