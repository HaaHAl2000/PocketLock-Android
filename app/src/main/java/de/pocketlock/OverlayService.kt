package de.pocketlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.TileService
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * User-controlled touch-blocking overlay. It does NOT secure the device and
 * does NOT override other apps' background execution rules.
 */
class OverlayService : Service() {
    companion object {
        const val ACTION_STOP = "de.pocketlock.action.STOP"
        private const val CHANNEL = "pocketlock_active"
        private const val NOTIFICATION_ID = 1001
        @Volatile var isActive = false
            private set

        fun refreshTile(context: Context) {
            try {
                TileService.requestListeningState(
                    context, ComponentName(context, PocketTileService::class.java)
                )
            } catch (_: Exception) { /* Tile may not be added */ }
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlay: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Taschenmodus", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val stopIntent = Intent(this, OverlayService::class.java).setAction(ACTION_STOP)
        val stopAction = PendingIntent.getService(
            this, 10, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_lock)
            .setContentTitle("Taschenmodus aktiv")
            .setContentText("Berührungen werden blockiert. Beenden über diese Meldung.")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .addAction(R.drawable.ic_lock, "Beenden", stopAction)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            if (overlay == null) showOverlay()
        } catch (_: Exception) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        val view = PocketLockView(this) { stopSelf() }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
            title = "PocketLock touch protection"
        }
        windowManager.addView(view, params)
        overlay = view
        isActive = true
        refreshTile(this)
    }

    override fun onDestroy() {
        overlay?.let { view ->
            try { windowManager.removeViewImmediate(view) } catch (_: Exception) { }
        }
        overlay = null
        isActive = false
        refreshTile(this)
        super.onDestroy()
    }
}
