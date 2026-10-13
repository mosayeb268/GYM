package com.kineticcare.feature.assessment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kineticcare.core.designsystem.theme.PlumbLineCalibratedGreen
import com.kineticcare.core.designsystem.theme.PlumbLineGridWhite
import com.kineticcare.core.designsystem.theme.PlumbLineTiltedRed
import com.kineticcare.core.localization.toPersianDigits
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class AssessmentViewFa(val titleFa: String) {
    ANTERIOR("نمای قدامی (روبرو)"),
    POSTERIOR("نمای خلفی (پشت)"),
    LATERAL_RIGHT("نمای طرفی راست"),
    LATERAL_LEFT("نمای طرفی چپ")
}

/**
 * صفحه فوتوگرامتری ارزیابی پاسچرال با شبکه شاقولی و سنسور شیب‌سنج CameraX
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
@Composable
fun PosturalCaptureScreen(
    onCaptureComplete: (view: AssessmentViewFa, pitch: Float, roll: Float) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedView by remember { mutableStateOf(AssessmentViewFa.LATERAL_RIGHT) }
    var pitchAngle by remember { mutableFloatStateOf(0f) }
    var rollAngle by remember { mutableFloatStateOf(0f) }

    // سنسور ژیروسکوپ / شتاب‌سنج جهت محاسبه شیب و تراز فیزیکی دستگاه
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val ax = event.values[0]
                    val ay = event.values[1]
                    val az = event.values[2]

                    // محاسبه زاویه چرخش عرضی (Roll) و طولی (Pitch)
                    val roll = (atan2(ax.toDouble(), sqrt(ay * ay + az * az).toDouble()) * 180 / Math.PI).toFloat()
                    val pitch = (atan2(ay.toDouble(), sqrt(ax * ax + az * az).toDouble()) * 180 / Math.PI).toFloat()

                    rollAngle = roll
                    pitchAngle = pitch
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // شرط تراز استاندارد: حداکثر خطای مجاز ۱.۵ درجه
    val isLevelCalibrated = abs(rollAngle) <= 1.5f && abs(pitchAngle - 90f) <= 3.0f
    val plumbLineColor = if (isLevelCalibrated) PlumbLineCalibratedGreen else PlumbLineTiltedRed

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // ۱. ویو دوربین زنده CameraX
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        // ۲. لایه خطوط شاقولی شطرنجی بیومکانیک (Plumb-Line Grid Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // شبکه شطرنجی فرعی (Grid Lines)
            val stepX = width / 8
            val stepY = height / 16

            for (i in 1..7) {
                drawLine(
                    color = PlumbLineGridWhite,
                    start = Offset(stepX * i, 0f),
                    end = Offset(stepX * i, height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            for (j in 1..15) {
                drawLine(
                    color = PlumbLineGridWhite,
                    start = Offset(0f, stepY * j),
                    end = Offset(width, stepY * j),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // خط شاقول عمودی اصلی (Main Central Plumb-Line)
            drawLine(
                color = plumbLineColor,
                start = Offset(width / 2, 0f),
                end = Offset(width / 2, height),
                strokeWidth = 4f
            )

            // خط افقی تراز کمربند شانه‌ای و لگن (Shoulder & Pelvis Reference)
            val shoulderY = height * 0.28f
            val pelvisY = height * 0.52f
            val kneeY = height * 0.72f

            drawLine(
                color = plumbLineColor,
                start = Offset(width * 0.2f, shoulderY),
                end = Offset(width * 0.8f, shoulderY),
                strokeWidth = 2f
            )

            drawLine(
                color = plumbLineColor,
                start = Offset(width * 0.2f, pelvisY),
                end = Offset(width * 0.8f, pelvisY),
                strokeWidth = 2f
            )

            drawLine(
                color = plumbLineColor,
                start = Offset(width * 0.25f, kneeY),
                end = Offset(width * 0.75f, kneeY),
                strokeWidth = 2f
            )
        }

        // ۳. هدر انتخاب نماهای چهارگانه پاسچرال
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssessmentViewFa.values().forEach { view ->
                        val isSelected = (view == selectedView)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.clickable { selectedView = view }
                        ) {
                            Text(
                                text = view.titleFa,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else Color.LightGray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // نمایش وضعیت تراز ژیروسکوپ با ارقام فارسی
            Surface(
                color = if (isLevelCalibrated) PlumbLineCalibratedGreen.copy(alpha = 0.85f) else PlumbLineTiltedRed.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isLevelCalibrated) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLevelCalibrated) {
                            "شاقول تراز است (${abs(rollAngle).roundToInt().toPersianDigits()}° خطا) - آماده ثبت"
                        } else {
                            "شیب دستگاه: ${abs(rollAngle).roundToInt().toPersianDigits()}° - گوشی را کاملاً عمود و بدون زاویه نگه دارید"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // ۴. دکمه شاتر ثبت تصویر (تنها هنگام تراز بودن سنسور فعال است)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Button(
                onClick = {
                    if (isLevelCalibrated) {
                        onCaptureComplete(selectedView, pitchAngle, rollAngle)
                    }
                },
                enabled = isLevelCalibrated,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLevelCalibrated) MaterialTheme.colorScheme.primary else Color.Gray,
                    disabledContainerColor = Color.DarkGray
                ),
                modifier = Modifier
                    .size(76.dp)
                    .border(3.dp, Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "ثبت ارزیابی",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
