package de.pocketlock

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

/** Quick settings toggle, so the original app need not be opened/left. */
class PocketTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let { tile ->
            tile.state = if (OverlayService.isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = "Taschenmodus"
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        if (isLocked) {
            unlockAndRun { toggle() }
        } else {
            toggle()
        }
    }

    private fun toggle() {
        if (OverlayService.isActive) {
            stopService(Intent(this, OverlayService::class.java))
            onStartListening()
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            openSettings()
            return
        }
        try {
            startForegroundService(Intent(this, OverlayService::class.java))
        } catch (_: Exception) {
            // Some Android variants reject a service launch here: use a user-visible Activity.
            openSettings()
        }
    }

    private fun openSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val pending = PendingIntent.getActivity(
                this, 6, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
