package com.nina.namofiscal

import android.content.Context
import java.util.Calendar
import java.util.Random

enum class NinaOutingCompanion {
    NENHUM,
    AMIGAS,
    AMIGO
}

data class NinaOuting(
    val dayOfWeek: Int,
    val companion: NinaOutingCompanion,
    val startHour: Int,
    val endHour: Int
) {
    fun isActive(now: Calendar): Boolean {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        return companion != NinaOutingCompanion.NENHUM &&
            now.get(Calendar.DAY_OF_WEEK) == dayOfWeek &&
            hour in startHour until endHour
    }
}

data class NinaWeeklyAgenda(
    val weekKey: String,
    val randomWeekdayOff: Int,
    val outing: NinaOuting?
)

data class NinaAgendaDay(
    val dayOfWeek: Int,
    val title: String,
    val summary: String,
    val isToday: Boolean
)

object NinaSchedule {
    private const val PREFS_NAME = "NinaPrefs"
    private const val OUTING_ROLL_MAX = 10_000
    private const val CHANCE_OUTING_WITH_FRIENDS = 50
    private const val CHANCE_OUTING_WITH_MALE_FRIEND = 1
    private const val FIELD_DAY_OFF = "day_off"
    private const val FIELD_OUTING_DAY = "outing_day"
    private const val FIELD_OUTING_COMPANION = "outing_companion"
    private const val FIELD_OUTING_START = "outing_start"
    private const val FIELD_OUTING_END = "outing_end"
    private const val KEY_FALSE_ALARM_ABSENCE_DAY = "nina_false_alarm_absence_day"

    fun isDayOffToday(context: Context): Boolean {
        ensureNextWeekPlannedOnMonday(context)
        val today = NinaTime.now(context).get(Calendar.DAY_OF_WEEK)
        return today == Calendar.SUNDAY || today == getCurrentWeekAgenda(context).randomWeekdayOff
    }

