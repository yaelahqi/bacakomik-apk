package id.pina.bacakomik

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity

/**
 * v2.0.7 — SMOKE TEST
 * Bare minimum: plain View, zero Compose.
 * If this shows "Hello Pina Komik" = bug is in Compose tree.
 * If this crashes = bug is in Application / dependency / native level.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("PinaKomik", ">>> smoke test onCreate START")
        super.onCreate(savedInstanceState)
        Log.d("PinaKomik", ">>> smoke test super.onCreate DONE")

        val tv = TextView(this).apply {
            text = "✅ Hello Pina Komik!\nApp works. Compose is the problem."
            textSize = 22f
            setPadding(64, 128, 64, 64)
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF0B0D14.toInt())
        }
        setContentView(tv)
        Log.d("PinaKomik", ">>> smoke test setContentView DONE")
    }
}
