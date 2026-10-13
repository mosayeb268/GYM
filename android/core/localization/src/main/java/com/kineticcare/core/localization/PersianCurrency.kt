package com.kineticcare.core.localization

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * فرمت‌کننده مبالغ مالی بر حسب واحد تومان و ریال با ارقام فارسی و تفکیک ۳ رقمی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
object PersianCurrency {

    private val formatter: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        DecimalFormat("#,###", symbols)
    }

    /**
     * فرمت‌بندی مبلغ به تومان با جداکننده ۳ رقمی و ارقام فارسی
     * مثال: 2500000 -> ۲,۵۰۰,۰۰۰ تومان
     */
    fun formatToman(amount: Long, includeUnit: Boolean = true): String {
        val formattedAscii = formatter.format(amount)
        val persianFormatted = formattedAscii.toPersianDigits()
        return if (includeUnit) "$persianFormatted تومان" else persianFormatted
    }

    fun formatToman(amount: Int, includeUnit: Boolean = true): String {
        return formatToman(amount.toLong(), includeUnit)
    }

    /**
     * تبدیل مبلغ دریافتی از درگاه پرداخت شتاب/شاپرک (ریال) به تومان با فرمت فارسی
     */
    fun fromRialToToman(rialAmount: Long, includeUnit: Boolean = true): String {
        val toman = rialAmount / 10
        return formatToman(toman, includeUnit)
    }

    /**
     * تبدیل متن فارسی دارای جداکننده و ارقام فارسی به مقدار عددی خالص (Long)
     */
    fun parseAmount(text: String): Long {
        val cleaned = text
            .replace("تومان", "")
            .replace("ریال", "")
            .replace(",", "")
            .replace("،", "")
            .trim()
            .toAsciiDigits()
        return cleaned.toLongOrNull() ?: 0L
    }
}

// Extension Functions جهت دسترسی آسان
fun Long.toToman(includeUnit: Boolean = true): String = PersianCurrency.formatToman(this, includeUnit)
fun Int.toToman(includeUnit: Boolean = true): String = PersianCurrency.formatToman(this, includeUnit)
fun Long.fromRialToToman(includeUnit: Boolean = true): String = PersianCurrency.fromRialToToman(this, includeUnit)
