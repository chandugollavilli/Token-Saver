package com.tokensaver.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.max

class FloatingBadgeManager(private val service: AccessibilityService) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var badge: TextView? = null
    private var preview: LinearLayout? = null

    fun showBadge(bounds: Rect, onTap: () -> Unit) {
        removeBadge()
        val view = TextView(service).apply {
            text = "⚡ Save tokens"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(28, 16, 28, 16)
            setBackgroundColor(0xFF3157D5.toInt())
            elevation = 12f
            setOnClickListener { onTap() }
        }
        val metrics = service.resources.displayMetrics
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left.coerceIn(0, max(0, metrics.widthPixels - 240))
            y = (bounds.top - 64).coerceAtLeast(0)
        }
        runCatching { windowManager.addView(view, params); badge = view }
    }

    fun showPreview(original: String, compressed: String, duplicate: Boolean, onApply: () -> Unit) {
        removeBadge()
        removePreview()
        val panel = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 16)
            setBackgroundColor(0xFFFFFFFF.toInt())
            elevation = 18f
        }
        panel.addView(TextView(service).apply {
            text = if (duplicate) "This prompt was optimized before. Review the suggested edit:" else "Review before applying — your original text is unchanged until you confirm."
            textSize = 15f
            setTextColor(0xFF202124.toInt())
        })
        panel.addView(TextView(service).apply {
            text = compressed
            textSize = 14f
            setTextColor(0xFF202124.toInt())
            setPadding(0, 14, 0, 14)
        })
        val actions = LinearLayout(service).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(Button(service).apply {
            text = "Apply"
            setOnClickListener { onApply(); removePreview() }
        })
        actions.addView(Button(service).apply {
            text = "Cancel"
            setOnClickListener { removePreview() }
        })
        panel.addView(actions)
        val params = WindowManager.LayoutParams(
            (service.resources.displayMetrics.widthPixels * 0.9f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }
        runCatching { windowManager.addView(panel, params); preview = panel }
    }

    fun clear() { removeBadge(); removePreview() }

    private fun removeBadge() {
        badge?.let { runCatching { windowManager.removeView(it) } }
        badge = null
    }

    private fun removePreview() {
        preview?.let { runCatching { windowManager.removeView(it) } }
        preview = null
    }
}
