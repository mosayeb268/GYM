package com.kineticcare.core.localization

/**
 * مدل داده‌ای تاریخ و زمان در گاه‌شماری هجری شمسی (جلالی)
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
) : Comparable<JalaliDate> {

    init {
        require(month in 1..12) { "ماه باید بین ۱ تا ۱۲ باشد (دریافت شده: $month)" }
        val maxDays = JalaliCalendarEngine.getDaysInMonth(year, month)
        require(day in 1..maxDays) { "روز نامعتبر است برای ماه $month سال $year (حداکثر روز: $maxDays، دریافت شده: $day)" }
        require(hour in 0..23) { "ساعت باید بین ۰ تا ۲۳ باشد" }
        require(minute in 0..59) { "دقیقه باید بین ۰ تا ۵۹ باشد" }
        require(second in 0..59) { "ثانیه باید بین ۰ تا ۵۹ باشد" }
    }

    val monthName: String
        get() = MONTH_NAMES[month - 1]

    val dayOfWeekName: String
        get() = JalaliCalendarEngine.getDayOfWeekName(this)

    val isLeapYear: Boolean
        get() = JalaliCalendarEngine.isLeapYear(year)

    /**
     * فرمت عددی استاندارد با ارقام فارسی: ۱۴۰۳/۰۷/۲۲
     */
    fun toFormattedDate(): String {
        val y = year.toString().toPersianDigits()
        val m = month.toString().padStart(2, '0').toPersianDigits()
        val d = day.toString().padStart(2, '0').toPersianDigits()
        return "$y/$m/$d"
    }

    /**
     * فرمت عددی با ارقام اسکی (جهت ارسال به سیستم‌های متنی یا لاگ)
     */
    fun toIsoFormattedDate(): String {
        val m = month.toString().padStart(2, '0')
        val d = day.toString().padStart(2, '0')
        return "$year/$m/$d"
    }

    /**
     * فرمت توصیفی کامل: سه‌شنبه ۲۲ مهر ۱۴۰۳
     */
    fun toFullDescription(): String {
        return "$dayOfWeekName ${day.toPersianDigits()} $monthName ${year.toPersianDigits()}"
    }

    /**
     * فرمت زمان فارسی: ۱۴:۳۵
     */
    fun toFormattedTime(): String {
        val h = hour.toString().padStart(2, '0').toPersianDigits()
        val min = minute.toString().padStart(2, '0').toPersianDigits()
        return "$h:$min"
    }

    /**
     * فرمت کامل تاریخ و زمان: سه‌شنبه ۲۲ مهر ۱۴۰۳ ساعت ۱۴:۳۵
     */
    fun toFullDateTimeString(): String {
        return "${toFullDescription()} ساعت ${toFormattedTime()}"
    }

    override fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        if (day != other.day) return day.compareTo(other.day)
        if (hour != other.hour) return hour.compareTo(other.hour)
        if (minute != other.minute) return minute.compareTo(other.minute)
        return second.compareTo(other.second)
    }

    companion object {
        val MONTH_NAMES = listOf(
            "فروردین",
            "اردیبهشت",
            "خرداد",
            "تیر",
            "مرداد",
            "شهریور",
            "مهر",
            "آبان",
            "آذر",
            "دی",
            "بهمن",
            "اسفند"
        )

        val WEEKDAY_NAMES = listOf(
            "شنبه",
            "یکشنبه",
            "دوشنبه",
            "سه‌شنبه",
            "چهارشنبه",
            "پنج‌شنبه",
            "جمعه"
        )
    }
}
