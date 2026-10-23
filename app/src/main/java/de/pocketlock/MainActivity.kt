package de.pocketlock

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * Offline, native PocketLock home screen. The cat illustration is a bundled asset,
 * and the touch shield itself remains a fully black canvas while active.
 */
class MainActivity : Activity() {
    private val backgroundColor = Color.rgb(11, 13, 19)
    private val mutedColor = Color.rgb(165, 170, 182)
    private val highlightColor = Color.rgb(235, 192, 132)
    private lateinit var statusText: TextView
    private lateinit var lockButton: TextView
    private lateinit var heroImage: ImageView
    private val motifNames = arrayOf(
        "Katzen-Schloss", "Schlafmütze", "Pfote", "Balkon", "Bernsteinaugen"
    )
    private val motifImages = intArrayOf(
        R.drawable.pocketlock_cat, R.drawable.katze_schlafend,
        R.drawable.katze_pfote, R.drawable.katze_balkon, R.drawable.katze_augen
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
        if (Build.VERSION.SDK_INT >= 29) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(backgroundColor)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(22), dp(22), dp(24))
        }
        scroll.addView(column)
        setContentView(scroll)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "PocketLock"
            setTextColor(Color.WHITE)
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            letterSpacing = -0.025f
        }
        header.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val setting = TextView(this).apply {
            text = "Einstellungen"
            setTextColor(Color.rgb(222, 225, 232))
            textSize = 12f
            gravity = Gravity.CENTER
            contentDescription = "Einstellungen und Anleitung"
            setPadding(dp(11), dp(11), dp(11), dp(11))
            background = rounded(Color.rgb(34, 38, 49), 100)
            setOnClickListener { showInstructions() }
        }
        header.addView(setting)
        column.addView(header, lp(top = 8))

        val motto = label("Dein Bildschirm. In Ruhe.", 16f, mutedColor)
        motto.gravity = Gravity.START
        column.addView(motto, lp(top = 8, bottom = 18))

        // The kitten and pocket lock illustration is bundled in the APK: no network calls.
        heroImage = ImageView(this).apply {
            val selected = getPreferences(MODE_PRIVATE).getInt("motif", 0).coerceIn(0, motifImages.lastIndex)
            setImageResource(motifImages[selected])
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Graue Katze mit bernsteinfarbenen Augen im PocketLock-Taschenmotiv"
            background = rounded(Color.rgb(20, 25, 35), 28)
            clipToOutline = true
        }
        val availableWidth = resources.displayMetrics.widthPixels - dp(44)
        val heroHeight = (availableWidth * 0.91f).toInt().coerceAtMost(dp(390))
        column.addView(heroImage, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heroHeight))

        val changeMotif = TextView(this).apply {
            text = "Katzenmotiv auswählen"
            textSize = 13f
            setTextColor(Color.rgb(217, 188, 153))
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(8))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                val selected = getPreferences(MODE_PRIVATE).getInt("motif", 0)
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Dein Katzenmotiv")
                    .setSingleChoiceItems(motifNames, selected) { dialog, item ->
                        getPreferences(MODE_PRIVATE).edit().putInt("motif", item).apply()
                        heroImage.setImageResource(motifImages[item])
                        dialog.dismiss()
                    }
                    .setNegativeButton("Abbrechen", null)
                    .show()
            }
        }
        column.addView(changeMotif, lp(top = 2))

        statusText = label("Status wird geprüft", 13f, Color.rgb(183, 218, 184)).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(28, 35, 36), 32)
            setPadding(dp(16), dp(11), dp(16), dp(11))
        }
        val statusWrap = LinearLayout(this).apply { gravity = Gravity.CENTER }
        statusWrap.addView(statusText)
        column.addView(statusWrap, lp(top = 18, bottom = 15))

        lockButton = TextView(this).apply {
            text = "Taschenmodus starten"
            textSize = 19f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(24, 27, 34))
            background = rounded(Color.rgb(244, 239, 231), 22)
            elevation = dp(6).toFloat()
            minHeight = dp(64)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { startPocketMode() }
        }
        column.addView(lockButton, lp(bottom = 12))

        column.addView(label(
            "Bildschirm abdunkeln und unbeabsichtigte Berührungen blockieren.",
            14f, mutedColor
        ).apply { gravity = Gravity.CENTER }, lp(bottom = 18))

        val tile = TextView(this).apply {
            text = "Schnellschalter hinzufügen"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(236, 238, 242))
            background = rounded(Color.rgb(32, 38, 49), 18)
            minHeight = dp(54)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setOnClickListener { requestQuickTile() }
        }
        column.addView(tile, lp(bottom = 18))

        val howTo = label("ENTSPERREN", 11f, highlightColor).apply {
            letterSpacing = 0.16f
            setTypeface(null, Typeface.BOLD)
        }
        column.addView(howTo, lp(bottom = 8))
        column.addView(label(
            "3 Sekunden gedrückt halten, dann ohne Loslassen nach rechts wischen.",
            14f, Color.rgb(215, 218, 227)
        ).apply { gravity = Gravity.CENTER }, lp(bottom = 16))

        val note = label(
            "Hinweis: Das Display bleibt technisch eingeschaltet. " +
                "Ob andere Apps im Hintergrund weiterarbeiten, hängt von Android und der jeweiligen App ab.",
            12f, Color.rgb(143, 152, 166)
        )
        note.gravity = Gravity.CENTER
        column.addView(note, lp(bottom = 4))
    }

    override fun onResume() {
        super.onResume()
        if (!::statusText.isInitialized) return
        when {
            !Settings.canDrawOverlays(this) -> {
                statusText.text = "Einblendungsberechtigung erforderlich"
                statusText.setTextColor(Color.rgb(241, 188, 139))
                lockButton.text = "Berechtigung erteilen"
            }
            OverlayService.isActive -> {
                statusText.text = "Taschenmodus ist aktiv"
                statusText.setTextColor(Color.rgb(187, 222, 188))
                lockButton.text = "Taschenmodus aktiv"
            }
            else -> {
                statusText.text = "Bereit für den Taschenmodus"
                statusText.setTextColor(Color.rgb(187, 222, 188))
                lockButton.text = "Taschenmodus starten"
            }
        }
        lockButton.isEnabled = !OverlayService.isActive
        lockButton.alpha = if (OverlayService.isActive) 0.5f else 1f
    }

    private fun startPocketMode() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Berechtigung für PocketLock")
                .setMessage("Um Berührungen in anderen Apps zu blockieren, benötigt PocketLock einmalig die Android-Berechtigung 'Über anderen Apps einblenden'.")
                .setPositiveButton("Einstellungen öffnen") { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                }
                .setNegativeButton("Abbrechen", null)
                .show()
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            // Wait for the Android permission dialog to complete before closing this Activity.
            // Otherwise the foreground-service notification may never become accessible.
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
            return
        }
        launchOverlayService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 7) {
            if (grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Benachrichtigungen sind aus: Bitte Entsperrgeste verwenden.",
                    Toast.LENGTH_LONG
                ).show()
            }
            // Permission is optional; the overlay remains user-initiated.
            launchOverlayService()
        }
    }

    private fun launchOverlayService() {
        try {
            startForegroundService(Intent(this, OverlayService::class.java))
            finish()
        } catch (error: Exception) {
            Toast.makeText(this, "Taschenmodus konnte nicht starten: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestQuickTile() {
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                val manager = getSystemService(StatusBarManager::class.java)
                manager.requestAddTileService(
                    ComponentName(this, PocketTileService::class.java),
                    "Taschenmodus",
                    Icon.createWithResource(this, R.drawable.ic_lock),
                    mainExecutor
                ) { result ->
                    if (result != StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED) {
                        Toast.makeText(this, "Schnelleinstellungen öffnen und Taschenmodus manuell hinzufügen.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (_: Exception) {
                Toast.makeText(this, "Schnelleinstellungen bearbeiten und Taschenmodus hinzufügen.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Schnelleinstellungen bearbeiten und Taschenmodus hinzufügen.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showInstructions() {
        AlertDialog.Builder(this)
            .setTitle("PocketLock | Anleitung")
            .setMessage(
                "1. Taschenmodus starten oder Schnellschalter verwenden.\n\n" +
                "2. Der Bildschirm wird schwarz; Berührungen im App-Fenster werden abgefangen.\n\n" +
                "3. Entsperren: 3 Sekunden still halten und ohne Loslassen nach rechts wischen.\n\n" +
                "4. Notausgang: Benachrichtigung 'Taschenmodus aktiv' öffnen und 'Beenden' wählen.\n\n" +
                "Keine Sicherheits-Displaysperre. Android-Systemgesten und Hardwaretasten bleiben verfügbar. " +
                "Das Display bleibt eingeschaltet; Akkuverbrauch und Wärme beobachten."
            )
            .setPositiveButton("Verstanden", null)
            .show()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun lp(top: Int = 0, bottom: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }
    private fun label(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER
        includeFontPadding = true
    }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }
}
