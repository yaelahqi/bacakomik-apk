package id.pina.bacakomik

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import id.pina.bacakomik.ui.AppRoot
import id.pina.bacakomik.ui.theme.PinaKomikTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        recordStage("MainActivity.onCreate.start")

        // Splash
        try { installSplashScreen() } catch (t: Throwable) {
            Log.e("PinaKomik", "splash failed", t); recordStage("splash.fail")
        }

        super.onCreate(savedInstanceState)
        recordStage("super.onCreate.done")

        // Check if previous run crashed → show diagnostics screen instead of Compose
        val prefs = getSharedPreferences("pina_crash", MODE_PRIVATE)
        val lastCrash = prefs.getString("last_crash", null)
        val showDiag = lastCrash != null

        if (showDiag) {
            recordStage("showing.diagnostics")
            setContentView(buildDiagnosticsView(lastCrash!!))
            // Clear so next launch tries Compose again
            prefs.edit().remove("last_crash").apply()
            return
        }

        try { enableEdgeToEdge() } catch (t: Throwable) {
            Log.e("PinaKomik", "enableEdgeToEdge failed", t); recordStage("edgeToEdge.fail")
        }

        try {
            recordStage("setContent.start")
            setContent {
                PinaKomikTheme {
                    AppRoot()
                }
            }
            recordStage("setContent.done")
        } catch (t: Throwable) {
            Log.e("PinaKomik", "setContent failed", t)
            recordStage("setContent.fail: ${t.javaClass.simpleName}: ${t.message}")
            // Save and redirect
            try {
                val sw = java.io.StringWriter()
                t.printStackTrace(java.io.PrintWriter(sw))
                prefs.edit().putString("last_crash", "=== setContent crash ===\n${sw}").commit()
                startActivity(Intent(this, CrashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                finish()
            } catch (e: Throwable) {
                throw t
            }
        }
    }

    private fun recordStage(stage: String) {
        try {
            val prefs = getSharedPreferences("pina_lifecycle", MODE_PRIVATE)
            val ts = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US)
                .format(java.util.Date())
            val existing = prefs.getString("trace", "") ?: ""
            prefs.edit().putString("trace", "$existing\n[$ts] $stage").apply()
        } catch (_: Throwable) {}
    }

    private fun buildDiagnosticsView(crashText: String): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0B0D14.toInt())
            setPadding(dp(20), dp(40), dp(20), dp(20))
        }

        layout.addView(TextView(this).apply {
            text = "🔧 Pina Komik — Diagnostics"
            setTextColor(0xFFE63946.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.DEFAULT_BOLD
        })

        layout.addView(TextView(this).apply {
            text = "App crashed last session. Screenshot this and send to dev. Tap 'Try Again' to retry."
            setTextColor(0xFF94A3B8.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(0, dp(8), 0, dp(16))
        })

        // Lifecycle trace
        val lifecyclePrefs = getSharedPreferences("pina_lifecycle", MODE_PRIVATE)
        val trace = lifecyclePrefs.getString("trace", "(empty)") ?: "(empty)"

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            setBackgroundColor(0xFF15192A.toInt())
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        val content = TextView(this).apply {
            text = "=== LIFECYCLE TRACE ===\n$trace\n\n=== CRASH ===\n$crashText"
            setTextColor(0xFFF1F5F9.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        }
        scroll.addView(content)
        layout.addView(scroll)

        // Action buttons
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(16), 0, dp(8))
        }

        row.addView(Button(this).apply {
            text = "📋 Copy"
            setBackgroundColor(0xFF1C2138.toInt())
            setTextColor(0xFFE63946.toInt())
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener {
                val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager
                cm.setPrimaryClip(android.content.ClipData.newPlainText("crash", content.text))
                android.widget.Toast.makeText(this@MainActivity, "Copied!",
                    android.widget.Toast.LENGTH_SHORT).show()
            }
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginEnd = dp(8)
            }
        })

        row.addView(Button(this).apply {
            text = "🔄 Try Again"
            setBackgroundColor(0xFFE63946.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener {
                // Clear lifecycle trace too
                lifecyclePrefs.edit().clear().apply()
                finish()
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
            }
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
        })

        layout.addView(row)
        return layout
    }

    private fun dp(v: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()
}
