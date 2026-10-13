package com.kineticcare.core.localization

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

/**
 * موتور محاسباتی گاه‌شماری هجری شمسی (جلالی) با دقت بالا
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
object JalaliCalendarEngine {

    val TEHRAN_ZONE: ZoneId = ZoneId.of("Asia/Tehran")

    /**
     * تعیین کبیسه بودن سال هجری شمسی بر اساس چرخه ۲۸۲۰ ساله و ۳۳ ساله
     */
    fun isLeapYear(year: Int): Boolean {
        val y = if (year > 0) year else year + 1
        val b = 2820
        var r = (y - 474) % b
        if (r < 0) r += b
        return (((r + 38) * 682) % 2816) < 682
    }

    /**
     * تعداد روزهای هر ماه در سال مورد نظر
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> if (isLeapYear(year)) 30 else 29
            else -> throw IllegalArgumentException("ماه نامعتبر: $month")
        }
    }

    /**
     * تاریخ و زمان فعلی بر حسب گاه‌شماری جلالی (منطقه زمانی تهران)
     */
    fun now(): JalaliDate {
        val zonedDateTime = ZonedDateTime.now(TEHRAN_ZONE)
        return fromZonedDateTime(zonedDateTime)
    }

    /**
     * تبدیل از ZonedDateTime به تاریخ جلالی
     */
    fun fromZonedDateTime(zdt: ZonedDateTime): JalaliDate {
        val (jy, jm, jd) = gregorianToJalali(zdt.year, zdt.monthValue, zdt.dayOfMonth)
        return JalaliDate(
            year = jy,
            month = jm,
            day = jd,
            hour = zdt.hour,
            minute = zdt.minute,
            second = zdt.second
        )
    }

    /**
     * تبدیل از میلی‌ثانیه یونیکس (Epoch Millis) به تاریخ جلالی
     */
    fun fromEpochMillis(epochMillis: Long): JalaliDate {
        val instant = Instant.ofEpochMilli(epochMillis)
        val zdt = instant.atZone(TEHRAN_ZONE)
        return fromZonedDateTime(zdt)
    }

    /**
     * تبدیل رشته زمان استاندارد ISO 8601 UTC به تاریخ جلالی
     * مثال: "2026-10-13T18:05:00Z"
     */
    fun fromIsoUtc(isoString: String): JalaliDate {
        val instant = Instant.parse(isoString)
        val zdt = instant.atZone(TEHRAN_ZONE)
        return fromZonedDateTime(zdt)
    }

    /**
     * تبدیل تاریخ جلالی به میلی‌ثانیه یونیکس (Epoch Millis)
     */
    fun toEpochMillis(jalaliDate: JalaliDate): Long {
        val (gy, gm, gd) = jalaliToGregorian(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        val zdt = ZonedDateTime.of(
            gy, gm, gd,
            jalaliDate.hour, jalaliDate.minute, jalaliDate.second, 0,
            TEHRAN_ZONE
        )
        return zdt.toInstant().toEpochMilli()
    }

    /**
     * تبدیل تاریخ جلالی به فرمت استاندارد ISO 8601 UTC جهت ذخیره در PostgreSQL
     */
    fun toIsoUtc(jalaliDate: JalaliDate): String {
        val (gy, gm, gd) = jalaliToGregorian(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        val zdt = ZonedDateTime.of(
            gy, gm, gd,
            jalaliDate.hour, jalaliDate.minute, jalaliDate.second, 0,
            TEHRAN_ZONE
        )
        return DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant())
    }

    /**
     * محاسبه نام روز هفته در تقویم جلالی (شنبه = 0 تا جمعه = 6)
     */
    fun getDayOfWeekIndex(date: JalaliDate): Int {
        val (gy, gm, gd) = jalaliToGregorian(date.year, date.month, date.day)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(gy, gm - 1, gd)
        }
        // در تقویم میلادی: Sunday = 1, Saturday = 7
        val gDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return when (gDayOfWeek) {
            Calendar.SATURDAY -> 0 // شنبه
            Calendar.SUNDAY -> 1   // یکشنبه
            Calendar.MONDAY -> 2   // دوشنبه
            Calendar.TUESDAY -> 3  // سه‌شنبه
            Calendar.WEDNESDAY -> 4// چهارشنبه
            Calendar.THURSDAY -> 5 // پنج‌شنبه
            Calendar.FRIDAY -> 6   // جمعه
            else -> 0
        }
    }

    fun getDayOfWeekName(date: JalaliDate): String {
        return JalaliDate.WEEKDAY_NAMES[getDayOfWeekIndex(date)]
    }

    /**
     * الگوریتم تبدیل میلادی به شمسی
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonths = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) (gy + 1) else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonths[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }

    /**
     * الگوریتم تبدیل شمسی به میلادی
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd + (if (jm < 7) ((jm - 1) * 31) else (((jm - 7) * 30) + 186))
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        var gd = days + 1
        val salA = intArrayOf(
            0, 31,
            if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28,
            31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )
        var gm = 0
        while (gm < 13 && gd > salA[gm]) {
            gd -= salA[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }
}
