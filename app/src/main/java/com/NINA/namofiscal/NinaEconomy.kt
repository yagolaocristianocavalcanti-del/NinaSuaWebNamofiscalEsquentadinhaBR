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

    private const val BIG_MAC_PRICE_CENTS_BRL = 2_290
    private const val BIG_MAC_PRICE_MONEY = 5
    private const val GAME_BIGS_PER_REAL_BIG_MAC = 10
    private const val BIG_MACS_PER_WORKDAY = 6
    private const val WORKDAYS_PER_MONTH_REFERENCE = 22
    private const val SALARY_MULTIPLIER = 2

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
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val monthKey = getMonthKey(context)
        val salaryMoney = monthlySalaryMoney()
        val billsMoney = monthlyBillsMoney(context)

        if (prefs.getString(KEY_LAST_MONTH_SETTLED, "") != monthKey) {
            val balance = prefs.getInt(KEY_BALANCE_MONEY, 0)
            prefs.edit()
                .putString(KEY_LAST_MONTH_SETTLED, monthKey)
                .putInt(KEY_BALANCE_MONEY, (balance + salaryMoney - billsMoney).coerceAtLeast(0))
                .putInt(KEY_LAST_PAYCHECK_MONEY, salaryMoney)
                .putInt(KEY_LAST_BILLS_MONEY, billsMoney)
                .apply()
        }

        return getSnapshot(context)
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

    fun getUserRealMoneyIndex(): NinaUserRealMoneyIndex {
        return NinaUserRealMoneyIndex(
            realBigMacReferenceCents = BIG_MAC_PRICE_CENTS_BRL,
            gameBigsPerRealBigMac = GAME_BIGS_PER_REAL_BIG_MAC
        )
    }

    fun convertRealBigMacsToGameBigs(realBigMacs: Int): Int {
        return realBigMacs.coerceAtLeast(0) * GAME_BIGS_PER_REAL_BIG_MAC
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