    fun markFalseAlarmAbsenceToday(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FALSE_ALARM_ABSENCE_DAY, getDayKey(NinaTime.now(context)))
            .apply()
    }

    fun isFalseAlarmAbsenceToday(context: Context): Boolean {
        val todayKey = getDayKey(NinaTime.now(context))
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FALSE_ALARM_ABSENCE_DAY, "") == todayKey
    }

    fun getDayOffLabel(context: Context): String {
        return if (NinaTime.now(context).get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            "domingo"
        } else {
            dayName(getCurrentWeekAgenda(context).randomWeekdayOff)
        }
    }

    fun ensureTodayOffForOnboarding(context: Context) {
        val today = NinaTime.now(context).get(Calendar.DAY_OF_WEEK)
        if (today == Calendar.SUNDAY || today == Calendar.SATURDAY) return

        val agenda = getCurrentWeekAgenda(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putInt(keyFor(agenda.weekKey, FIELD_DAY_OFF), today)

        val outing = agenda.outing
        if (outing != null && outing.dayOfWeek != Calendar.SUNDAY && outing.dayOfWeek != today) {
            editor
                .putString(keyFor(agenda.weekKey, FIELD_OUTING_COMPANION), NinaOutingCompanion.NENHUM.name)
                .remove(keyFor(agenda.weekKey, FIELD_OUTING_DAY))
                .remove(keyFor(agenda.weekKey, FIELD_OUTING_START))
                .remove(keyFor(agenda.weekKey, FIELD_OUTING_END))
        }

        editor.apply()
    }

    fun describeRoutine(context: Context): String {
        ensureNextWeekPlannedOnMonday(context)
        val agenda = getCurrentWeekAgenda(context)
        val nextAgenda = getNextWeekAgenda(context)
        val currentOuting = describePublicOuting(agenda.outing)
        val nextWeek = if (isMonday(context)) {
            " A próxima semana já ficou decidida hoje: minha folga extra vai ser ${dayName(nextAgenda.randomWeekdayOff)}${describePublicOuting(nextAgenda.outing)}."
        } else {
            ""
        }

        return "Minha rotina é assim: eu trabalho das 9h às 15h, almoço às 12h, corro às 17h e vou dormir às 23h. Domingo eu sempre folgo, e toda semana eu tenho uma folga aleatória que eu mesma decido. Essa semana minha folga extra é ${dayName(agenda.randomWeekdayOff)}$currentOuting. ${NinaTime.describeScale()}$nextWeek"
    }

    fun getCurrentWeekAgenda(context: Context): NinaWeeklyAgenda {
        ensureNextWeekPlannedOnMonday(context)
        return getAgendaForWeek(context, 0)
    }

    fun getNextWeekAgenda(context: Context): NinaWeeklyAgenda {
        ensureNextWeekPlannedOnMonday(context)
        return getAgendaForWeek(context, 1)
    }

    fun getActiveOuting(context: Context): NinaOuting? {
        val outing = getCurrentWeekAgenda(context).outing ?: return null
        return if (outing.isActive(NinaTime.now(context))) outing else null
    }

    fun isOutWithMaleFriendNow(context: Context): Boolean {
        return getActiveOuting(context)?.companion == NinaOutingCompanion.AMIGO
    }

    fun isSleepingNow(context: Context): Boolean {
        val hour = NinaTime.now(context).get(Calendar.HOUR_OF_DAY)
        return hour >= 23 || hour < 6
    }

    fun getMessageContext(context: Context): String {
        if (isFalseAlarmAbsenceToday(context)) {
            return "Nina descobriu que uma emergência era mentira ou falso alarme. Ela está sentida, completamente apática e triste hoje, faltou ao serviço por causa do susto, mas ainda diz que está feliz por ele estar bem. Ela deve responder manhosa, baixa, insegura e sem energia, sem virar piada."
        }

        val outing = getActiveOuting(context)
        return when (outing?.companion) {
            NinaOutingCompanion.AMIGO ->
                "Nina está numa folga fora de casa com um amigo. Ela responde o tempo todo, mas manda menos mensagens, fica desconfiada, curta e defensiva, como se estivesse explicando demais sem querer."
            NinaOutingCompanion.AMIGAS ->
                "Nina está numa folga fora de casa com amigas. Ela pode responder, mas está distraída e não quer ficar presa no celular."
            else -> if (isDayOffToday(context)) {
                "Hoje é folga da Nina. Ela decide o ritmo das mensagens e pode sumir um pouco se estiver cuidando das próprias coisas."
            } else {
                ""
            }
        }
    }

    fun getPublicWeek(context: Context, weekOffset: Int = 0): List<NinaAgendaDay> {
        val agenda = if (weekOffset == 0) {
            getCurrentWeekAgenda(context)
        } else {
            getAgendaForWeek(context, weekOffset)
        }
        val today = NinaTime.now(context).get(Calendar.DAY_OF_WEEK)
        val days = listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )

        return days.map { day ->
            val summary = if (weekOffset == 0 && day == today && isFalseAlarmAbsenceToday(context)) {
                "Avisou que hoje não consegue trabalhar. Ficou recolhida depois do susto."
            } else {
                publicSummaryForDay(day, agenda)
            }

            NinaAgendaDay(
                dayOfWeek = day,
                title = dayNameShort(day),
                summary = summary,
                isToday = weekOffset == 0 && day == today
            )
        }
    }

    private fun ensureNextWeekPlannedOnMonday(context: Context) {
        if (isMonday(context)) {
            getAgendaForWeek(context, 1)
        }
    }

    private fun getAgendaForWeek(context: Context, weekOffset: Int): NinaWeeklyAgenda {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val calendar = NinaTime.now(context).apply {
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
        val weekKey = getWeekKey(calendar)
        val dayOffKey = keyFor(weekKey, FIELD_DAY_OFF)

        val savedDayOff = prefs.getInt(dayOffKey, 0)
        if (savedDayOff in Calendar.MONDAY..Calendar.FRIDAY) {
            return NinaWeeklyAgenda(
                weekKey = weekKey,
                randomWeekdayOff = savedDayOff,
                outing = readOuting(context, weekKey)
            )
        }

        val random = Random()
        val randomDay = (Calendar.MONDAY..Calendar.FRIDAY).toList()[Random().nextInt(5)]
        val outing = generateOuting(random, randomDay)
        val editor = prefs.edit()
            .putInt(dayOffKey, randomDay)

        if (outing == null) {
            editor.putString(keyFor(weekKey, FIELD_OUTING_COMPANION), NinaOutingCompanion.NENHUM.name)
        } else {
            editor
                .putInt(keyFor(weekKey, FIELD_OUTING_DAY), outing.dayOfWeek)
                .putString(keyFor(weekKey, FIELD_OUTING_COMPANION), outing.companion.name)
                .putInt(keyFor(weekKey, FIELD_OUTING_START), outing.startHour)
                .putInt(keyFor(weekKey, FIELD_OUTING_END), outing.endHour)
        }

        editor.apply()
        return NinaWeeklyAgenda(weekKey, randomDay, outing)
    }

    private fun readOuting(context: Context, weekKey: String): NinaOuting? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val companion = runCatching {
            NinaOutingCompanion.valueOf(
                prefs.getString(keyFor(weekKey, FIELD_OUTING_COMPANION), NinaOutingCompanion.NENHUM.name)
                    ?: NinaOutingCompanion.NENHUM.name
            )
        }.getOrDefault(NinaOutingCompanion.NENHUM)

        if (companion == NinaOutingCompanion.NENHUM) return null

        return NinaOuting(
            dayOfWeek = prefs.getInt(keyFor(weekKey, FIELD_OUTING_DAY), Calendar.SUNDAY),
            companion = companion,
            startHour = prefs.getInt(keyFor(weekKey, FIELD_OUTING_START), 16),
            endHour = prefs.getInt(keyFor(weekKey, FIELD_OUTING_END), 20)
        )
    }

    private fun generateOuting(random: Random, weekdayOff: Int): NinaOuting? {
        val roll = random.nextInt(OUTING_ROLL_MAX)
        val companion = when {
            roll < CHANCE_OUTING_WITH_MALE_FRIEND -> NinaOutingCompanion.AMIGO
            roll < CHANCE_OUTING_WITH_MALE_FRIEND + CHANCE_OUTING_WITH_FRIENDS -> NinaOutingCompanion.AMIGAS
            else -> return null
        }

        val day = if (random.nextBoolean()) Calendar.SUNDAY else weekdayOff
        val startHour = listOf(14, 16, 19)[random.nextInt(3)]
        return NinaOuting(
            dayOfWeek = day,
            companion = companion,
            startHour = startHour,
            endHour = (startHour + 3).coerceAtMost(22)
        )
    }

    private fun describePublicOuting(outing: NinaOuting?): String {
        if (outing == null) return ""
        val companhia = when (outing.companion) {
            NinaOutingCompanion.AMIGAS -> "com minhas amigas"
            NinaOutingCompanion.AMIGO -> return ""
            NinaOutingCompanion.NENHUM -> return ""
        }
        return ", e talvez eu saia $companhia ${dayName(outing.dayOfWeek)} das ${outing.startHour}h às ${outing.endHour}h"
    }

    private fun publicSummaryForDay(day: Int, agenda: NinaWeeklyAgenda): String {
        val publicOuting = agenda.outing?.takeIf {
            it.dayOfWeek == day && it.companion == NinaOutingCompanion.AMIGAS
        }

        val base = when {
            day == Calendar.SUNDAY -> "Folga fixa. Ritmo dela."
            day == agenda.randomWeekdayOff -> "Folga extra escolhida pela Nina."
            day in Calendar.MONDAY..Calendar.FRIDAY -> "Trabalho 9h-15h. Almoço 12h. Corrida 17h."
            else -> "Dia solto. Casa, descanso e coisas dela."
        }

        return if (publicOuting != null) {
            "$base Talvez sair com amigas das ${publicOuting.startHour}h às ${publicOuting.endHour}h."
        } else {
            base
        }
    }

    private fun getWeekKey(now: Calendar): String {
        return "${now.get(Calendar.YEAR)}-${now.get(Calendar.WEEK_OF_YEAR)}"
    }

    private fun getDayKey(now: Calendar): String {
        return "${now.get(Calendar.YEAR)}-${now.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun keyFor(weekKey: String, field: String): String {
        return "nina_agenda_${weekKey}_$field"
    }

    private fun isMonday(context: Context): Boolean {
        return NinaTime.now(context).get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
    }

    private fun dayName(day: Int): String {
        return when (day) {
            Calendar.MONDAY -> "segunda-feira"
            Calendar.TUESDAY -> "terça-feira"
            Calendar.WEDNESDAY -> "quarta-feira"
            Calendar.THURSDAY -> "quinta-feira"
            Calendar.FRIDAY -> "sexta-feira"
            Calendar.SATURDAY -> "sábado"
            Calendar.SUNDAY -> "domingo"
            else -> "um dia surpresa"
        }
    }

    private fun dayNameShort(day: Int): String {
        return when (day) {
            Calendar.MONDAY -> "SEG"
            Calendar.TUESDAY -> "TER"
            Calendar.WEDNESDAY -> "QUA"
            Calendar.THURSDAY -> "QUI"
            Calendar.FRIDAY -> "SEX"
            Calendar.SATURDAY -> "SAB"
            Calendar.SUNDAY -> "DOM"
            else -> "DIA"
        }
    }
}
