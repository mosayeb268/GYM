package com.kineticcare.feature.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

/**
 * دریافت‌کننده خودکار کد پیامک بدون نیاز به اجازه خواندن SMS (Android SMS Retriever API)
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
class SmsRetrieverReceiver(
    private val onOtpReceived: (String) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: ""
                    extractOtpCode(message)?.let { code ->
                        onOtpReceived(code)
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    // منقضی شدن مهلت پنج دقیقه دریافت خودکار پیامک
                }
            }
        }
    }

    private fun extractOtpCode(message: String): String? {
        // استخراج کد ۵ رقمی پیامک شده
        val pattern = Pattern.compile("\\b(\\d{5})\\b")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }
}
