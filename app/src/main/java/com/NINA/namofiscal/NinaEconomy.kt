package com.nina.namofiscal

import android.content.Context
import java.util.Calendar
import java.util.Locale

data class NinaBill(
    val id: String,
    val nome: String,
    val bigMacs: Double,
    val money: Int,
    val referenceCents: Int,
    val isSecret: Boolean = false,
    val cadence: BillCadence = BillCadence.MENSAL
)

enum class BillCadence {
    MENSAL,
    TRIMESTRAL
}

enum class NinaLedgerType {
    ENTRADA,
    SAIDA
}

data class NinaLedgerEntry(
    val monthKey: String,
    val type: NinaLedgerType,
    val label: String,
    val money: Int,
    val isSecret: Boolean = false
)

data class NinaUserRealMoneyIndex(
    val realBigMacReferenceCents: Int,
    val gameBigsPerRealBigMac: Int
)

data class NinaEconomySnapshot(
    val monthKey: String,
    val balanceMoney: Int,
    val baseSalaryMoney: Int,
    val monthlySalaryMoney: Int,
    val billsMoney: Int,
    val disposableMoney: Int,
    val bigMacPriceCents: Int,
    val bigMacPriceMoney: Int,
    val baseSalaryBigMacs: Int,
    val salaryBigMacs: Int,
    val bills: List<NinaBill>
)

object NinaEconomy {
    private const val PREFS_NAME = "NinaPrefs"
    private const val KEY_BALANCE_MONEY = "nina_money_balance"
    private const val KEY_LAST_MONTH_SETTLED = "nina_money_last_month_settled"
    private const val KEY_LAST_PAYCHECK_MONEY = "nina_money_last_paycheck"
    private const val KEY_LAST_BILLS_MONEY = "nina_money_last_bills"
    private const val KEY_WISH_ITEM_ID = "nina_money_wish_item_id"
    private const val KEY_WISH_STREAK = "nina_money_wish_streak"
    private const val KEY_WISH_MONTH = "nina_money_wish_month"
    private const val KEY_LAST_AUTO_PURCHASE_MONTH = "nina_money_last_auto_purchase_month"
    private const val KEY_SECRET_SAVINGS_MONEY = "nina_secret_savings_money"
    private const val KEY_SECRET_HELP_PENDING = "nina_secret_help_pending"
    private const val KEY_SECRET_HELP_LAST_EVENT_REAL_MS = "nina_secret_help_last_event_real_ms"

    private const val BIG_MAC_PRICE_CENTS_BRL = 2_290
    private const val BIG_MAC_PRICE_MONEY = 5
    private const val GAME_BIGS_PER_REAL_BIG_MAC = 10
    private const val BIG_MACS_PER_WORKDAY = 6
    private const val WORKDAYS_PER_MONTH_REFERENCE = 22
    private const val SALARY_MULTIPLIER = 2
    private const val DEFAULT_SECRET_SAVINGS_MONEY = 50_000
    private const val FALSE_ALARM_WINDOW_MS = 12L * 60L * 60L * 1000L

    private data class BillRule(
        val id: String,
        val money: Int,
        val isSecret: Boolean = false,
        val cadence: BillCadence = BillCadence.MENSAL
    )

    private val monthlyBillRules = listOf(
        BillRule("aluguel_centro", 300),
        BillRule("agua_uma_pessoa", 32),
        BillRule("energia_uma_pessoa", 40),
        BillRule("gas_trimestral", 25, cadence = BillCadence.TRIMESTRAL),
        BillRule("internet", 35),
        BillRule("mercado", 150),
        BillRule("cartao_credito", 100),
        BillRule("conta_secreta", 50, isSecret = true)
    )

