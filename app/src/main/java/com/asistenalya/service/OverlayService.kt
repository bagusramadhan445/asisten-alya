package com.asistenalya.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.asistenalya.MainActivity
import com.asistenalya.R
import com.asistenalya.data.repository.AIRepository
import com.asistenalya.domain.model.AssistantState
import com.asistenalya.manager.AlyaPowerManager
import com.asistenalya.manager.AppLauncher
import com.asistenalya.manager.AssistantStateManager
import com.asistenalya.manager.IntentParser
import com.asistenalya.manager.IntentRouter
import com.asistenalya.manager.SpeechManager
import com.asistenalya.manager.TTSManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "alya_overlay_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: OrbOverlayView? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var speechManager: SpeechManager? = null
    private var ttsManager: TTSManager? = null
    private var intentRouter: IntentRouter? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        speechManager = SpeechManager(this)
        ttsManager = TTSManager(this)
        val appLauncher = AppLauncher(this)
        val powerManager = AlyaPowerManager(this)
        val aiRepository = AIRepository()
        intentRouter = IntentRouter(this, appLauncher, ttsManager!!, powerManager, aiRepository)

        speechManager?.onResult = { text ->
            val intent = IntentParser.parse(text)
            intentRouter?.route(intent)
        }

        showOverlay()
        observeState()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeOverlay()
        speechManager?.destroy()
        ttsManager?.shutdown()
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Asisten Alya",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Asisten Alya aktif"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Asisten Alya aktif")
            .setContentText("Tap orb untuk mulai bicara")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun onOrbTapped() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            speechManager?.startListening()
        } else {
            mainHandler.post {
                Toast.makeText(this, "Izin mikrofon belum diberikan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onOrbLongPressed() {
        AssistantStateManager.toggleContinuousListening()
        val enabled = AssistantStateManager.continuousListening.value
        mainHandler.post {
            val msg = if (enabled) "Continuous listening aktif" else "Continuous listening nonaktif"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        if (enabled) {
            onOrbTapped()
        }
    }

    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = OrbOverlayView(this)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            150,
            150,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = 0
            y = 0
        }

        windowManager?.addView(overlayView, params)

        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var longPressTriggered = false
            private val longPressHandler = Handler(Looper.getMainLooper())
            private val longPressRunnable = Runnable {
                longPressTriggered = true
                onOrbLongPressed()
            }

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        longPressTriggered = false
                        longPressHandler.postDelayed(longPressRunnable, 800)
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (kotlin.math.abs(dx) > 10 || kotlin.math.abs(dy) > 10) {
                            longPressHandler.removeCallbacks(longPressRunnable)
                        }
                        params.x = initialX - dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(overlayView, params)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        longPressHandler.removeCallbacks(longPressRunnable)
                        val dx = kotlin.math.abs(event.rawX - initialTouchX)
                        val dy = kotlin.math.abs(event.rawY - initialTouchY)
                        if (dx < 10 && dy < 10 && !longPressTriggered) {
                            onOrbTapped()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        overlayView = null
    }

    private fun observeState() {
        scope.launch {
            AssistantStateManager.state.collect { state ->
                overlayView?.updateState(state)
            }
        }
    }

    private class OrbOverlayView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var currentState = AssistantState.READY
        private val handler = Handler(Looper.getMainLooper())
        private var animPhase = 0f

        private val animRunnable = object : Runnable {
            override fun run() {
                animPhase += 0.1f
                if (animPhase > 2f * Math.PI.toFloat()) animPhase = 0f
                invalidate()
                handler.postDelayed(this, 50)
            }
        }

        init {
            handler.post(animRunnable)
        }

        fun updateState(state: AssistantState) {
            currentState = state
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val baseRadius = minOf(cx, cy) - 5f

            val pulse = 1f + 0.1f * kotlin.math.sin(animPhase.toDouble()).toFloat()
            val radius = baseRadius * pulse

            val (innerColor, outerColor) = when (currentState) {
                AssistantState.READY -> Pair(Color.parseColor("#FF69B4"), Color.parseColor("#4A0030"))
                AssistantState.LISTENING -> Pair(Color.parseColor("#00BFFF"), Color.parseColor("#001A33"))
                AssistantState.THINKING -> Pair(Color.parseColor("#9B59B6"), Color.parseColor("#2D0A3E"))
                AssistantState.SPEAKING -> Pair(Color.parseColor("#FF69B4"), Color.parseColor("#3D0020"))
                AssistantState.ERROR -> Pair(Color.parseColor("#FF4444"), Color.parseColor("#330000"))
            }

            // Outer glow
            paint.shader = null
            paint.color = innerColor
            paint.alpha = 40
            canvas.drawCircle(cx, cy, radius + 8f, paint)

            // Main orb
            paint.alpha = 255
            paint.shader = RadialGradient(cx, cy, radius, innerColor, outerColor, Shader.TileMode.CLAMP)
            canvas.drawCircle(cx, cy, radius, paint)

            // Inner bright core
            paint.shader = null
            paint.color = Color.WHITE
            paint.alpha = (120 * pulse).toInt().coerceIn(0, 255)
            canvas.drawCircle(cx, cy, radius * 0.25f, paint)
        }

        override fun onDetachedFromWindow() {
            handler.removeCallbacks(animRunnable)
            super.onDetachedFromWindow()
        }
    }
}
