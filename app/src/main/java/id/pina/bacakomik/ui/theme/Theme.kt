package id.pina.bacakomik.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Pina Komik palette — dark navy + red coral accent
val PinaNavy = Color(0xFF0B0D14)        // page bg
val PinaNavyCard = Color(0xFF15192A)    // card bg
val PinaNavyElev = Color(0xFF1C2138)    // elevated card
val PinaRed = Color(0xFFE63946)         // accent
val PinaRedDeep = Color(0xFFC2293B)     // gradient deep
val PinaRedLight = Color(0xFFFF6B7A)    // hover/light
val PinaTextPrimary = Color(0xFFF1F5F9)
val PinaTextSecondary = Color(0xFF94A3B8)
val PinaTextMuted = Color(0xFF64748B)
val PinaDivider = Color(0xFF1E2335)

private val PinaDarkColors = darkColorScheme(
    primary = PinaRed,
    onPrimary = Color.White,
    secondary = PinaRedLight,
    onSecondary = Color.White,
    tertiary = PinaRedDeep,
    background = PinaNavy,
    onBackground = PinaTextPrimary,
    surface = PinaNavyCard,
    onSurface = PinaTextPrimary,
    surfaceVariant = PinaNavyElev,
    onSurfaceVariant = PinaTextSecondary,
    outline = PinaDivider,
    outlineVariant = PinaDivider,
    error = PinaRed,
    onError = Color.White,
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PinaTextPrimary),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PinaTextPrimary),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = PinaTextPrimary),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PinaTextPrimary),
    bodyLarge = TextStyle(fontSize = 16.sp, color = PinaTextPrimary),
    bodyMedium = TextStyle(fontSize = 14.sp, color = PinaTextPrimary),
    bodySmall = TextStyle(fontSize = 12.sp, color = PinaTextSecondary),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.sp, color = PinaTextMuted),
)

@Composable
fun PinaKomikTheme(content: @Composable () -> Unit) {
    // Always dark — design language matches reference fully dark
    MaterialTheme(
        colorScheme = PinaDarkColors,
        typography = AppTypography,
        content = content,
    )
}
