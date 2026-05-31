package id.pina.bacakomik

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

/**
 * CrashActivity — displays crash info on screen when the app crashes.
 * Uses plain Android Views (no Compose) to minimize crash risk.
 *
 * Flow:
 * 1. PinaKomikApp.UncaughtExceptionHandler saves crash to internal file
 * 2. Handler launches this Activity with FLAG_ACTIVITY_NEW_TASK
 * 3. Activity reads the file and shows stack trace on screen
 * 4. User can Copy or Share via Telegram
 */
class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashFile = getExternalFilesDir(null)?.let {
            java.io.File(it, "crash.log")
        } ?: java.io.File(filesDir, "crash.log")

        val crashText = if (crashFile.exists()) {
            crashFile.readText()
        } else {
            "No crash log found."
        }

        // Build UI programmatically (no XML, no Compose)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(40), dp(20), dp(20))
            setBackgroundColor(0xFF0B0D14.toInt())
        }

        // Title
        layout.addView(TextView(this).apply {
            text = "💥 Pina Komik Crash Report"
            setTextColor(0xFFE63946.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.DEFAULT_BOLD
        })

        // Subtitle
        layout.addView(TextView(this).apply {
            text = "Screenshot this page and send to developer via Telegram"
            setTextColor(0xFF94A3B8.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, dp(8), 0, dp(16))
        })

        // Crash content in scrollable view
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            setBackgroundColor(0xFF15192A.toInt())
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        scrollView.addView(TextView(this).apply {
            text = crashText
            setTextColor(0xFFF1F5F9.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.MONOSPACE
            movementMethod = ScrollingMovementMethod()
            setTextIsSelectable(true)
        })

        layout.addView(scrollView)

        // Button row
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(16), 0, dp(8))
        }

        // Copy button
        buttonRow.addView(Button(this).apply {
            text = "📋 Copy"
            setBackgroundColor(0xFF1C2138.toInt())
            setTextColor(0xFFE63946.toInt())
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("crash", crashText)
                )
                Toast.makeText(this@CrashActivity, "Copied!", Toast.LENGTH_SHORT).show()
            }
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginEnd = dp(8)
            }
        })

        // Share via Telegram button
        buttonRow.addView(Button(this).apply {
            text = "📤 Share"
            setBackgroundColor(0xFFE63946.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, crashText)
                    putExtra(Intent.EXTRA_SUBJECT, "Pina Komik Crash Report")
                    setPackage("org.telegram.messenger")  // Direct to Telegram
                }
                try {
                    startActivity(shareIntent)
                } catch (e: Exception) {
                    // Fallback: general share
                    val fallback = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, crashText)
                        putExtra(Intent.EXTRA_SUBJECT, "Pina Komik Crash Report")
                    }
                    startActivity(Intent.createChooser(fallback, "Share crash report"))
                }
            }
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
        })

        layout.addView(buttonRow)

        setContentView(layout)
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()

    override fun onDestroy() {
        super.onDestroy()
        // Clean up crash file after user has seen it
        val crashFile = getExternalFilesDir(null)?.let {
            java.io.File(it, "crash.log")
        }
        crashFile?.delete()
    }
}
