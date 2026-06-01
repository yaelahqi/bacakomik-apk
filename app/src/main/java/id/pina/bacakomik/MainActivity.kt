package id.pina.bacakomik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.pina.bacakomik.ui.DetailScreen
import id.pina.bacakomik.ui.ExploreScreen
import id.pina.bacakomik.ui.HomeScreen
import id.pina.bacakomik.ui.ReaderScreen

// Colors
val PinaNavy = Color(0xFF0B0D14)
val PinaNavyLight = Color(0xFF151822)
val PinaNavyCard = Color(0xFF1A1D2A)
val PinaRed = Color(0xFFE63946)
val PinaGray = Color(0xFF8E919C)

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PinaKomikApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinaKomikApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("home", "Home", Icons.Filled.Home),
        BottomNavItem("explore", "Explore", Icons.Filled.Explore),
        BottomNavItem("favorit", "Favorit", Icons.Filled.Bookmark),
        BottomNavItem("me", "Me", Icons.Filled.Person),
    )

    // Hide bottom bar on detail/reader screens
    val showBottomBar = currentRoute in listOf("home", "explore", "favorit", "me")

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = PinaRed,
            background = PinaNavy,
            surface = PinaNavyCard,
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
        )
    ) {
        Scaffold(
            containerColor = PinaNavy,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = PinaNavyLight) {
                        items.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = PinaGray,
                                    selectedTextColor = Color.White,
                                    unselectedTextColor = PinaGray,
                                    indicatorColor = PinaRed,
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController,
                startDestination = "home",
                Modifier.padding(padding)
            ) {
                composable("home") {
                    HomeScreen(onMangaClick = { slug ->
                        navController.navigate("detail/$slug")
                    })
                }
                composable("explore") {
                    ExploreScreen(onMangaClick = { slug ->
                        navController.navigate("detail/$slug")
                    })
                }
                composable("favorit") { PlaceholderTab("📚", "Favorit") }
                composable("me") { PlaceholderTab("👤", "Me") }

                composable(
                    "detail/{slug}",
                    arguments = listOf(navArgument("slug") { type = NavType.StringType })
                ) { backStack ->
                    val slug = backStack.arguments?.getString("slug") ?: ""
                    DetailScreen(
                        slug = slug,
                        onBack = { navController.popBackStack() },
                        onChapterClick = { chSlug ->
                            navController.navigate("reader/$chSlug")
                        }
                    )
                }

                composable(
                    "reader/{chapterSlug}",
                    arguments = listOf(navArgument("chapterSlug") { type = NavType.StringType })
                ) { backStack ->
                    val chSlug = backStack.arguments?.getString("chapterSlug") ?: ""
                    ReaderScreen(
                        chapterSlug = chSlug,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderTab(emoji: String, label: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(PinaNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("Coming soon...", fontSize = 14.sp, color = PinaGray)
        }
    }
}
