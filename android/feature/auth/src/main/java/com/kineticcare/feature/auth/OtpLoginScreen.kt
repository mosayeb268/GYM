package com.kineticcare.feature.auth

import android.content.IntentFilter
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.kineticcare.core.designsystem.theme.KineticTealPrimary
import com.kineticcare.core.localization.isValidIranianMobile
import com.kineticcare.core.localization.normalizeIranianMobile
import com.kineticcare.core.localization.toPersianDigits
import kotlinx.coroutines.delay

/**
 * صفحه احراز هویت پیامکی بدون رمز عبور (Passwordless SMS OTP) با پشتیبانی از پر شدن خودکار کدهای دریافتی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
@Composable
fun OtpLoginScreen(
    onLoginSuccess: (token: String, phone: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpDigits by remember { mutableStateOf(listOf("", "", "", "", "")) }
    var countdownSeconds by remember { mutableIntStateOf(120) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // راه‌اندازی و ثبت شنونده SMS Retriever API هنگام ورود به مرحله تایید کد
    DisposableEffect(isOtpSent) {
        if (isOtpSent) {
            val client = SmsRetriever.getClient(context)
            client.startSmsRetriever()

            val receiver = SmsRetrieverReceiver { receivedCode ->
                if (receivedCode.length == 5) {
                    otpDigits = receivedCode.map { it.toString() }
                }
            }

            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, intentFilter)
            }

            onDispose {
                try {
                    context.unregisterReceiver(receiver)
                } catch (e: Exception) {
                    // نادیده گرفتن خطای عدم ثبت
                }
            }
        } else {
            onDispose { }
        }
    }

    // تایمر شمارش معکوس ۱۲۰ ثانیه‌ای
    LaunchedEffect(isOtpSent, countdownSeconds) {
        if (isOtpSent && countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    val focusRequesters = remember { List(5) { FocusRequester() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ۱. آیکون و برندینگ کلینیک دکتر قدم‌پور
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ورود به سامانه کینتیک کر",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور",
            style = MaterialTheme.typography.bodyMedium,
            color = KineticTealPrimary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (!isOtpSent) {
                    // فاز ۱: دریافت شماره موبایل
                    Text(
                        text = "شماره تلفن همراه خود را وارد نمایید:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "کد تایید ورود یکبار مصرف (OTP) برای شما ارسال خواهد شد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            errorMessage = null
                        },
                        label = { Text("شماره همراه (مثلاً ۰۹۱۲۳۴۵۶۷۸۹)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.isValidIranianMobile()) {
                                isOtpSent = true
                                countdownSeconds = 120
                                errorMessage = null
                            } else {
                                errorMessage = "لطفاً شماره موبایل معتبر ۱۱ رقمی ایران (با پیش‌شماره ۰۹) وارد کنید."
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "دریافت کد تایید پیامکی",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    // فاز ۲: ورود کد ۵ رقمی پیامک شده
                    Text(
                        text = "کد تایید ارسال‌شده را وارد کنید:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "کد ۵ رقمی به شماره ${phoneNumber.normalizeIranianMobile().toPersianDigits()} پیامک شد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ۵ کادر ورود ارقام با ناوبری خودکار فوکوس
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 5) {
                            OutlinedTextField(
                                value = otpDigits[i],
                                onValueChange = { value ->
                                    if (value.length <= 1) {
                                        val newDigits = otpDigits.toMutableList()
                                        newDigits[i] = value
                                        otpDigits = newDigits

                                        if (value.isNotEmpty() && i < 4) {
                                            focusRequesters[i + 1].requestFocus()
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .size(54.dp)
                                    .focusRequester(focusRequesters[i]),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // تایمر معکوس ۱۲۰ ثانیه‌ای با ارقام فارسی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (countdownSeconds > 0) {
                            Text(
                                text = "اعتبار کد: ${countdownSeconds.toPersianDigits()} ثانیه",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            TextButton(onClick = {
                                countdownSeconds = 120
                                otpDigits = listOf("", "", "", "", "")
                            }) {
                                Text(
                                    text = "ارسال مجدد کد پیامکی",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        TextButton(onClick = {
                            isOtpSent = false
                            errorMessage = null
                        }) {
                            Text(
                                text = "ویرایش شماره همراه",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val isComplete = otpDigits.all { it.isNotEmpty() }
                    Button(
                        onClick = {
                            val fullCode = otpDigits.joinToString("")
                            if (fullCode.length == 5) {
                                onLoginSuccess("fake_jwt_token_sample", phoneNumber.normalizeIranianMobile())
                            }
                        },
                        enabled = isComplete,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "ورود به کلینیک",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
