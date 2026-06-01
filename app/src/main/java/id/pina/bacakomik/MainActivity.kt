package id.pina.bacakomik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkScheme()) {
                Hello()
            }
        }
    }
}

@Composable
private fun Hello() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D14))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🎨 Compose works!",
            color = Color(0xFFE63946),
            fontSize = 28.sp,
        )
        Text(
            text = "Step 1: ComponentActivity + Compose minimal",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
        )
    }
}

private fun darkScheme() = androidx.compose.material3.darkColorScheme(
    background = Color(0xFF0B0D14),
    surface = Color(0xFF15192A),
    primary = Color(0xFFE63946),
)