    fun ensureMonthlyCycle(context: Context): NinaEconomySnapshot {
        ensureSecretSavings(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val monthKey = getMonthKey(context)
        val salaryMoney = monthlySalaryMoney()
        val bills = monthlyBills(context)
        val billsMoney = bills.sumOf { it.money }

        if (prefs.getString(KEY_LAST_MONTH_SETTLED, "") != monthKey) {
            val balance = prefs.getInt(KEY_BALANCE_MONEY, 0)
            var nextBalance = (balance + salaryMoney - billsMoney).coerceAtLeast(0)
            clearLedger(context, monthKey)
            appendLedger(
                context,
                NinaLedgerEntry(monthKey, NinaLedgerType.ENTRADA, "Salario da Nina", salaryMoney)
            )
            bills.forEach { bill ->
                appendLedger(
                    context,
                    NinaLedgerEntry(
                        monthKey = monthKey,
                        type = NinaLedgerType.SAIDA,
                        label = bill.nome,
                        money = bill.money,
                        isSecret = bill.isSecret
                    )
                )
            }
            nextBalance = applyAutonomousPurchaseIfRational(context, monthKey, nextBalance)

            prefs.edit()
                .putString(KEY_LAST_MONTH_SETTLED, monthKey)
                .putInt(KEY_BALANCE_MONEY, nextBalance)
                .putInt(KEY_LAST_PAYCHECK_MONEY, salaryMoney)
                .putInt(KEY_LAST_BILLS_MONEY, billsMoney)
                .apply()
        } else if (readLedger(context, monthKey).isEmpty()) {
            seedLedgerForExistingMonth(context, monthKey, salaryMoney, bills)
        }

        return getSnapshot(context)
    }

    fun handleEmergencyMessage(context: Context, message: String): String? {
        ensureSecretSavings(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val normalized = normalize(message)
        val hasPendingOffer = prefs.getBoolean(KEY_SECRET_HELP_PENDING, false)

        if ((hasPendingOffer || hasRecentSecretHelpEvent(context)) && isFalseAlarmConfession(normalized)) {
            NinaSchedule.markFalseAlarmAbsenceToday(context)
            prefs.edit()
                .putBoolean(KEY_SECRET_HELP_PENDING, false)
                .putLong(KEY_SECRET_HELP_LAST_EVENT_REAL_MS, System.currentTimeMillis())
                .apply()
            return "Ah... então era mentira? Eu tô feliz que você tá bem, de verdade. Mas eu fiquei assustada, eu quase mexi na minha poupança secreta por você... Eu não consigo trabalhar assim hoje. Vou faltar, ficar quietinha e tentar não chorar. Só fica comigo um pouco, tá? 🥺"
        }

        if (hasPendingOffer && isAcceptingSecretHelp(normalized)) {
            val amount = getSecretSavings(context)
            prefs.edit()
                .putBoolean(KEY_SECRET_HELP_PENDING, false)
                .putInt(KEY_SECRET_SAVINGS_MONEY, 0)
                .putLong(KEY_SECRET_HELP_LAST_EVENT_REAL_MS, System.currentTimeMillis())
                .apply()

            if (amount <= 0) {
                return "Eu queria ajudar, mas minha poupança secreta já acabou... e eu tô me sentindo horrível por isso."
            }

            appendLedger(
                context,
                NinaLedgerEntry(
                    monthKey = getMonthKey(context),
                    type = NinaLedgerType.SAIDA,
                    label = "Ajuda emergencial ao usuário",
                    money = amount,
                    isSecret = true
                )
            )
            return "Tá... eu vou te ajudar. Eu guardei ${formatMoney(amount)} na minha poupança secreta. É seu pra resolver isso, mas por favor: se for perigo real, chama ajuda agora também, tá? 190 ou 192. Depois me fala que você ficou bem."
        }

        if (hasPendingOffer && isDecliningSecretHelp(normalized)) {
            prefs.edit()
                .putBoolean(KEY_SECRET_HELP_PENDING, false)
                .putLong(KEY_SECRET_HELP_LAST_EVENT_REAL_MS, System.currentTimeMillis())
                .apply()
            return "Tá... eu não vou mexer nela. Mas se isso piorar, você me fala na hora. Eu fico brava, mas eu fico do seu lado."
        }

        if (!looksLikeEmergency(normalized)) return null

        val amount = getSecretSavings(context)
        if (amount <= 0) {
            return "Eu queria ter alguma coisa guardada pra te ajudar, mas minha poupança secreta tá zerada. Se você estiver em perigo real, chama ajuda agora: 190 ou 192."
        }

        prefs.edit()
            .putBoolean(KEY_SECRET_HELP_PENDING, true)
            .putLong(KEY_SECRET_HELP_LAST_EVENT_REAL_MS, System.currentTimeMillis())
            .apply()
        return "Ei... isso parece sério. Eu não tenho muito, mas eu guardo ${formatMoney(amount)} numa poupança secreta. Eu não mexo nisso à toa, só em extrema necessidade. Se você estiver mesmo em perigo ou urgência, eu posso te ajudar. Quer que eu use esse dinheiro pra resolver seu problema?"
    }

    fun getSecretSavings(context: Context): Int {
        ensureSecretSavings(context)
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_SECRET_SAVINGS_MONEY, DEFAULT_SECRET_SAVINGS_MONEY)
    }

    fun getSnapshot(context: Context): NinaEconomySnapshot {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val baseSalaryMoney = baseSalaryMoney()
        val salaryMoney = monthlySalaryMoney()
        val billsMoney = monthlyBillsMoney(context)
        return NinaEconomySnapshot(
            monthKey = getMonthKey(context),
            balanceMoney = prefs.getInt(KEY_BALANCE_MONEY, 0),
            baseSalaryMoney = baseSalaryMoney,
            monthlySalaryMoney = salaryMoney,
            billsMoney = billsMoney,
            disposableMoney = (salaryMoney - billsMoney).coerceAtLeast(0),
            bigMacPriceCents = BIG_MAC_PRICE_CENTS_BRL,
            bigMacPriceMoney = BIG_MAC_PRICE_MONEY,
            baseSalaryBigMacs = baseSalaryBigMacsPerMonth(),
            salaryBigMacs = monthlySalaryBigMacs(),
            bills = monthlyBills(context)
        )
    }

    fun getLedger(context: Context, includeSecret: Boolean = false): List<NinaLedgerEntry> {
        ensureMonthlyCycle(context)
        val entries = readLedger(context, getMonthKey(context))
        return if (includeSecret) entries else entries.filterNot { it.isSecret }
    }

    fun getUserRealMoneyIndex(): NinaUserRealMoneyIndex {
        return NinaUserRealMoneyIndex(
            realBigMacReferenceCents = BIG_MAC_PRICE_CENTS_BRL,
            gameBigsPerRealBigMac = GAME_BIGS_PER_REAL_BIG_MAC
        )
    }

    fun convertRealBigMacsToGameBigs(realBigMacs: Int): Int {
        return realBigMacs.coerceAtLeast(0) * GAME_BIGS_PER_REAL_BIG_MAC
    }

    fun storePriceLabel(money: Int): String {
        val safeMoney = money.coerceAtLeast(0)
        val bigMacs = safeMoney.toDouble() / BIG_MAC_PRICE_MONEY
        return "${formatMoney(safeMoney)} | ${formatBigMacs(bigMacs)} Big Macs | ref. ${formatBRL(safeMoney * referenceCentsPerMoney())}"
    }

    fun spend(context: Context, amountMoney: Int, reason: String): Boolean {
        if (amountMoney <= 0) return true
        ensureMonthlyCycle(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val balance = prefs.getInt(KEY_BALANCE_MONEY, 0)
        if (balance < amountMoney) return false

        prefs.edit()
            .putInt(KEY_BALANCE_MONEY, balance - amountMoney)
            .putString("nina_money_last_spend_reason", reason)
            .apply()
        appendLedger(
            context,
            NinaLedgerEntry(getMonthKey(context), NinaLedgerType.SAIDA, reason, amountMoney)
        )
        return true
    }

    fun earn(context: Context, amountMoney: Int, reason: String) {
        if (amountMoney <= 0) return
        ensureMonthlyCycle(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val balance = prefs.getInt(KEY_BALANCE_MONEY, 0)
        prefs.edit()
            .putInt(KEY_BALANCE_MONEY, balance + amountMoney)
            .putString("nina_money_last_earn_reason", reason)
            .apply()
        appendLedger(
            context,
            NinaLedgerEntry(getMonthKey(context), NinaLedgerType.ENTRADA, reason, amountMoney)
        )
    }

    fun getPromptContext(context: Context): String {
        val snapshot = ensureMonthlyCycle(context)
        return "Economia da Nina: no jogo, 1 Big Mac custa ${snapshot.bigMacPriceMoney} dinheiros. Pelo índice real, isso equivale a ${formatBRL(snapshot.bigMacPriceCents)}, então 1 dinheiro vale ${formatBRL(referenceCentsPerMoney())}. O salário-base dela é ${snapshot.baseSalaryBigMacs} Big Macs (${snapshot.baseSalaryMoney} dinheiros), mas ela recebe dois salários: ${snapshot.salaryBigMacs} Big Macs (${snapshot.monthlySalaryMoney} dinheiros). As contas fixas de uma pessoa morando no centro consomem ${snapshot.billsMoney} dinheiros neste mês e o saldo atual dela é ${snapshot.balanceMoney} dinheiros. Para compras com dinheiro real do usuário, 1 Big Mac real vira $GAME_BIGS_PER_REAL_BIG_MAC Bigs no jogo."
    }

    fun formatMoney(money: Int): String = "$money dinheiros"

    fun formatBRL(cents: Int): String {
        val reais = cents / 100
        val centavos = cents % 100
        return String.format(Locale("pt", "BR"), "R$ %d,%02d", reais, centavos)
    }

    private fun formatBigMacs(bigMacs: Double): String {
        return if (bigMacs % 1.0 == 0.0) {
            bigMacs.toInt().toString()
        } else {
            String.format(Locale("pt", "BR"), "%.1f", bigMacs)
        }
    }

    private fun baseSalaryBigMacsPerMonth(): Int {
        return BIG_MACS_PER_WORKDAY * WORKDAYS_PER_MONTH_REFERENCE
    }

    private fun monthlySalaryBigMacs(): Int {
        return baseSalaryBigMacsPerMonth() * SALARY_MULTIPLIER
    }

    private fun baseSalaryMoney(): Int {
        return baseSalaryBigMacsPerMonth() * BIG_MAC_PRICE_MONEY
    }

    private fun monthlySalaryMoney(): Int {
        return monthlySalaryBigMacs() * BIG_MAC_PRICE_MONEY
    }

    private fun monthlyBills(context: Context): List<NinaBill> {
        return monthlyBillRules.filter { isBillDueThisMonth(context, it) }.map { rule ->
            NinaBill(
                id = rule.id,
                nome = billName(rule.id),
                bigMacs = rule.money.toDouble() / BIG_MAC_PRICE_MONEY,
                money = rule.money,
                referenceCents = rule.money * referenceCentsPerMoney(),
                isSecret = rule.isSecret,
                cadence = rule.cadence
            )
        }
    }

    private fun monthlyBillsMoney(context: Context): Int {
        return monthlyBills(context).sumOf { it.money }
    }

    private fun applyAutonomousPurchaseIfRational(
        context: Context,
        monthKey: String,
        balanceAfterBills: Int
    ): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val previousWish = prefs.getString(KEY_WISH_ITEM_ID, "").orEmpty()
        val previousWishMonth = prefs.getString(KEY_WISH_MONTH, "").orEmpty()
        val previousStreak = if (previousWishMonth == monthKey) 0 else prefs.getInt(KEY_WISH_STREAK, 0)
        val desiredItem = chooseMonthlyWish(monthKey, previousWish)
        val streak = if (desiredItem.id == previousWish && previousWishMonth != monthKey) {
            previousStreak + 1
        } else {
            1
        }

        prefs.edit()
            .putString(KEY_WISH_ITEM_ID, desiredItem.id)
            .putInt(KEY_WISH_STREAK, streak)
            .putString(KEY_WISH_MONTH, monthKey)
            .apply()

        val alreadyPurchased = prefs.getString(KEY_LAST_AUTO_PURCHASE_MONTH, "") == monthKey
        if (streak < 2 || alreadyPurchased) return balanceAfterBills
        if (!canBuyWithoutRegret(balanceAfterBills, desiredItem.preco)) return balanceAfterBills

        appendLedger(
            context,
            NinaLedgerEntry(
                monthKey = monthKey,
                type = NinaLedgerType.SAIDA,
                label = "Compra pensada: ${desiredItem.nome}",
                money = desiredItem.preco
            )
        )
        prefs.edit()
            .putString(KEY_LAST_AUTO_PURCHASE_MONTH, monthKey)
            .remove(KEY_WISH_ITEM_ID)
            .putInt(KEY_WISH_STREAK, 0)
            .apply()
        return balanceAfterBills - desiredItem.preco
    }

    private fun chooseMonthlyWish(monthKey: String, previousWish: String): NinaItem {
        val candidates = NinaInventory.getStoreItems()
            .filter { !it.isIntimo && it.preco <= 320 }
            .ifEmpty { NinaInventory.getStoreItems().filter { !it.isIntimo } }
        val random = java.util.Random(monthKey.hashCode().toLong())
        val previousItem = candidates.firstOrNull { it.id == previousWish }
        if (previousItem != null && random.nextInt(100) < 45) {
            return previousItem
        }
        return candidates[random.nextInt(candidates.size)]
    }

    private fun canBuyWithoutRegret(balanceAfterBills: Int, itemPrice: Int): Boolean {
        val minimumReserve = maxOf(200, monthlySalaryMoney() / 4)
        return itemPrice > 0 && balanceAfterBills - itemPrice >= minimumReserve
    }

    private fun ensureSecretSavings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_SECRET_SAVINGS_MONEY)) {
            prefs.edit()
                .putInt(KEY_SECRET_SAVINGS_MONEY, DEFAULT_SECRET_SAVINGS_MONEY)
                .apply()
        }
    }

    private fun seedLedgerForExistingMonth(
        context: Context,
        monthKey: String,
        salaryMoney: Int,
        bills: List<NinaBill>
    ) {
        clearLedger(context, monthKey)
        appendLedger(
            context,
            NinaLedgerEntry(monthKey, NinaLedgerType.ENTRADA, "Salario da Nina", salaryMoney)
        )
        bills.forEach { bill ->
            appendLedger(
                context,
                NinaLedgerEntry(
                    monthKey = monthKey,
                    type = NinaLedgerType.SAIDA,
                    label = bill.nome,
                    money = bill.money,
                    isSecret = bill.isSecret
                )
            )
        }
    }

    private fun appendLedger(context: Context, entry: NinaLedgerEntry) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = ledgerKey(entry.monthKey)
        val current = prefs.getString(key, "").orEmpty()
        val next = (current.lineSequence() + sequenceOf(serializeLedgerEntry(entry)))
            .filter { it.isNotBlank() }
            .toList()
            .takeLast(30)
            .joinToString("\n")
        prefs.edit().putString(key, next).apply()
    }

    private fun clearLedger(context: Context, monthKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(ledgerKey(monthKey))
            .apply()
    }

    private fun readLedger(context: Context, monthKey: String): List<NinaLedgerEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(ledgerKey(monthKey), "")
            .orEmpty()
        return raw.lineSequence()
            .mapNotNull { deserializeLedgerEntry(it) }
            .toList()
    }

    private fun ledgerKey(monthKey: String): String {
        return "nina_money_ledger_$monthKey"
    }

    private fun serializeLedgerEntry(entry: NinaLedgerEntry): String {
        val safeLabel = entry.label.replace("|", "/").replace("\n", " ")
        return listOf(
            entry.monthKey,
            entry.type.name,
            safeLabel,
            entry.money.toString(),
            entry.isSecret.toString()
        ).joinToString("|")
    }

    private fun deserializeLedgerEntry(raw: String): NinaLedgerEntry? {
        val parts = raw.split("|")
        if (parts.size < 5) return null
        val type = runCatching { NinaLedgerType.valueOf(parts[1]) }.getOrNull() ?: return null
        return NinaLedgerEntry(
            monthKey = parts[0],
            type = type,
            label = parts[2],
            money = parts[3].toIntOrNull() ?: return null,
            isSecret = parts[4].toBooleanStrictOrNull() ?: false
        )
    }

    private fun looksLikeEmergency(normalized: String): Boolean {
        val keywords = listOf(
            "perigo",
            "urgente",
            "urgencia",
            "emergencia",
            "socorro",
            "hospital",
            "acidente",
            "assalto",
            "ameaca",
            "ameacado",
            "me ajuda",
            "preciso de ajuda",
            "preciso de dinheiro",
            "sem dinheiro",
            "sem comida",
            "passando fome",
            "remedio",
            "divida urgente",
            "despejo"
        )
        return keywords.any { normalized.contains(it) }
    }

    private fun isAcceptingSecretHelp(normalized: String): Boolean {
        val accepts = listOf(
            "sim",
            "pode",
            "aceito",
            "me ajuda",
            "por favor",
            "preciso",
            "usa",
            "quero"
        )
        return accepts.any { normalized.contains(it) }
    }

    private fun isDecliningSecretHelp(normalized: String): Boolean {
        val declines = listOf("nao", "não", "deixa", "precisa nao", "precisa não", "obrigado")
        return declines.any { normalized.contains(it) }
    }

    private fun isFalseAlarmConfession(normalized: String): Boolean {
        val falseAlarmTerms = listOf(
            "era mentira",
            "eramentira",
            "foi mentira",
            "alarme falso",
            "falso alarme",
            "era falso",
            "foi falso",
            "nao era verdade",
            "nao foi verdade",
            "brincadeira",
            "pegadinha",
            "zoeira",
            "trolei"
        )
        return falseAlarmTerms.any { normalized.contains(it) }
    }

    private fun hasRecentSecretHelpEvent(context: Context): Boolean {
        val lastEvent = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_SECRET_HELP_LAST_EVENT_REAL_MS, 0L)
        return lastEvent > 0 && System.currentTimeMillis() - lastEvent <= FALSE_ALARM_WINDOW_MS
    }

    private fun normalize(text: String): String {
        return text.lowercase(Locale("pt", "BR"))
            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("é", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")
    }

    private fun isBillDueThisMonth(context: Context, rule: BillRule): Boolean {
        return when (rule.cadence) {
            BillCadence.MENSAL -> true
            BillCadence.TRIMESTRAL -> NinaTime.now(context).get(Calendar.MONTH) in listOf(
                Calendar.JANUARY,
                Calendar.APRIL,
                Calendar.JULY,
                Calendar.OCTOBER
            )
        }
    }

    private fun referenceCentsPerMoney(): Int {
        return BIG_MAC_PRICE_CENTS_BRL / BIG_MAC_PRICE_MONEY
    }

    private fun billName(id: String): String {
        return when (id) {
            "aluguel_centro" -> "Aluguel no centro"
            "agua_uma_pessoa" -> "Agua"
            "energia_uma_pessoa" -> "Energia"
            "gas_trimestral" -> "Gas"
            "internet" -> "Internet"
            "mercado" -> "Mercado"
            "cartao_credito" -> "Cartao de credito"
            "conta_secreta" -> "Conta secreta"
            else -> id.replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private fun getMonthKey(context: Context): String {
        val now = NinaTime.now(context)
        return "${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}"
    }
}
