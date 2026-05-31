package id.pina.bacakomik

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView

/**
 * v2.0.9 — ULTRA MINIMAL
 * Plain Activity (not ComponentActivity), no Application class,
 * no dependencies. If this STILL crashes, issue is signing/device.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("PinaKomik", "minimal onCreate START")
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0B0D14.toInt())
            setPadding(64, 200, 64, 64)
        }
        layout.addView(TextView(this).apply {
            text = "✅ Pina Komik\nAlive on Android ${android.os.Build.VERSION.SDK_INT}"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
        })
        setContentView(layout)
        Log.i("PinaKomik", "minimal onCreate DONE")
    }
}
