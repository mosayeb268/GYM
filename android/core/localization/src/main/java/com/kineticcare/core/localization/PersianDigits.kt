package com.kineticcare.core.localization

/**
 * موتور تبدیل ارقام فارسی و اعتبارسنجی الگوهای مخابراتی و هویتی ایران
 * ویژه سیستم عامل کینتیک کر - کلینیک دکتر قدم‌پور
 */
object PersianDigits {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    /**
     * تبدیل تمامی ارقام لاتین (0-9) و عربی به ارقام استاندارد فارسی (۰-۹)
     */
    fun convertToPersian(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val builder = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '0'..'9' -> builder.append(PERSIAN_DIGITS[ch - '0'])
                in '٠'..'٩' -> builder.append(PERSIAN_DIGITS[ch - '٠'])
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }

    /**
     * تبدیل ارقام فارسی و عربی به ارقام استاندارد اسکی (0-9) جهت ارسال به API و دیتابیس
     */
    fun convertToAscii(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val builder = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> builder.append((ch - '۰' + '0'.code).toChar())
                in '٠'..'٩' -> builder.append((ch - '٠' + '0'.code).toChar())
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }
}

// Extension Functions عمومی جهت تبدیل سریع
fun String.toPersianDigits(): String = PersianDigits.convertToPersian(this)
fun Int.toPersianDigits(): String = PersianDigits.convertToPersian(this.toString())
fun Long.toPersianDigits(): String = PersianDigits.convertToPersian(this.toString())
fun Float.toPersianDigits(): String = PersianDigits.convertToPersian(this.toString())
fun Double.toPersianDigits(): String = PersianDigits.convertToPersian(this.toString())

fun String.toAsciiDigits(): String = PersianDigits.convertToAscii(this)

/**
 * اعتبارسنجی شماره موبایل اپراتورهای ایران (همراه اول، ایرانسل، رایتل، شاتل موبایل و ...)
 * الگوهای مجاز: 09123456789 یا +989123456789
 */
fun String.isValidIranianMobile(): Boolean {
    val clean = this.trim().toAsciiDigits()
    val regex = Regex("^(09|\\+989)[0-9]{9}$")
    return regex.matches(clean)
}

/**
 * استانداردسازی شماره موبایل به فرمت داخلی (09xxxxxxxxx)
 */
fun String.normalizeIranianMobile(): String {
    val clean = this.trim().toAsciiDigits().replace(" ", "")
    return when {
        clean.startsWith("+98") -> "0" + clean.substring(3)
        clean.startsWith("0098") -> "0" + clean.substring(4)
        clean.startsWith("98") -> "0" + clean.substring(2)
        else -> clean
    }
}

/**
 * اعتبارسنجی کد ملی ۱۰ رقمی با الگوریتم رسمی باقیمانده بر ۱۱
 */
fun String.isValidIranianNationalCode(): Boolean {
    val code = this.trim().toAsciiDigits()
    if (!code.matches(Regex("^[0-9]{10}$"))) return false

    // رد کدهای تکراری نامعتبر مانند 1111111111
    if ((0..9).any { digit -> code == digit.toString().repeat(10) }) {
        return false
    }

    val checkDigit = code[9].digitToInt()
    var sum = 0
    for (i in 0 until 9) {
        sum += code[i].digitToInt() * (10 - i)
    }

    val remainder = sum % 11
    return if (remainder < 2) {
        checkDigit == remainder
    } else {
        checkDigit == (11 - remainder)
    }
}
