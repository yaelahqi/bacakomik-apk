package id.pina.bacakomik.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Komiku+ inspired palette: white bg, teal accent, dark text
private val OceanCyan = Color(0xFF06B6D4)
private val OceanTeal = Color(0xFF0EA5E9)
private val OceanInk = Color(0xFF080C18)

private val LightColors = lightColorScheme(
    primary = OceanCyan,
    onPrimary = Color.White,
    secondary = OceanTeal,
    onSecondary = Color.White,
    background = Color(0xFFFAFBFD),
    onBackground = Color(0xFF101828),
    surface = Color.White,
    onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
)

private val DarkColors = darkColorScheme(
    primary = OceanCyan,
    onPrimary = Color.White,
    secondary = OceanTeal,
    onSecondary = Color.White,
    background = OceanInk,
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp, color = Color(0xFF64748B)),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun BacaKomikTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
