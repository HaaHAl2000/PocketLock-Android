package de.pocketlock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/** Touches are consumed; unlock = stationary 3s hold, then right swipe. */
class PocketLockView(context: Context, private val onUnlock: () -> Unit) : View(context) {
    private val density = resources.displayMetrics.density
    private val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(110, 119, 135)
        textSize = 13f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }
    private var hint = "3 Sekunden halten, dann nach rechts wischen"
    private var hintVisible = true
    private var holding = false
    private var armed = false
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var downTime = 0L
    private var moveInvalidatedHold = false
    private var unlocked = false
    private val armRunnable = Runnable {
        if (holding && !moveInvalidatedHold && !unlocked &&
            SystemClock.uptimeMillis() - downTime >= 3000L
        ) {
            armed = true
            hint = "Jetzt nach rechts wischen"
            hintVisible = true
            invalidate()
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    init {
        setBackgroundColor(Color.BLACK)
        isClickable = true
        isHapticFeedbackEnabled = true
        contentDescription = "Taschenmodus aktiv. Zum Entsperren drei Sekunden halten, dann nach rechts wischen."
        postDelayed({ hintVisible = false; invalidate() }, 4400L)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        if (hintVisible) {
            if (armed) {
                canvas.drawText("Jetzt nach rechts wischen", width / 2f, height * 0.47f, captionPaint)
            } else {
                canvas.drawText("3 Sekunden halten", width / 2f, height * 0.46f, captionPaint)
                canvas.drawText("dann nach rechts wischen", width / 2f, height * 0.46f + 24f * density, captionPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                removeCallbacks(armRunnable)
                holding = true
                armed = false
                moveInvalidatedHold = false
                downX = event.x
                downY = event.y
                lastX = downX
                lastY = downY
                downTime = SystemClock.uptimeMillis()
                postDelayed(armRunnable, 3000L)
            }
            MotionEvent.ACTION_MOVE -> {
                lastX = event.x
                lastY = event.y
                if (armed) {
                    if (lastX - downX > 160f * density &&
                        abs(lastY - downY) < 180f * density && !unlocked
                    ) {
                        unlocked = true
                        holding = false
                        removeCallbacks(armRunnable)
                        onUnlock()
                    }
                } else if (abs(lastX - downX) > 24f * density ||
                    abs(lastY - downY) > 24f * density
                ) {
                    moveInvalidatedHold = true
                    removeCallbacks(armRunnable)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                holding = false
                armed = false
                removeCallbacks(armRunnable)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                holding = false
                armed = false
                removeCallbacks(armRunnable)
                if (!unlocked) {
                    hintVisible = false
                    invalidate()
                }
            }
        }
        return true
    }
}
