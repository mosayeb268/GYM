package com.kineticcare.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * کلاینت ارتباط با سرور و APIهای سیستم عامل کینتیک کر
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
class KineticApiClient(
    private var baseUrl: String = "http://10.0.2.2:4000/api/v1"
) {
    private var authToken: String? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun setAuthToken(token: String?) {
        this.authToken = token
    }

    fun setBaseUrl(url: String) {
        this.baseUrl = url.trimEnd('/')
    }

    /**
     * ۱. درخواست ارسال کد OTP پیامکی
     */
    suspend fun requestOtp(phoneNumber: String): NetworkResult<String> = withContext(Dispatchers.IO) {
        val payload = buildJsonObject { put("phoneNumber", phoneNumber) }
        postJson("/auth/request-otp", payload) { jsonResp ->
            jsonResp["messageFa"]?.jsonPrimitive?.contentOrNull ?: "کد تایید پیامک شد."
        }
    }

    /**
     * ۲. اعتبارسنجی کد پیامکی و دریافت توکن JWT
     */
    suspend fun verifyOtp(phoneNumber: String, code: String): NetworkResult<Pair<String, String>> = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("phoneNumber", phoneNumber)
            put("code", code)
        }
        postJson("/auth/verify-otp", payload) { jsonResp ->
            val token = jsonResp["token"]?.jsonPrimitive?.contentOrNull ?: ""
            val userObj = jsonResp["user"]?.jsonObject
            val fullName = userObj?.get("fullName")?.jsonPrimitive?.contentOrNull ?: "ورزشکار"
            setAuthToken(token)
            Pair(token, fullName)
        }
    }

    /**
     * ۳. ارزیابی نمره درد مراجع و رگرسیون هوشمند تمرین
     */
    suspend fun evaluatePain(
        clientId: string,
        exerciseId: string,
        prePainVas: Int,
        painRegion: String
    ): NetworkResult<Triple<Boolean, String, String?>> = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("clientId", clientId)
            put("exerciseId", exerciseId)
            put("prePainVas", prePainVas)
            put("painRegion", painRegion)
        }
        postJson("/biomechanics/evaluate-pain", payload) { jsonResp ->
            val hasRedFlag = jsonResp["hasRedFlag"]?.jsonPrimitive?.contentOrNull?.toBoolean() ?: false
            val origTitle = jsonResp["originalExerciseTitleFa"]?.jsonPrimitive?.contentOrNull ?: ""
            val regTitle = jsonResp["regressedExerciseTitleFa"]?.jsonPrimitive?.contentOrNull
            Triple(hasRedFlag, origTitle, regTitle)
        }
    }

    /**
     * ۴. سیستم جایگزینی و تبادل درشت‌مغذی‌های غذایی
     */
    suspend fun exchangeFood(
        sourceFood: String,
        targetFood: String,
        portionGrams: Int
    ): NetworkResult<Pair<Int, String>> = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("sourceFood", sourceFood)
            put("targetFood", targetFood)
            put("portionGrams", portionGrams)
        }
        postJson("/nutrition/exchange", payload) { jsonResp ->
            val targetGrams = jsonResp["targetPortionGrams"]?.jsonPrimitive?.intOrNull ?: portionGrams
            val summary = jsonResp["macroSummaryFa"]?.jsonPrimitive?.contentOrNull ?: ""
            Pair(targetGrams, summary)
        }
    }

    /**
     * ۵. ثبت‌نام مراجع جدید در پذیرش
     */
    suspend fun registerClient(
        fullName: String,
        phoneNumber: String,
        nationalCode: String?
    ): NetworkResult<String> = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("fullName", fullName)
            put("phoneNumber", phoneNumber)
            nationalCode?.let { put("nationalCode", it) }
        }
        postJson("/clients/register", payload) { jsonResp ->
            jsonResp["messageFa"]?.jsonPrimitive?.contentOrNull ?: "مراجع با موفقیت ثبت شد."
        }
    }

    /**
     * ۶. تنظیم دستی سهمیه جلسات ورزشکار
     */
    suspend fun adjustSessions(
        subscriptionId: String,
        deltaSessions: Int,
        reasonFa: String
    ): NetworkResult<String> = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("subscriptionId", subscriptionId)
            put("deltaSessions", deltaSessions)
            put("reasonFa", reasonFa)
        }
        postJson("/clients/adjust-sessions", payload) { jsonResp ->
            jsonResp["messageFa"]?.jsonPrimitive?.contentOrNull ?: "سهمیه با موفقیت به‌روزرسانی شد."
        }
    }

    private fun <T> postJson(
        path: String,
        payload: JsonObject,
        mapper: (JsonObject) -> T
    ): NetworkResult<T> {
        return try {
            val url = URL("$baseUrl$path")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            authToken?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true

            OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                os.write(payload.toString())
                os.flush()
            }

            val statusCode = conn.responseCode
            if (statusCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonObject = json.parseToJsonElement(responseText).jsonObject
                NetworkResult.Success(mapper(jsonObject))
            } else {
                val errorText = conn.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
                val errorMsg = try {
                    json.parseToJsonElement(errorText).jsonObject["error"]?.jsonPrimitive?.contentOrNull
                } catch (e: Exception) {
                    null
                } ?: "خطای سرور کلینیک (کد $statusCode)"
                NetworkResult.Error(errorMsg, statusCode)
            }
        } catch (e: java.net.ConnectException) {
            NetworkResult.Error("عدم برقراری ارتباط با سرور کلینیک. لطفاً اتصال اینترنت یا شبکه محلی را بررسی نمایید.")
        } catch (e: java.net.SocketTimeoutException) {
            NetworkResult.Error("مهلت زمان پاسخ‌دهی سرور به پایان رسید (Timeout).")
        } catch (e: Exception) {
            NetworkResult.Error("خطای غیرمنتظره در ارتباط: ${e.localizedMessage}")
        }
    }
}
