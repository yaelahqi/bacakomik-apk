package id.pina.bacakomik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private val PinaNavy = Color(0xFF0B0D14)
private val PinaNavyCard = Color(0xFF15192A)
private val PinaRed = Color(0xFFE63946)
private val PinaTextPrimary = Color(0xFFF1F5F9)
private val PinaTextSecondary = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = PinaNavy,
                    surface = PinaNavyCard,
                    primary = PinaRed,
                ),
            ) {
                AppRoot()
            }
        }
    }
}

private sealed class Tab(
    val route: String,
    val label: String,
    val filled: ImageVector,
    val outline: ImageVector,
) {
    object Home : Tab("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Explore : Tab("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
    object Favorit : Tab("favorit", "Favorit", Icons.Filled.Bookmark, Icons.Outlined.Bookmark)
    object Me : Tab("me", "Me", Icons.Filled.Person, Icons.Outlined.Person)
}
private val Tabs = listOf(Tab.Home, Tab.Explore, Tab.Favorit, Tab.Me)

@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = PinaNavy,
        bottomBar = { BottomBar(nav, currentRoute) },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Tab.Home.route)    { Placeholder("🏠 Home") }
            composable(Tab.Explore.route) { Placeholder("🧭 Explore") }
            composable(Tab.Favorit.route) { Placeholder("⭐ Favorit") }
            composable(Tab.Me.route)      { Placeholder("👤 Me") }
        }
    }
}

@Composable
private fun Placeholder(label: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(PinaNavy),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = PinaTextPrimary, fontSize = 32.sp)
    }
}

@Composable
private fun BottomBar(nav: NavHostController, currentRoute: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().background(PinaNavy)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape)
                    .background(if (selected) PinaRed else Color.Transparent)
                    .clickable {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) tab.filled else tab.outline,
                    contentDescription = tab.label,
                    tint = if (selected) Color.White else PinaTextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
